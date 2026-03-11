package com.brilarisoft.lamuertapokerintelligence.dto.rule;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.StrategyLegend;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DecisionRuleDetailDto(
        UUID id,
        UUID profileId,
        String name,
        String description,
        GameType gameType,
        Street street,
        ScenarioType scenarioType,
        Position heroPosition,
        Position villainPosition,
        String heroHandCode,
        StrategyLegend heroStrategyLegend,
        UUID heroRangeSetId,
        UUID villainRangeSetId,
        ActionType facingActionType,
        String lineSignature,
        String sizingConditionCode,
        boolean enabled,
        boolean stopOnMatch,
        Integer priority,
        String note,
        List<RuleConditionDto> conditions,
        List<RuleActionDto> actions,
        Instant createdAt,
        Instant updatedAt
) {
}
