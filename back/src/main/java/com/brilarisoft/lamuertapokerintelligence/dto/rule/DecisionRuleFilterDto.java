package com.brilarisoft.lamuertapokerintelligence.dto.rule;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import java.util.UUID;

public record DecisionRuleFilterDto(
        UUID profileId,
        GameType gameType,
        Street street,
        ScenarioType scenarioType,
        Boolean enabled,
        Position heroPosition,
        Position villainPosition,
        Integer priority
) {
}
