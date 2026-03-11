package com.brilarisoft.lamuertapokerintelligence.dto.rule;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import java.time.Instant;
import java.util.UUID;

public record DecisionRuleSummaryDto(
        UUID id,
        UUID profileId,
        String name,
        String description,
        GameType gameType,
        Street street,
        ScenarioType scenarioType,
        Position heroPosition,
        Position villainPosition,
        boolean enabled,
        boolean stopOnMatch,
        Integer priority,
        int conditionCount,
        int actionCount,
        Instant updatedAt
) {
}
