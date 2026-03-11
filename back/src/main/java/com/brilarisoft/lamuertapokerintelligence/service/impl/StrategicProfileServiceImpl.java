package com.brilarisoft.lamuertapokerintelligence.service.impl;

import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeCell;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeCell;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.DecisionRule;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.RuleAction;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.RuleCondition;
import com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile.StrategyProfile;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.DuplicateStrategyProfileDto;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.exception.BusinessValidationException;
import com.brilarisoft.lamuertapokerintelligence.exception.NotFoundException;
import com.brilarisoft.lamuertapokerintelligence.mapper.StrategyProfileMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import com.brilarisoft.lamuertapokerintelligence.service.StrategicProfileService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service responsible for the strategic profile lifecycle.
 */
@Service
@Transactional
public class StrategicProfileServiceImpl implements StrategicProfileService {

    private final StrategyProfileRepository strategyProfileRepository;
    private final StrategyProfileMapper strategyProfileMapper;

    public StrategicProfileServiceImpl(
            StrategyProfileRepository strategyProfileRepository,
            StrategyProfileMapper strategyProfileMapper
    ) {
        this.strategyProfileRepository = strategyProfileRepository;
        this.strategyProfileMapper = strategyProfileMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StrategyProfileSummaryDto> listProfiles() {
        return strategyProfileRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(strategyProfileMapper::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StrategyProfileDetailDto getProfile(UUID profileId) {
        return strategyProfileMapper.toDetailDto(getRequiredProfile(profileId));
    }

    @Override
    @Transactional(readOnly = true)
    public StrategyProfileDetailDto getActiveProfile() {
        return strategyProfileRepository.findByActiveTrueAndArchivedFalse()
                .map(strategyProfileMapper::toDetailDto)
                .orElseThrow(() -> new NotFoundException("No active strategy profile found"));
    }

    @Override
    public StrategyProfileDetailDto createProfile(StrategyProfileUpsertDto request) {
        validateProfileFlags(request.active(), request.archived());
        enforceUniqueName(request.name(), null);

        StrategyProfile profile = strategyProfileMapper.toNewEntity(request);
        if (profile.isActive()) {
            deactivateOtherProfiles(profile.getGameType(), null);
        }

        return strategyProfileMapper.toDetailDto(strategyProfileRepository.save(profile));
    }

    @Override
    public StrategyProfileDetailDto updateProfile(UUID profileId, StrategyProfileUpsertDto request) {
        validateProfileFlags(request.active(), request.archived());
        StrategyProfile profile = getRequiredProfile(profileId);
        enforceUniqueName(request.name(), profile.getId());

        strategyProfileMapper.updateEntity(profile, request);
        if (profile.isActive()) {
            deactivateOtherProfiles(profile.getGameType(), profile.getId());
        }

        return strategyProfileMapper.toDetailDto(strategyProfileRepository.save(profile));
    }

    @Override
    public void deleteProfile(UUID profileId) {
        StrategyProfile profile = getRequiredProfile(profileId);
        strategyProfileRepository.delete(profile);
    }

    @Override
    public StrategyProfileDetailDto activateProfile(UUID profileId) {
        StrategyProfile profile = getRequiredProfile(profileId);
        if (profile.isArchived()) {
            throw new BusinessValidationException("Archived profiles cannot be activated");
        }

        deactivateOtherProfiles(profile.getGameType(), profile.getId());
        profile.setActive(true);
        return strategyProfileMapper.toDetailDto(strategyProfileRepository.save(profile));
    }

    @Override
    public StrategyProfileDetailDto duplicateProfile(UUID profileId, DuplicateStrategyProfileDto request) {
        StrategyProfile source = getRequiredProfile(profileId);
        validateProfileFlags(request.activateCopy(), false);
        enforceUniqueName(request.name(), null);

        StrategyProfile copy = copyProfile(source, request.name().trim(), request.activateCopy());
        if (copy.isActive()) {
            deactivateOtherProfiles(copy.getGameType(), null);
        }

        return strategyProfileMapper.toDetailDto(strategyProfileRepository.save(copy));
    }

    private StrategyProfile getRequiredProfile(UUID profileId) {
        return strategyProfileRepository.findById(profileId)
                .orElseThrow(() -> new NotFoundException("Strategy profile not found: " + profileId));
    }

    private void enforceUniqueName(String name, UUID excludedProfileId) {
        strategyProfileRepository.findByNameIgnoreCase(name.trim())
                .filter(existing -> !existing.getId().equals(excludedProfileId))
                .ifPresent(existing -> {
                    throw new BusinessValidationException("A strategy profile with this name already exists");
                });
    }

    private void validateProfileFlags(boolean active, boolean archived) {
        if (active && archived) {
            throw new BusinessValidationException("A profile cannot be active and archived at the same time");
        }
    }

    private void deactivateOtherProfiles(com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType gameType, UUID excludedProfileId) {
        List<StrategyProfile> activeProfiles = strategyProfileRepository
                .findAllByGameTypeAndActiveTrueAndArchivedFalse(gameType);
        for (StrategyProfile profile : activeProfiles) {
            if (excludedProfileId != null && excludedProfileId.equals(profile.getId())) {
                continue;
            }
            profile.setActive(false);
        }
    }

    private StrategyProfile copyProfile(StrategyProfile source, String newName, boolean activeCopy) {
        StrategyProfile copy = new StrategyProfile();
        copy.setName(newName);
        copy.setDescription(source.getDescription());
        copy.setGameType(source.getGameType());
        copy.setActive(activeCopy);
        copy.setArchived(false);
        copy.setVersionLabel(source.getVersionLabel());

        copy.setHeroRangeSets(source.getHeroRangeSets().stream().map(range -> copyHeroRangeSet(range, copy)).toList());
        copy.setVillainRangeSets(source.getVillainRangeSets().stream().map(range -> copyVillainRangeSet(range, copy)).toList());
        copy.setDecisionRules(source.getDecisionRules().stream().map(rule -> copyDecisionRule(rule, copy)).toList());
        return copy;
    }

    private HeroRangeSet copyHeroRangeSet(HeroRangeSet source, StrategyProfile targetProfile) {
        HeroRangeSet copy = new HeroRangeSet();
        copy.setStrategyProfile(targetProfile);
        copy.setName(source.getName());
        copy.setDescription(source.getDescription());
        copy.setGameType(source.getGameType());
        copy.setStreet(source.getStreet());
        copy.setHeroPosition(source.getHeroPosition());
        copy.setScenarioType(source.getScenarioType());
        copy.setSubScenarioCode(source.getSubScenarioCode());
        copy.setEnabled(source.isEnabled());
        copy.setPriority(source.getPriority());
        copy.setNotes(source.getNotes());
        copy.setCells(source.getCells().stream().map(cell -> copyHeroRangeCell(cell, copy)).toList());
        return copy;
    }

    private HeroRangeCell copyHeroRangeCell(HeroRangeCell source, HeroRangeSet targetSet) {
        HeroRangeCell copy = new HeroRangeCell();
        copy.setHeroRangeSet(targetSet);
        copy.setHandCode(source.getHandCode());
        copy.setEnabled(source.isEnabled());
        copy.setStrategyLegend(source.getStrategyLegend());
        copy.setColorCode(source.getColorCode());
        copy.setNote(source.getNote());
        copy.setWeightPercent(source.getWeightPercent());
        return copy;
    }

    private VillainRangeSet copyVillainRangeSet(VillainRangeSet source, StrategyProfile targetProfile) {
        VillainRangeSet copy = new VillainRangeSet();
        copy.setStrategyProfile(targetProfile);
        copy.setName(source.getName());
        copy.setDescription(source.getDescription());
        copy.setGameType(source.getGameType());
        copy.setStreet(source.getStreet());
        copy.setVillainPosition(source.getVillainPosition());
        copy.setHeroPosition(source.getHeroPosition());
        copy.setScenarioType(source.getScenarioType());
        copy.setTriggerActionType(source.getTriggerActionType());
        copy.setLineSignature(source.getLineSignature());
        copy.setEnabled(source.isEnabled());
        copy.setPriority(source.getPriority());
        copy.setNotes(source.getNotes());
        copy.setCells(source.getCells().stream().map(cell -> copyVillainRangeCell(cell, copy)).toList());
        return copy;
    }

    private VillainRangeCell copyVillainRangeCell(VillainRangeCell source, VillainRangeSet targetSet) {
        VillainRangeCell copy = new VillainRangeCell();
        copy.setVillainRangeSet(targetSet);
        copy.setHandCode(source.getHandCode());
        copy.setEnabled(source.isEnabled());
        copy.setWeightPercent(source.getWeightPercent());
        copy.setTagCode(source.getTagCode());
        copy.setNote(source.getNote());
        return copy;
    }

    private DecisionRule copyDecisionRule(DecisionRule source, StrategyProfile targetProfile) {
        DecisionRule copy = new DecisionRule();
        copy.setStrategyProfile(targetProfile);
        copy.setName(source.getName());
        copy.setDescription(source.getDescription());
        copy.setGameType(source.getGameType());
        copy.setStreet(source.getStreet());
        copy.setScenarioType(source.getScenarioType());
        copy.setHeroPosition(source.getHeroPosition());
        copy.setVillainPosition(source.getVillainPosition());
        copy.setHeroHandCode(source.getHeroHandCode());
        copy.setHeroStrategyLegend(source.getHeroStrategyLegend());
        copy.setFacingActionType(source.getFacingActionType());
        copy.setLineSignature(source.getLineSignature());
        copy.setSizingConditionCode(source.getSizingConditionCode());
        copy.setPriority(source.getPriority());
        copy.setActive(source.isActive());
        copy.setStopOnMatch(source.isStopOnMatch());
        copy.setNote(source.getNote());
        copy.setConditions(source.getConditions().stream().map(condition -> copyRuleCondition(condition, copy)).toList());
        copy.setActions(source.getActions().stream().map(action -> copyRuleAction(action, copy)).toList());
        return copy;
    }

    private RuleCondition copyRuleCondition(RuleCondition source, DecisionRule targetRule) {
        RuleCondition copy = new RuleCondition();
        copy.setDecisionRule(targetRule);
        copy.setConditionType(source.getConditionType());
        copy.setOperator(source.getOperator());
        copy.setValueType(source.getValueType());
        copy.setExpectedValue(source.getExpectedValue());
        copy.setSecondaryValue(source.getSecondaryValue());
        copy.setConditionOrder(source.getConditionOrder());
        return copy;
    }

    private RuleAction copyRuleAction(RuleAction source, DecisionRule targetRule) {
        RuleAction copy = new RuleAction();
        copy.setDecisionRule(targetRule);
        copy.setActionType(source.getActionType());
        copy.setSizingType(source.getSizingType());
        copy.setSizingValue(source.getSizingValue());
        copy.setExecutionOrder(source.getExecutionOrder());
        copy.setMessageTemplate(source.getMessageTemplate());
        return copy;
    }
}
