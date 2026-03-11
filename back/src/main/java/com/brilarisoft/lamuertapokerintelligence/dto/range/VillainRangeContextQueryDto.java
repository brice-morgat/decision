package com.brilarisoft.lamuertapokerintelligence.dto.range;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import java.util.UUID;

public record VillainRangeContextQueryDto(
        UUID profileId,
        GameType gameType,
        Street street,
        Position villainPosition,
        Position heroPosition,
        ScenarioType scenarioType,
        ActionType triggerActionCode,
        String lineSignature
) {
}
