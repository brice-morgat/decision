package com.brilarisoft.lamuertapokerintelligence.service.impl;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionInput;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionResult;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.DecisionStatus;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionRequestDto;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionResponseDto;
import com.brilarisoft.lamuertapokerintelligence.exception.NotFoundException;
import com.brilarisoft.lamuertapokerintelligence.mapper.DecisionMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.DecisionRuleRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import com.brilarisoft.lamuertapokerintelligence.service.DecisionEngineService;
import com.brilarisoft.lamuertapokerintelligence.service.StateReconstructionService;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.DecisionAssembler;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.DecisionInputValidator;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.HeroRangeResolver;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.RuleEngineFacade;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.VillainRangeResolver;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DecisionEngineServiceImpl implements DecisionEngineService {

    private final DecisionMapper decisionMapper;
    private final StrategyProfileRepository strategyProfileRepository;
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
        var villainRange = villainRangeResolver.resolve(context);
        boolean hasRules = !decisionRuleRepository.findByStrategyProfileIdAndStreetAndScenarioTypeAndActiveTrueOrderByPriorityAsc(
                context.getStrategyProfile().getId(),
                context.getStreet(),
                context.getScenarioType()
        ).isEmpty();

        if (heroRange.isEmpty() || !hasRules || requiresVillainRange(context, villainRange.isPresent())) {
            DecisionResult incomplete = new DecisionResult();
            incomplete.setDecisionContext(context);
            incomplete.setStatus(DecisionStatus.INCOMPLETE_CONFIGURATION);
            incomplete.setExplanation("Configuration incomplete pour le contexte courant.");
            incomplete.setWarnings(new ArrayList<>());
            if (heroRange.isEmpty()) {
                incomplete.getWarnings().add("Hero range manquante");
            }
            if (!villainRange.isPresent() && context.getFacingActionType() != null) {
                incomplete.getWarnings().add("Villain range manquante");
            }
            if (!hasRules) {
                incomplete.getWarnings().add("Aucune regle active pour ce contexte");
            }
            return decisionMapper.toResponseDto(incomplete, java.util.List.of());
        }

        var selection = ruleEngineFacade.evaluate(context);
        DecisionResult result = decisionAssembler.assemble(context, selection);
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

    private boolean requiresVillainRange(DecisionContext context, boolean villainRangePresent) {
        return !villainRangePresent && context.getFacingActionType() != null;
    }
}
