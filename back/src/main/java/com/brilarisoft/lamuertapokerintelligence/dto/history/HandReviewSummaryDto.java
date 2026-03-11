package com.brilarisoft.lamuertapokerintelligence.dto.history;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.DecisionStatus;
import java.time.Instant;
import java.util.UUID;

public record HandReviewSummaryDto(
        UUID id,
        UUID strategyProfileId,
        String strategyProfileName,
        String heroHandCode,
        String boardCards,
        DecisionStatus decisionStatus,
        ActionType recommendedAction,
        String engineExplanation,
        Instant updatedAt
) {
}
