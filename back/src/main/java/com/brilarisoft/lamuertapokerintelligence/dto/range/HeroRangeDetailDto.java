package com.brilarisoft.lamuertapokerintelligence.dto.range;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record HeroRangeDetailDto(
        UUID id,
        UUID profileId,
        String name,
        String description,
        GameType gameType,
        Street street,
        Position heroPosition,
        ScenarioType scenarioType,
        String subScenarioCode,
        boolean enabled,
        Integer priority,
        String notes,
        List<HeroRangeCellDto> cells,
        Instant createdAt,
        Instant updatedAt
) {
}
