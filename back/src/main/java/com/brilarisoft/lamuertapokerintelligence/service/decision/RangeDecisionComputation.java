package com.brilarisoft.lamuertapokerintelligence.service.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.StrategyLegend;
import java.math.BigDecimal;
import java.util.List;

public record RangeDecisionComputation(
        ActionType action,
        BigDecimal sizingValue,
        String explanation,
        String decisionSource,
        StrategyLegend heroLegend,
        Double villainCoveragePercent,
        Integer heroHandStrengthScore,
        List<String> insights
) {
}
