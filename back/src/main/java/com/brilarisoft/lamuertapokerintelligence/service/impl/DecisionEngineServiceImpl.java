package com.brilarisoft.lamuertapokerintelligence.service.impl;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionInput;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionResult;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.DecisionStatus;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.StrategyLegend;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionEquityDto;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionRequestDto;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionResponseDto;
import com.brilarisoft.lamuertapokerintelligence.exception.NotFoundException;
import com.brilarisoft.lamuertapokerintelligence.mapper.DecisionMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.HeroRangeSetRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.VillainRangeSetRepository;
import com.brilarisoft.lamuertapokerintelligence.service.DecisionEngineService;
import com.brilarisoft.lamuertapokerintelligence.service.StateReconstructionService;
import com.brilarisoft.lamuertapokerintelligence.service.decision.DecisionAnalysisAssembler;
import com.brilarisoft.lamuertapokerintelligence.service.decision.DecisionDiagnosticsService;
import com.brilarisoft.lamuertapokerintelligence.service.decision.HeroRangeDecisionResolver;
import com.brilarisoft.lamuertapokerintelligence.service.decision.RangeDecisionComputation;
import com.brilarisoft.lamuertapokerintelligence.service.decision.VillainRangeAdjustmentService;
import com.brilarisoft.lamuertapokerintelligence.service.equity.EquityAnalysisService;
import com.brilarisoft.lamuertapokerintelligence.service.range.SyntheticVillainRangeFactory;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.DecisionInputValidator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DecisionEngineServiceImpl implements DecisionEngineService {

    private final DecisionMapper decisionMapper;
    private final StrategyProfileRepository strategyProfileRepository;
    private final HeroRangeSetRepository heroRangeSetRepository;
    private final VillainRangeSetRepository villainRangeSetRepository;
    private final DecisionInputValidator decisionInputValidator;
    private final StateReconstructionService stateReconstructionService;
    private final HeroRangeDecisionResolver heroRangeDecisionResolver;
    private final VillainRangeAdjustmentService villainRangeAdjustmentService;
    private final DecisionAnalysisAssembler decisionAnalysisAssembler;
    private final DecisionDiagnosticsService decisionDiagnosticsService;
    private final EquityAnalysisService equityAnalysisService;
    private final SyntheticVillainRangeFactory syntheticVillainRangeFactory;

    public DecisionEngineServiceImpl(
            DecisionMapper decisionMapper,
            StrategyProfileRepository strategyProfileRepository,
            HeroRangeSetRepository heroRangeSetRepository,
            VillainRangeSetRepository villainRangeSetRepository,
            DecisionInputValidator decisionInputValidator,
            StateReconstructionService stateReconstructionService,
            HeroRangeDecisionResolver heroRangeDecisionResolver,
            VillainRangeAdjustmentService villainRangeAdjustmentService,
            DecisionAnalysisAssembler decisionAnalysisAssembler,
            DecisionDiagnosticsService decisionDiagnosticsService,
            EquityAnalysisService equityAnalysisService,
            SyntheticVillainRangeFactory syntheticVillainRangeFactory
    ) {
        this.decisionMapper = decisionMapper;
        this.strategyProfileRepository = strategyProfileRepository;
        this.heroRangeSetRepository = heroRangeSetRepository;
        this.villainRangeSetRepository = villainRangeSetRepository;
        this.decisionInputValidator = decisionInputValidator;
        this.stateReconstructionService = stateReconstructionService;
        this.heroRangeDecisionResolver = heroRangeDecisionResolver;
        this.villainRangeAdjustmentService = villainRangeAdjustmentService;
        this.decisionAnalysisAssembler = decisionAnalysisAssembler;
        this.decisionDiagnosticsService = decisionDiagnosticsService;
        this.equityAnalysisService = equityAnalysisService;
        this.syntheticVillainRangeFactory = syntheticVillainRangeFactory;
    }

    @Override
    public DecisionResponseDto decide(DecisionRequestDto request) {
        var strategyProfile = strategyProfileRepository.findById(request.strategyProfileId())
                .orElseThrow(() -> new NotFoundException("Strategy profile not found: " + request.strategyProfileId()));

        HeroRangeSet heroRangeSet = heroRangeSetRepository.findById(request.heroRangeSetId())
                .orElseThrow(() -> new NotFoundException("Hero range not found: " + request.heroRangeSetId()));
        validateHeroRangeOwnership(strategyProfile.getId(), heroRangeSet);
        Optional<VillainRangeSet> selectedVillainRange = resolveSelectedVillainRange(strategyProfile.getId(), request.villainRangeSetId());

        DecisionInput decisionInput = decisionMapper.toDecisionInput(request);
        decisionInput.setStrategyProfile(strategyProfile);
        decisionInput.setGameType(strategyProfile.getGameType());
        decisionInput.setHeroPosition(resolveHeroPosition(request, heroRangeSet));
        decisionInput.setVillainPosition(resolveVillainPosition(request, selectedVillainRange));
        decisionInput.setStreet(resolveStreet(request, heroRangeSet, selectedVillainRange));
        decisionInput.setScenarioType(request.scenarioType());
        decisionInput.setEffectiveStackInBigBlinds(request.effectiveStackInBigBlinds() == null
                ? java.math.BigDecimal.valueOf(100)
                : request.effectiveStackInBigBlinds());
        decisionInput.setPotSizeInBigBlinds(request.potSizeInBigBlinds() == null
                ? java.math.BigDecimal.ZERO
                : request.potSizeInBigBlinds());

        try {
            decisionInputValidator.validate(decisionInput);
        } catch (RuntimeException exception) {
            return invalidInputResult(exception.getMessage());
        }

        DecisionContext context = stateReconstructionService.reconstruct(decisionInput);
        context.setHeroRangeSet(heroRangeSet);
        if (selectedVillainRange.isEmpty()) {
            selectedVillainRange = resolveFallbackVillainRange(request, context);
        }
        selectedVillainRange.ifPresent(context::setVillainRangeSet);

        Optional<RangeDecisionComputation> heroDecision = heroRangeDecisionResolver.resolve(context, heroRangeSet);
        if (heroDecision.isEmpty()) {
            DecisionResult foldResult = buildFoldResult(context, "Main absente ou inactive dans la range hero");
            DecisionEquityDto foldEquity = equityAnalysisService.analyze(context, selectedVillainRange.orElse(null));
            DecisionDiagnosticsService.DecisionDiagnostics diagnostics = decisionDiagnosticsService.enrich(context, foldResult);
            return decisionMapper.toResponseDto(
                    foldResult,
                    foldEquity,
                    diagnostics.matchedCandidates()
            );
        }

        RangeDecisionComputation decision = heroDecision.get();
        DecisionEquityDto equity = equityAnalysisService.analyze(context, selectedVillainRange.orElse(null));
        if (selectedVillainRange.isPresent()) {
            decision = villainRangeAdjustmentService.adjust(context, selectedVillainRange.get(), heroDecision.get());
        }

        DecisionResult result = decisionAnalysisAssembler.success(context, decision);
        DecisionDiagnosticsService.DecisionDiagnostics diagnostics = decisionDiagnosticsService.enrich(context, result);
        return decisionMapper.toResponseDto(
                result,
                equity,
                diagnostics.matchedCandidates()
        );
    }

    private void validateHeroRangeOwnership(java.util.UUID strategyProfileId, HeroRangeSet heroRangeSet) {
        if (!heroRangeSet.getStrategyProfile().getId().equals(strategyProfileId)) {
            throw new NotFoundException("Hero range does not belong to strategy profile: " + heroRangeSet.getId());
        }
    }

    private Optional<VillainRangeSet> resolveSelectedVillainRange(java.util.UUID strategyProfileId, java.util.UUID villainRangeSetId) {
        if (villainRangeSetId == null) {
            return Optional.empty();
        }

        VillainRangeSet villainRangeSet = villainRangeSetRepository.findById(villainRangeSetId)
                .orElseThrow(() -> new NotFoundException("Villain range not found: " + villainRangeSetId));
        if (!villainRangeSet.getStrategyProfile().getId().equals(strategyProfileId)) {
            throw new NotFoundException("Villain range does not belong to strategy profile: " + villainRangeSetId);
        }
        return Optional.of(villainRangeSet);
    }

    private Position resolveHeroPosition(DecisionRequestDto request, HeroRangeSet heroRangeSet) {
        return request.heroPosition() != null ? request.heroPosition() : heroRangeSet.getHeroPosition();
    }

    private Position resolveVillainPosition(DecisionRequestDto request, Optional<VillainRangeSet> villainRangeSet) {
        if (request.villainPosition() != null) {
            return request.villainPosition();
        }
        if (villainRangeSet.isPresent() && villainRangeSet.get().getVillainPosition() != null) {
            return villainRangeSet.get().getVillainPosition();
        }
        return defaultVillainPosition(request.heroPosition());
    }

    private Position defaultVillainPosition(Position heroPosition) {
        if (heroPosition == null) {
            return Position.BB;
        }
        return heroPosition == Position.BB ? Position.BTN : Position.BB;
    }

    private Street resolveStreet(DecisionRequestDto request, HeroRangeSet heroRangeSet, Optional<VillainRangeSet> villainRangeSet) {
        if (request.street() != null) {
            return request.street();
        }
        if (heroRangeSet.getStreet() != null) {
            return heroRangeSet.getStreet();
        }
        return villainRangeSet.map(VillainRangeSet::getStreet).orElse(Street.PREFLOP);
    }

    private DecisionResult buildFoldResult(DecisionContext context, String reason) {
        return decisionAnalysisAssembler.success(
                context,
                new RangeDecisionComputation(
                        ActionType.FOLD,
                        null,
                        reason,
                        "HERO_RANGE",
                        StrategyLegend.FOLD,
                        null,
                        null,
                        List.of("Decision source: HERO_RANGE", "Resolution mode: RANGE_ONLY")
                )
        );
    }

    private DecisionResponseDto invalidInputResult(String message) {
        DecisionResult result = new DecisionResult();
        result.setStatus(DecisionStatus.INVALID_INPUT);
        result.setExplanation(message);
        result.setWarnings(java.util.List.of(message));
        result.setTrace(java.util.List.of("Decision input validation failed"));
        return decisionMapper.toResponseDto(result, null, java.util.List.of());
    }

    private Optional<VillainRangeSet> resolveFallbackVillainRange(DecisionRequestDto request, DecisionContext context) {
        Double targetPercent = request.villainRangePercent() == null
                ? syntheticVillainRangeFactory.defaultPercent()
                : request.villainRangePercent().doubleValue();
        return Optional.of(syntheticVillainRangeFactory.create(context, targetPercent));
    }
}
