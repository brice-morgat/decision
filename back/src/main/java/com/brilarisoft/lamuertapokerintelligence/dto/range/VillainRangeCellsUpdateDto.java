package com.brilarisoft.lamuertapokerintelligence.dto.range;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record VillainRangeCellsUpdateDto(
        @NotNull @Valid List<VillainRangeCellDto> cells
) {
}
