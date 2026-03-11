package com.brilarisoft.lamuertapokerintelligence.dto.rule;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.SizingType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RuleActionDto(
        @NotNull ActionType actionType,
        @NotNull SizingType sizingType,
        BigDecimal sizingValue,
        @NotNull Integer executionOrder,
        @Size(max = 512) String messageTemplate
) {
}
