package com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DuplicateStrategyProfileDto(
        @NotBlank @Size(max = 120) String name,
        boolean activateCopy
) {
}
