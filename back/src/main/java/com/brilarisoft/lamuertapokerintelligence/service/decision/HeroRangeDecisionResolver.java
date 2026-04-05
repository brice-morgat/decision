package com.brilarisoft.lamuertapokerintelligence.service.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeCell;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.StrategyLegend;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class HeroRangeDecisionResolver {

    public Optional<RangeDecisionComputation> resolve(DecisionContext context, HeroRangeSet heroRangeSet) {
        Optional<HeroRangeCell> heroCell = heroRangeSet.getCells().stream()
                .filter(cell -> cell.getHandCode().equalsIgnoreCase(context.getHeroHandCode()))
                .findFirst();
        if (heroCell.isEmpty() || !heroCell.get().isEnabled() || heroCell.get().getStrategyLegend() == null) {
            context.setHeroStrategyLegend(StrategyLegend.FOLD);
            return Optional.empty();
        }

        StrategyLegend heroLegend = heroCell.get().getStrategyLegend();
        context.setHeroStrategyLegend(heroLegend);
        ActionType action = mapLegendToAction(heroLegend);
        if (action == null) {
            return Optional.empty();
        }

        return Optional.of(new RangeDecisionComputation(
                action,
                mapLegendToSizing(heroLegend).orElse(null),
                "Decision derivee de la range hero (" + heroLegend + ")",
                "HERO_RANGE",
                heroLegend,
                null,
                computeHandStrengthScore(context.getHeroHandCode()),
                List.of(
                        "Decision source: HERO_RANGE",
                        "Hero legend: " + heroLegend,
                        "Hero hand strength score: " + computeHandStrengthScore(context.getHeroHandCode())
                )
        ));
    }

    private ActionType mapLegendToAction(StrategyLegend legend) {
        return switch (legend) {
            case OPEN -> ActionType.OPEN;
            case CALL, CALL_ONLY, DEFEND, CHECK_CALL -> ActionType.CALL;
            case THREE_BET -> ActionType.THREE_BET;
            case FOUR_BET -> ActionType.FOUR_BET;
            case SHOVE -> ActionType.SHOVE;
            case FOLD, CHECK_FOLD -> ActionType.FOLD;
            case CHECK -> ActionType.CHECK;
            case CHECK_RAISE, ISO_RAISE, RAISE -> ActionType.RAISE;
            case BET_25, BET_50, BET_75 -> ActionType.BET;
        };
    }

    private Optional<BigDecimal> mapLegendToSizing(StrategyLegend legend) {
        return switch (legend) {
            case BET_25 -> Optional.of(BigDecimal.valueOf(25));
            case BET_50 -> Optional.of(BigDecimal.valueOf(50));
            case BET_75 -> Optional.of(BigDecimal.valueOf(75));
            default -> Optional.empty();
        };
    }

    private int computeHandStrengthScore(String heroHandCode) {
        String normalized = heroHandCode == null ? "" : heroHandCode.trim().toUpperCase();
        if (normalized.length() < 2) {
            return 0;
        }
        java.util.Map<Character, Integer> rankValues = java.util.Map.ofEntries(
                java.util.Map.entry('A', 13),
                java.util.Map.entry('K', 12),
                java.util.Map.entry('Q', 11),
                java.util.Map.entry('J', 10),
                java.util.Map.entry('T', 9),
                java.util.Map.entry('9', 8),
                java.util.Map.entry('8', 7),
                java.util.Map.entry('7', 6),
                java.util.Map.entry('6', 5),
                java.util.Map.entry('5', 4),
                java.util.Map.entry('4', 3),
                java.util.Map.entry('3', 2),
                java.util.Map.entry('2', 1)
        );
        int first = rankValues.getOrDefault(normalized.charAt(0), 0);
        int second = rankValues.getOrDefault(normalized.charAt(1), 0);
        if (normalized.length() == 2) {
            return Math.min(100, first * 6 + 20);
        }
        int suitedBonus = normalized.endsWith("S") ? 12 : 0;
        int connectorBonus = Math.abs(first - second) == 1 ? 8 : 0;
        return Math.min(100, first * 3 + second * 2 + suitedBonus + connectorBonus);
    }
}
