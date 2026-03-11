package com.brilarisoft.lamuertapokerintelligence.dto.rule;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.StrategyLegend;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record DecisionRuleUpsertDto(
        @NotNull UUID profileId,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description,
        @NotNull GameType gameType,
        @NotNull Street street,
        @NotNull ScenarioType scenarioType,
        Position heroPosition,
        Position villainPosition,
        @Size(max = 16) String heroHandCode,
        StrategyLegend heroStrategyLegend,
        UUID heroRangeSetId,
        UUID villainRangeSetId,
        ActionType facingActionType,
        @Size(max = 128) String lineSignature,
        @Size(max = 128) String sizingConditionCode,
        boolean enabled,
        boolean stopOnMatch,
        @NotNull Integer priority,
        @Size(max = 2000) String note,
        @Valid List<RuleConditionDto> conditions,
        @NotEmpty @Valid List<RuleActionDto> actions
) {
}
