package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import com.brilarisoft.lamuertapokerintelligence.domain.rule.DecisionRule;
import java.util.List;

public record RuleMatchCandidate(
        DecisionRule rule,
        int matchedConditionCount,
        int structuralSpecificityScore,
        List<String> evaluationTrace
) {
}
