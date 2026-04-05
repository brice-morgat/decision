package com.brilarisoft.lamuertapokerintelligence.dto.range;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record HeroRangeUpsertDto(
        @NotNull UUID profileId,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description,
        @NotNull GameType gameType,
        @NotNull Street street,
        @NotNull Position heroPosition,
        ScenarioType scenarioType,
        @Size(max = 64) String subScenarioCode,
        boolean enabled,
        @NotNull Integer priority,
        @Size(max = 2000) String notes
) {
}
