package com.brilarisoft.lamuertapokerintelligence.service.impl;

import com.brilarisoft.lamuertapokerintelligence.domain.range.PokerHandCodeCatalog;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeCellDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeCellsUpdateDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeContextQueryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.exception.BusinessValidationException;
import com.brilarisoft.lamuertapokerintelligence.exception.NotFoundException;
import com.brilarisoft.lamuertapokerintelligence.mapper.VillainRangeMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.VillainRangeSetRepository;
import com.brilarisoft.lamuertapokerintelligence.service.VillainRangeService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages villain range lifecycle and transactional replacement of matrix cells.
 */
@Service
@Transactional
public class VillainRangeServiceImpl implements VillainRangeService {

    private final VillainRangeSetRepository villainRangeSetRepository;
    private final StrategyProfileRepository strategyProfileRepository;
    private final VillainRangeMapper villainRangeMapper;

    public VillainRangeServiceImpl(
            VillainRangeSetRepository villainRangeSetRepository,
            StrategyProfileRepository strategyProfileRepository,
            VillainRangeMapper villainRangeMapper
    ) {
        this.villainRangeSetRepository = villainRangeSetRepository;
        this.strategyProfileRepository = strategyProfileRepository;
        this.villainRangeMapper = villainRangeMapper;
    }

    @Override
    public VillainRangeDetailDto createRange(VillainRangeUpsertDto request) {
        VillainRangeSet rangeSet = new VillainRangeSet();
        villainRangeMapper.updateEntity(rangeSet, request);
        rangeSet.setStrategyProfile(strategyProfileRepository.findById(request.profileId())
                .orElseThrow(() -> new NotFoundException("Strategy profile not found: " + request.profileId())));
        validateLogicalUniqueness(request, null);
        return villainRangeMapper.toDetailDto(villainRangeSetRepository.save(rangeSet));
    }

    @Override
    public VillainRangeDetailDto updateRange(UUID rangeId, VillainRangeUpsertDto request) {
        VillainRangeSet rangeSet = getRequiredRange(rangeId);
        if (!rangeSet.getStrategyProfile().getId().equals(request.profileId())) {
            rangeSet.setStrategyProfile(strategyProfileRepository.findById(request.profileId())
                    .orElseThrow(() -> new NotFoundException("Strategy profile not found: " + request.profileId())));
        }
        villainRangeMapper.updateEntity(rangeSet, request);
        validateLogicalUniqueness(request, rangeId);
        return villainRangeMapper.toDetailDto(villainRangeSetRepository.save(rangeSet));
    }

    @Override
    public void deleteRange(UUID rangeId) {
        villainRangeSetRepository.delete(getRequiredRange(rangeId));
    }

    @Override
    @Transactional(readOnly = true)
    public VillainRangeDetailDto getRange(UUID rangeId) {
        return villainRangeMapper.toDetailDto(getRequiredRange(rangeId));
    }

    @Override
    @Transactional(readOnly = true)
    public VillainRangeDetailDto getRangeByContext(VillainRangeContextQueryDto query) {
        validateContext(query);
        VillainRangeSet rangeSet = villainRangeSetRepository
                .findByStrategyProfileIdAndGameTypeAndStreetAndVillainPositionAndHeroPositionAndScenarioTypeAndTriggerActionTypeAndLineSignature(
                        query.profileId(),
                        query.gameType(),
                        query.street(),
                        query.villainPosition(),
                        query.heroPosition(),
                        query.scenarioType(),
                        query.triggerActionCode(),
                        normalizeNullable(query.lineSignature())
                )
                .orElseThrow(() -> new NotFoundException("Villain range not found for supplied context"));
        return villainRangeMapper.toDetailDto(rangeSet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VillainRangeSummaryDto> listByProfile(UUID profileId) {
        return villainRangeSetRepository.findByStrategyProfileIdOrderByUpdatedAtDesc(profileId)
                .stream()
                .map(villainRangeMapper::toSummaryDto)
                .toList();
    }

    @Override
    public VillainRangeDetailDto updateCells(UUID rangeId, VillainRangeCellsUpdateDto request) {
        VillainRangeSet rangeSet = getRequiredRange(rangeId);
        validateCells(request.cells());
        rangeSet.getCells().clear();
        rangeSet.getCells().addAll(request.cells().stream()
                .map(dto -> villainRangeMapper.toCellEntity(dto, rangeSet))
                .toList());
        return villainRangeMapper.toDetailDto(villainRangeSetRepository.save(rangeSet));
    }

    private VillainRangeSet getRequiredRange(UUID rangeId) {
        return villainRangeSetRepository.findById(rangeId)
                .orElseThrow(() -> new NotFoundException("Villain range not found: " + rangeId));
    }

    private void validateLogicalUniqueness(VillainRangeUpsertDto request, UUID currentRangeId) {
        villainRangeSetRepository.findByStrategyProfileIdAndGameTypeAndStreetAndVillainPositionAndHeroPositionAndScenarioTypeAndTriggerActionTypeAndLineSignature(
                        request.profileId(),
                        request.gameType(),
                        request.street(),
                        request.villainPosition(),
                        request.heroPosition(),
                        request.scenarioType(),
                        request.triggerActionCode(),
                        normalizeNullable(request.lineSignature())
                )
                .filter(existing -> currentRangeId == null || existing.getId() == null || !existing.getId().equals(currentRangeId))
                .ifPresent(existing -> {
                    throw new BusinessValidationException("A villain range already exists for this context");
                });
    }

    private void validateContext(VillainRangeContextQueryDto query) {
        if (query.profileId() == null || query.gameType() == null || query.street() == null
                || query.villainPosition() == null || query.scenarioType() == null) {
            throw new BusinessValidationException("Villain range context is incomplete");
        }
    }

    private void validateCells(List<VillainRangeCellDto> cells) {
        Set<String> uniqueHandCodes = new HashSet<>();
        for (VillainRangeCellDto cell : cells) {
            String normalizedHandCode = PokerHandCodeCatalog.normalize(cell.handCode());
            if (!PokerHandCodeCatalog.isValid(normalizedHandCode)) {
                throw new BusinessValidationException("Invalid hand code: " + cell.handCode());
            }
            if (!uniqueHandCodes.add(normalizedHandCode)) {
                throw new BusinessValidationException("Duplicate hand code in villain range: " + normalizedHandCode);
            }
            if (cell.weight() != null && cell.weight() < 0) {
                throw new BusinessValidationException("Villain range weight must be positive or null");
            }
        }
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
