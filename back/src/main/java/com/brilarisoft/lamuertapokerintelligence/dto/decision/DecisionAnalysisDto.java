package com.brilarisoft.lamuertapokerintelligence.dto.decision;

import java.util.List;

public record DecisionAnalysisDto(
        String decisionSource,
        String heroHandCode,
        String heroLegend,
        Double villainCoveragePercent,
        Integer heroHandStrengthScore,
        List<String> insights
) {
}
