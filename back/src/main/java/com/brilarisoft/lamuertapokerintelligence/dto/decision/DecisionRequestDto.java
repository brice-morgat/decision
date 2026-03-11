package com.brilarisoft.lamuertapokerintelligence.dto.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DecisionRequestDto(
        @NotNull UUID strategyProfileId,
        @NotNull Position heroPosition,
        @NotNull Position villainPosition,
        @NotNull Street street,
        @NotNull ScenarioType scenarioType,
        @NotNull @DecimalMin("0.0") BigDecimal effectiveStackInBigBlinds,
        @NotNull @DecimalMin("0.0") BigDecimal potSizeInBigBlinds,
        @NotEmpty List<String> heroCards,
        List<String> boardCards,
        @Valid List<ActionEventDto> actionEvents
) {
}
