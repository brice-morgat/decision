package com.brilarisoft.lamuertapokerintelligence.service.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionResult;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.MatchedRuleCandidateDto;
import com.brilarisoft.lamuertapokerintelligence.repository.DecisionRuleRepository;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.DecisionAssembler;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.ResolvedRuleSelection;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.RuleEngineFacade;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DecisionDiagnosticsService {

    private final DecisionRuleRepository decisionRuleRepository;
    private final RuleEngineFacade ruleEngineFacade;
    private final DecisionAssembler decisionAssembler;
    private final DecisionAnalysisAssembler decisionAnalysisAssembler;

    public DecisionDiagnosticsService(
            DecisionRuleRepository decisionRuleRepository,
            RuleEngineFacade ruleEngineFacade,
            DecisionAssembler decisionAssembler,
            DecisionAnalysisAssembler decisionAnalysisAssembler
    ) {
        this.decisionRuleRepository = decisionRuleRepository;
        this.ruleEngineFacade = ruleEngineFacade;
        this.decisionAssembler = decisionAssembler;
        this.decisionAnalysisAssembler = decisionAnalysisAssembler;
    }

    public DecisionDiagnostics enrich(DecisionContext context, DecisionResult result) {
        if (context.getScenarioType() == null) {
            return DecisionDiagnostics.empty();
        }

        boolean hasRules = !decisionRuleRepository.findByStrategyProfileIdAndStreetAndScenarioTypeAndActiveTrueOrderByPriorityAsc(
                context.getStrategyProfile().getId(),
                context.getStreet(),
                context.getScenarioType()
        ).isEmpty();
        if (!hasRules) {
            return DecisionDiagnostics.empty();
        }

        ResolvedRuleSelection selection = ruleEngineFacade.evaluate(context);
        if (selection.winningCandidate() != null) {
            result.setMatchedRule(selection.winningCandidate().rule());
            decisionAnalysisAssembler.appendTechnicalDetail(
                    result,
                    "Technical rule match: " + selection.winningCandidate().rule().getName()
            );
        } else if (selection.conflicting()) {
            decisionAnalysisAssembler.appendWarning(result, "Conflit de regles detecte en detail technique");
        }

        return new DecisionDiagnostics(decisionAssembler.toMatchedCandidates(selection));
    }

    public record DecisionDiagnostics(List<MatchedRuleCandidateDto> matchedCandidates) {
        public static DecisionDiagnostics empty() {
            return new DecisionDiagnostics(List.of());
        }
    }
}
