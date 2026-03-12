package com.brilarisoft.lamuertapokerintelligence.dto.history;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.DecisionStatus;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ReviewIssue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record HandReviewCreateDto(
        @NotNull UUID strategyProfileId,
        @NotBlank String heroHandCode,
        String boardCards,
        @NotNull DecisionStatus decisionStatus,
        @NotNull ActionType recommendedAction,
        @NotNull ActionType actualAction,
        @NotNull @DecimalMin("-1000.0") BigDecimal netResultInBigBlinds,
        @NotNull ReviewIssue issue,
        String situationSummary,
        String engineExplanation,
        String reviewNote
) {
}
