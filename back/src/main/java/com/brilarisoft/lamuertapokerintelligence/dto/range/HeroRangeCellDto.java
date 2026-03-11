package com.brilarisoft.lamuertapokerintelligence.dto.range;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.StrategyLegend;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HeroRangeCellDto(
        @NotBlank @Size(max = 8) String handCode,
        boolean enabled,
        StrategyLegend legendCode,
        @Size(max = 16) String colorCode,
        @Size(max = 1000) String note
) {
}
