package com.brilarisoft.lamuertapokerintelligence.dto.range;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

public record VillainRangeUpsertDto(
        @NotNull UUID profileId,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description,
        @NotNull GameType gameType,
        @NotNull Street street,
        @NotNull Position villainPosition,
        Position heroPosition,
        ScenarioType scenarioType,
        ActionType triggerActionCode,
        @Size(max = 128) String lineSignature,
        boolean enabled,
        @NotNull Integer priority,
        @Size(max = 2000) String notes,
        @Valid List<VillainRangeScopeDto> scopes
) {
}
