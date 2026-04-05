package com.brilarisoft.lamuertapokerintelligence.dto.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.DecisionStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DecisionResponseDto(
        DecisionStatus status,
        ActionType recommendedAction,
        BigDecimal recommendedSizingValue,
        UUID matchedRuleId,
        String matchedRuleName,
        String explanation,
        DecisionAnalysisDto analysis,
        DecisionEquityDto equity,
        List<String> trace,
        List<String> warnings,
        List<MatchedRuleCandidateDto> matchedCandidates
) {
}
