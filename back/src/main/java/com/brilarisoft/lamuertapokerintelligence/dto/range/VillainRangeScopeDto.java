package com.brilarisoft.lamuertapokerintelligence.dto.range;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import jakarta.validation.constraints.Size;

public record VillainRangeScopeDto(
        Position heroPosition,
        Position villainPosition,
        ActionType triggerActionCode,
        @Size(max = 128) String lineSignature,
        Integer scopeWeight
) {
}
