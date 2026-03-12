package com.brilarisoft.lamuertapokerintelligence.dto.history;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.DecisionStatus;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ReviewIssue;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record HandReviewDetailDto(
        UUID id,
        UUID strategyProfileId,
        String strategyProfileName,
        String heroHandCode,
        String boardCards,
        DecisionStatus decisionStatus,
        ActionType recommendedAction,
        ActionType actualAction,
        BigDecimal netResultInBigBlinds,
        ReviewIssue issue,
        String engineExplanation,
        String situationSummary,
        String reviewNote,
        Instant createdAt,
        Instant updatedAt
) {
}
