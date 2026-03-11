package com.brilarisoft.lamuertapokerintelligence.dto.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.PlayerRole;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.SizingType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ActionEventDto(
        @NotNull Integer orderIndex,
        @NotNull PlayerRole actorType,
        Position actorPosition,
        @NotNull Street street,
        @NotNull ActionType actionCode,
        SizingType sizingType,
        BigDecimal sizingValue,
        BigDecimal potSizeBefore,
        BigDecimal potSizeAfter,
        BigDecimal stackBefore,
        BigDecimal stackAfter,
        String note
) {
}
