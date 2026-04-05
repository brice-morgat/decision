package com.brilarisoft.lamuertapokerintelligence.dto.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DecisionRequestDto(
        @NotNull UUID strategyProfileId,
        @NotNull UUID heroRangeSetId,
        UUID villainRangeSetId,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal villainRangePercent,
        GameType gameType,
        Position heroPosition,
        Position villainPosition,
        Street street,
        ScenarioType scenarioType,
        @DecimalMin("0.0") BigDecimal effectiveStackInBigBlinds,
        @DecimalMin("0.0") BigDecimal potSizeInBigBlinds,
        @NotBlank String heroHandCode,
        List<String> heroCards,
        List<String> boardCards,
        @Valid List<ActionEventDto> actionEvents
) {
}
