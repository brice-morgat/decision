package com.brilarisoft.lamuertapokerintelligence.dto.decision;

import java.util.UUID;

public record MatchedRuleCandidateDto(
        UUID ruleId,
        String ruleName,
        Integer priority,
        int matchedConditionCount
) {
}
