package com.brilarisoft.lamuertapokerintelligence.dto.decision;

import java.math.BigDecimal;

public record DecisionEquityDto(
        BigDecimal heroEquityPercent,
        BigDecimal villainEquityPercent,
        BigDecimal tiePercent
) {
}
