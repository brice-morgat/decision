package com.brilarisoft.lamuertapokerintelligence.service.impl;

import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.range.PokerHandCodeCatalog;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeCellDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeCellsUpdateDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeContextQueryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.exception.BusinessValidationException;
import com.brilarisoft.lamuertapokerintelligence.exception.NotFoundException;
import com.brilarisoft.lamuertapokerintelligence.mapper.HeroRangeMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.HeroRangeSetRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import com.brilarisoft.lamuertapokerintelligence.service.HeroRangeService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages hero range lifecycle and transactional replacement of matrix cells.
 */
@Service
@Transactional
public class HeroRangeServiceImpl implements HeroRangeService {

    private final HeroRangeSetRepository heroRangeSetRepository;
    private final StrategyProfileRepository strategyProfileRepository;
    private final HeroRangeMapper heroRangeMapper;

    public HeroRangeServiceImpl(
            HeroRangeSetRepository heroRangeSetRepository,
            StrategyProfileRepository strategyProfileRepository,
            HeroRangeMapper heroRangeMapper
    ) {
        this.heroRangeSetRepository = heroRangeSetRepository;
        this.strategyProfileRepository = strategyProfileRepository;
        this.heroRangeMapper = heroRangeMapper;
    }

    @Override
    public HeroRangeDetailDto createRange(HeroRangeUpsertDto request) {
        HeroRangeSet rangeSet = new HeroRangeSet();
        heroRangeMapper.updateEntity(rangeSet, request);
        rangeSet.setStrategyProfile(strategyProfileRepository.findById(request.profileId())
                .orElseThrow(() -> new NotFoundException("Strategy profile not found: " + request.profileId())));
        validateLogicalUniqueness(request, null);
        return heroRangeMapper.toDetailDto(heroRangeSetRepository.save(rangeSet));
    }

    @Override
    public HeroRangeDetailDto updateRange(UUID rangeId, HeroRangeUpsertDto request) {
        HeroRangeSet rangeSet = getRequiredRange(rangeId);
        if (!rangeSet.getStrategyProfile().getId().equals(request.profileId())) {
            rangeSet.setStrategyProfile(strategyProfileRepository.findById(request.profileId())
                    .orElseThrow(() -> new NotFoundException("Strategy profile not found: " + request.profileId())));
        }
        heroRangeMapper.updateEntity(rangeSet, request);
        validateLogicalUniqueness(request, rangeId);
        return heroRangeMapper.toDetailDto(heroRangeSetRepository.save(rangeSet));
    }

    @Override
    public void deleteRange(UUID rangeId) {
        heroRangeSetRepository.delete(getRequiredRange(rangeId));
    }

    @Override
    @Transactional(readOnly = true)
    public HeroRangeDetailDto getRange(UUID rangeId) {
        return heroRangeMapper.toDetailDto(getRequiredRange(rangeId));
    }

    @Override
    @Transactional(readOnly = true)
    public HeroRangeDetailDto getRangeByContext(HeroRangeContextQueryDto query) {
        validateContext(query);
        HeroRangeSet rangeSet = heroRangeSetRepository
                .findByStrategyProfileIdAndGameTypeAndStreetAndHeroPositionAndScenarioTypeAndSubScenarioCode(
                        query.profileId(),
                        query.gameType(),
                        query.street(),
                        query.heroPosition(),
                        query.scenarioType(),
                        normalizeNullable(query.subScenarioCode())
                )
                .orElseThrow(() -> new NotFoundException("Hero range not found for supplied context"));
        return heroRangeMapper.toDetailDto(rangeSet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HeroRangeSummaryDto> listByProfile(UUID profileId) {
        return heroRangeSetRepository.findByStrategyProfileIdOrderByUpdatedAtDesc(profileId)
                .stream()
                .map(heroRangeMapper::toSummaryDto)
                .toList();
    }

    @Override
    public HeroRangeDetailDto updateCells(UUID rangeId, HeroRangeCellsUpdateDto request) {
        HeroRangeSet rangeSet = getRequiredRange(rangeId);
        validateCells(request.cells());
        rangeSet.getCells().clear();
        rangeSet.getCells().addAll(request.cells().stream()
                .map(dto -> heroRangeMapper.toCellEntity(dto, rangeSet))
                .toList());
        return heroRangeMapper.toDetailDto(heroRangeSetRepository.save(rangeSet));
    }

    private HeroRangeSet getRequiredRange(UUID rangeId) {
        return heroRangeSetRepository.findById(rangeId)
                .orElseThrow(() -> new NotFoundException("Hero range not found: " + rangeId));
    }

    private void validateLogicalUniqueness(HeroRangeUpsertDto request, UUID currentRangeId) {
        heroRangeSetRepository.findByStrategyProfileIdAndGameTypeAndStreetAndHeroPositionAndScenarioTypeAndSubScenarioCode(
                        request.profileId(),
                        request.gameType(),
                        request.street(),
                        request.heroPosition(),
                        request.scenarioType(),
                        normalizeNullable(request.subScenarioCode())
                )
                .filter(existing -> currentRangeId == null || existing.getId() == null || !existing.getId().equals(currentRangeId))
                .ifPresent(existing -> {
                    throw new BusinessValidationException("A hero range already exists for this context");
                });
    }

    private void validateContext(HeroRangeContextQueryDto query) {
        if (query.profileId() == null || query.gameType() == null || query.street() == null
                || query.heroPosition() == null || query.scenarioType() == null) {
            throw new BusinessValidationException("Hero range context is incomplete");
        }
    }

    private void validateCells(List<HeroRangeCellDto> cells) {
        Set<String> uniqueHandCodes = new HashSet<>();
        for (HeroRangeCellDto cell : cells) {
            String normalizedHandCode = PokerHandCodeCatalog.normalize(cell.handCode());
            if (!PokerHandCodeCatalog.isValid(normalizedHandCode)) {
                throw new BusinessValidationException("Invalid hand code: " + cell.handCode());
            }
            if (!uniqueHandCodes.add(normalizedHandCode)) {
                throw new BusinessValidationException("Duplicate hand code in hero range: " + normalizedHandCode);
            }
            if (cell.enabled() && cell.legendCode() == null) {
                throw new BusinessValidationException("Enabled hero range cells require a legend code");
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
