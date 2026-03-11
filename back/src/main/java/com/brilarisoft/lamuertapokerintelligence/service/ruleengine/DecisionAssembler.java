package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionResult;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.DecisionStatus;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.MatchedRuleCandidateDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DecisionAssembler {

    public DecisionResult assemble(DecisionContext context, ResolvedRuleSelection selection) {
        DecisionResult result = new DecisionResult();
        result.setDecisionContext(context);

        if (selection.matchedCandidates().isEmpty()) {
            result.setStatus(DecisionStatus.NO_MATCH);
            result.setExplanation("Aucune regle active n'a matche le contexte reconstruit.");
            result.setTrace(List.of("Candidate rules found: 0"));
            return result;
        }

        if (selection.conflicting()) {
            result.setStatus(DecisionStatus.CONFLICTING_RULES);
            result.setExplanation("Plusieurs regles equivalentes ont matche sans resolution deterministe.");
            result.setWarnings(List.of("Conflit de regles detecte"));
            result.setTrace(flattenTrace(selection.matchedCandidates()));
            return result;
        }

        var winningRule = selection.winningCandidate().rule();
        var primaryAction = winningRule.getActions().stream()
                .sorted(java.util.Comparator.comparing(action -> action.getExecutionOrder()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("A matched rule must contain at least one action"));

        result.setStatus(DecisionStatus.SUCCESS);
        result.setMatchedRule(winningRule);
        result.setRecommendedAction(primaryAction.getActionType());
        result.setRecommendedSizingValue(primaryAction.getSizingValue());
        result.setExplanation("Regle resolue: " + winningRule.getName());
        result.setTrace(flattenTrace(selection.matchedCandidates()));
        return result;
    }

    public List<MatchedRuleCandidateDto> toMatchedCandidates(ResolvedRuleSelection selection) {
        return selection.matchedCandidates().stream()
                .map(candidate -> new MatchedRuleCandidateDto(
                        candidate.rule().getId(),
                        candidate.rule().getName(),
                        candidate.rule().getPriority(),
                        candidate.matchedConditionCount()
                ))
                .toList();
    }

    private List<String> flattenTrace(List<RuleMatchCandidate> matchedCandidates) {
        return matchedCandidates.stream()
                .flatMap(candidate -> candidate.evaluationTrace().stream())
                .toList();
    }
}
