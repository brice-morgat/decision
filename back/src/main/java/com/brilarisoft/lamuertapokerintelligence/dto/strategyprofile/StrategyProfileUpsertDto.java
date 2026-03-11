package com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StrategyProfileUpsertDto(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description,
        @NotNull GameType gameType,
        boolean active,
        boolean archived,
        @NotBlank @Size(max = 32) String versionLabel
) {
}
