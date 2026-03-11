package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import java.util.List;

public record ResolvedRuleSelection(
        RuleMatchCandidate winningCandidate,
        List<RuleMatchCandidate> matchedCandidates,
        boolean conflicting
) {
}
