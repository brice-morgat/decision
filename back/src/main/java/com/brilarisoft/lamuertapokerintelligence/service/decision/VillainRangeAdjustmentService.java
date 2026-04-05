package com.brilarisoft.lamuertapokerintelligence.service.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VillainRangeAdjustmentService {

    public RangeDecisionComputation adjust(
            DecisionContext context,
            VillainRangeSet villainRangeSet,
            RangeDecisionComputation heroDecision
    ) {
        double villainCoverage = computeVillainCoverage(villainRangeSet);
        int handStrengthScore = heroDecision.heroHandStrengthScore() == null ? 0 : heroDecision.heroHandStrengthScore();
        ActionType adjustedAction = adjustAgainstVillain(heroDecision.action(), villainCoverage, handStrengthScore);
        return new RangeDecisionComputation(
                adjustedAction,
                heroDecision.sizingValue(),
                "Decision derivee de la range hero, ajustee contre la range vilain selectionnee.",
                "HERO_VS_VILLAIN_RANGE",
                heroDecision.heroLegend(),
                villainCoverage,
                handStrengthScore,
                List.of(
                        "Decision source: HERO_VS_VILLAIN_RANGE",
                        "Hero legend: " + heroDecision.heroLegend(),
                        "Villain coverage: " + String.format(java.util.Locale.ROOT, "%.1f", villainCoverage) + "%",
                        "Hero hand strength score: " + handStrengthScore,
                        "Adjusted action: " + adjustedAction
                )
        );
    }

    private double computeVillainCoverage(VillainRangeSet villainRangeSet) {
        if (villainRangeSet.getCells().isEmpty()) {
            return 0;
        }
        double weightedEnabled = villainRangeSet.getCells().stream()
                .filter(cell -> cell.isEnabled())
                .mapToDouble(cell -> (cell.getWeightPercent() == null ? 100 : cell.getWeightPercent()) / 100.0d)
                .sum();
        return Math.min(100.0d, (weightedEnabled / 169.0d) * 100.0d);
    }

    private ActionType adjustAgainstVillain(ActionType baseAction, double villainCoverage, int handStrengthScore) {
        if (baseAction == ActionType.CALL && villainCoverage < 14.0d && handStrengthScore < 55) {
            return ActionType.FOLD;
        }
        if (baseAction == ActionType.CALL && villainCoverage > 42.0d && handStrengthScore > 72) {
            return ActionType.RAISE;
        }
        if ((baseAction == ActionType.OPEN || baseAction == ActionType.RAISE || baseAction == ActionType.THREE_BET)
                && villainCoverage < 12.0d
                && handStrengthScore < 52) {
            return ActionType.FOLD;
        }
        return baseAction;
    }
}
