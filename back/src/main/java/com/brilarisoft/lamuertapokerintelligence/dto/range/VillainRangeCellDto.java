package com.brilarisoft.lamuertapokerintelligence.dto.range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VillainRangeCellDto(
        @NotBlank @Size(max = 8) String handCode,
        boolean enabled,
        Integer weight,
        @Size(max = 64) String tagCode,
        @Size(max = 1000) String note
) {
}
