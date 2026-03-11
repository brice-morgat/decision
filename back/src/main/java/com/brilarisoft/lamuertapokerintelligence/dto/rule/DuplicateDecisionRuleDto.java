package com.brilarisoft.lamuertapokerintelligence.dto.rule;

import jakarta.validation.constraints.Size;

public record DuplicateDecisionRuleDto(
        @Size(max = 120) String name
) {
}
