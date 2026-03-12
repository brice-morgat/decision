package com.brilarisoft.lamuertapokerintelligence.service.impl;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionInput;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionResult;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.DecisionStatus;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.StrategyLegend;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionRequestDto;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionResponseDto;
import com.brilarisoft.lamuertapokerintelligence.exception.NotFoundException;
import com.brilarisoft.lamuertapokerintelligence.mapper.DecisionMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.DecisionRuleRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.VillainRangeSetRepository;
import com.brilarisoft.lamuertapokerintelligence.service.DecisionEngineService;
import com.brilarisoft.lamuertapokerintelligence.service.StateReconstructionService;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.DecisionAssembler;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.DecisionInputValidator;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.HeroRangeResolver;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.RuleEngineFacade;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.VillainRangeResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DecisionEngineServiceImpl implements DecisionEngineService {

    private final DecisionMapper decisionMapper;
    private final StrategyProfileRepository strategyProfileRepository;
    private final VillainRangeSetRepository villainRangeSetRepository;
    private final DecisionRuleRepository decisionRuleRepository;
    private final DecisionInputValidator decisionInputValidator;
    private final StateReconstructionService stateReconstructionService;
    private final HeroRangeResolver heroRangeResolver;
    private final VillainRangeResolver villainRangeResolver;
    private final RuleEngineFacade ruleEngineFacade;
    private final DecisionAssembler decisionAssembler;

    public DecisionEngineServiceImpl(
            DecisionMapper decisionMapper,
            StrategyProfileRepository strategyProfileRepository,
            VillainRangeSetRepository villainRangeSetRepository,
            DecisionRuleRepository decisionRuleRepository,
            DecisionInputValidator decisionInputValidator,
            StateReconstructionService stateReconstructionService,
            HeroRangeResolver heroRangeResolver,
            VillainRangeResolver villainRangeResolver,
            RuleEngineFacade ruleEngineFacade,
            DecisionAssembler decisionAssembler
    ) {
        this.decisionMapper = decisionMapper;
        this.strategyProfileRepository = strategyProfileRepository;
        this.villainRangeSetRepository = villainRangeSetRepository;
        this.decisionRuleRepository = decisionRuleRepository;
        this.decisionInputValidator = decisionInputValidator;
        this.stateReconstructionService = stateReconstructionService;
        this.heroRangeResolver = heroRangeResolver;
        this.villainRangeResolver = villainRangeResolver;
        this.ruleEngineFacade = ruleEngineFacade;
        this.decisionAssembler = decisionAssembler;
    }

    @Override
    public DecisionResponseDto decide(DecisionRequestDto request) {
        var strategyProfile = strategyProfileRepository.findById(request.strategyProfileId())
                .orElseThrow(() -> new NotFoundException("Strategy profile not found: " + request.strategyProfileId()));

        if (request.gameType() != strategyProfile.getGameType()) {
            return invalidInputResult("Request gameType must match profile gameType");
        }

        DecisionInput decisionInput = decisionMapper.toDecisionInput(request);
        decisionInput.setStrategyProfile(strategyProfile);
        decisionInput.setGameType(strategyProfile.getGameType());

        try {
            decisionInputValidator.validate(decisionInput);
        } catch (RuntimeException exception) {
            return invalidInputResult(exception.getMessage());
        }

        DecisionContext context = stateReconstructionService.reconstruct(decisionInput);

        var heroRange = heroRangeResolver.resolve(context);
        var villainRange = resolveVillainRange(request, context);
        boolean hasRules = !decisionRuleRepository.findByStrategyProfileIdAndStreetAndScenarioTypeAndActiveTrueOrderByPriorityAsc(
                context.getStrategyProfile().getId(),
                context.getStreet(),
                context.getScenarioType()
        ).isEmpty();

        if (heroRange.isEmpty()) {
            DecisionResult incomplete = new DecisionResult();
            incomplete.setDecisionContext(context);
            incomplete.setStatus(DecisionStatus.INCOMPLETE_CONFIGURATION);
            incomplete.setExplanation("Configuration incomplete pour le contexte courant.");
            incomplete.setWarnings(new ArrayList<>());
            if (heroRange.isEmpty()) {
                incomplete.getWarnings().add("Hero range manquante");
            }
            return decisionMapper.toResponseDto(incomplete, java.util.List.of());
        }

        if (!hasRules) {
            Optional<DecisionResult> rangeFallback = buildRangeLegendFallback(context, "Aucune regle active pour ce contexte");
            if (rangeFallback.isPresent()) {
                return decisionMapper.toResponseDto(rangeFallback.get(), List.of());
            }

            DecisionResult incomplete = new DecisionResult();
            incomplete.setDecisionContext(context);
            incomplete.setStatus(DecisionStatus.INCOMPLETE_CONFIGURATION);
            incomplete.setExplanation("Configuration incomplete pour le contexte courant.");
            incomplete.setWarnings(List.of("Aucune regle active pour ce contexte"));
            return decisionMapper.toResponseDto(incomplete, List.of());
        }

        var selection = ruleEngineFacade.evaluate(context);
        DecisionResult result = decisionAssembler.assemble(context, selection);
        if (result.getStatus() == DecisionStatus.NO_MATCH) {
            Optional<DecisionResult> rangeFallback = buildRangeLegendFallback(context, "Aucune regle n'a matche, fallback range applique");
            if (rangeFallback.isPresent()) {
                return decisionMapper.toResponseDto(rangeFallback.get(), List.of());
            }
        }
        return decisionMapper.toResponseDto(result, decisionAssembler.toMatchedCandidates(selection));
    }

    private DecisionResponseDto invalidInputResult(String message) {
        DecisionResult result = new DecisionResult();
        result.setStatus(DecisionStatus.INVALID_INPUT);
        result.setExplanation(message);
        result.setWarnings(java.util.List.of(message));
        result.setTrace(java.util.List.of("Decision input validation failed"));
        return decisionMapper.toResponseDto(result, java.util.List.of());
    }

    private Optional<DecisionResult> buildRangeLegendFallback(DecisionContext context, String reason) {
        StrategyLegend legend = context.getHeroStrategyLegend();
        if (legend == null) {
            return Optional.empty();
        }

        ActionType action = mapLegendToAction(legend);
        if (action == null) {
            return Optional.empty();
        }

        DecisionResult fallback = new DecisionResult();
        fallback.setDecisionContext(context);
        fallback.setStatus(DecisionStatus.SUCCESS);
        fallback.setRecommendedAction(action);
        fallback.setRecommendedSizingValue(mapLegendToSizing(legend).orElse(null));
        fallback.setExplanation("Decision derivee de la range hero (" + legend + ")");
        fallback.setTrace(List.of(
                reason,
                "Fallback source: HERO_RANGE_LEGEND",
                "Legend: " + legend
        ));
        return Optional.of(fallback);
    }

    private ActionType mapLegendToAction(StrategyLegend legend) {
        return switch (legend) {
            case OPEN -> ActionType.OPEN;
            case CALL, CALL_ONLY, DEFEND, CHECK_CALL -> ActionType.CALL;
            case THREE_BET -> ActionType.THREE_BET;
            case FOUR_BET -> ActionType.FOUR_BET;
            case SHOVE -> ActionType.SHOVE;
            case FOLD, CHECK_FOLD -> ActionType.FOLD;
            case CHECK -> ActionType.CHECK;
            case CHECK_RAISE, ISO_RAISE, RAISE -> ActionType.RAISE;
            case BET_25, BET_50, BET_75 -> ActionType.BET;
        };
    }

    private Optional<java.math.BigDecimal> mapLegendToSizing(StrategyLegend legend) {
        return switch (legend) {
            case BET_25 -> Optional.of(java.math.BigDecimal.valueOf(25));
            case BET_50 -> Optional.of(java.math.BigDecimal.valueOf(50));
            case BET_75 -> Optional.of(java.math.BigDecimal.valueOf(75));
            default -> Optional.empty();
        };
    }

    private Optional<VillainRangeSet> resolveVillainRange(DecisionRequestDto request, DecisionContext context) {
        if (request.villainRangeSetId() != null) {
            VillainRangeSet selectedRange = villainRangeSetRepository.findById(request.villainRangeSetId())
                    .orElseThrow(() -> new NotFoundException("Villain range not found: " + request.villainRangeSetId()));
            if (!selectedRange.getStrategyProfile().getId().equals(context.getStrategyProfile().getId())) {
                throw new NotFoundException("Villain range does not belong to strategy profile: " + request.villainRangeSetId());
            }
            context.setVillainRangeSet(selectedRange);
            return Optional.of(selectedRange);
        }

        if (request.villainRangePercent() != null) {
            double targetPercent = request.villainRangePercent().doubleValue();
            Optional<VillainRangeSet> resolved = villainRangeResolver.resolveByTargetWidth(context, targetPercent);
            resolved.ifPresent(context::setVillainRangeSet);
            return resolved;
        }

        Optional<VillainRangeSet> resolved = villainRangeResolver.resolve(context);
        resolved.ifPresent(context::setVillainRangeSet);
        return resolved;
    }
}
