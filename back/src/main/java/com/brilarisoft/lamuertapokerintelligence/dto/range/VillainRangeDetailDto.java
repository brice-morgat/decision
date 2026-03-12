package com.brilarisoft.lamuertapokerintelligence.dto.range;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VillainRangeDetailDto(
        UUID id,
        UUID profileId,
        String name,
        String description,
        GameType gameType,
        Street street,
        Position villainPosition,
        Position heroPosition,
        ScenarioType scenarioType,
        ActionType triggerActionCode,
        String lineSignature,
        boolean enabled,
        Integer priority,
        String notes,
        List<VillainRangeScopeDto> scopes,
        List<VillainRangeCellDto> cells,
        Instant createdAt,
        Instant updatedAt
) {
}
