package com.brilarisoft.lamuertapokerintelligence.dto.rule;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.RuleOperator;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.ConditionValueType;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.RuleConditionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RuleConditionDto(
        @NotNull RuleConditionType conditionType,
        @NotNull RuleOperator operator,
        @NotNull ConditionValueType valueType,
        @NotBlank String expectedValue,
        String secondaryValue,
        @NotNull Integer conditionOrder
) {
}
