package com.brilarisoft.lamuertapokerintelligence.service.range;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.range.PokerHandCodeCatalog;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeCell;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Builds a transient villain range from a target preflop coverage when no saved villain range is selected.
 */
@Service
public class SyntheticVillainRangeFactory {

    private static final double DEFAULT_RANGE_PERCENT = 80.0d;

    public VillainRangeSet create(DecisionContext context, Double requestedPercent) {
        double normalizedPercent = normalizePercent(requestedPercent);
        int enabledHandCount = Math.max(1, (int) Math.round((normalizedPercent / 100.0d) * PokerHandCodeCatalog.all().size()));

        VillainRangeSet rangeSet = new VillainRangeSet();
        rangeSet.setStrategyProfile(context.getStrategyProfile());
        rangeSet.setName(String.format(java.util.Locale.ROOT, "Range vilain synthetique %.0f%%", normalizedPercent));
        rangeSet.setDescription("Range vilain synthetique derivee d'un pourcentage de mains jouees.");
        rangeSet.setGameType(context.getGameType());
        rangeSet.setStreet(context.getStreet());
        rangeSet.setVillainPosition(context.getVillainPosition());
        rangeSet.setHeroPosition(context.getHeroPosition());
        rangeSet.setScenarioType(null);
        rangeSet.setTriggerActionType(null);
        rangeSet.setLineSignature(null);
        rangeSet.setEnabled(true);
        rangeSet.setPriority(0);
        rangeSet.setNotes("Synthetique");

        List<String> orderedCodes = PokerHandCodeCatalog.all().stream()
                .sorted(Comparator.comparingInt(this::score).reversed().thenComparing(String::compareTo))
                .toList();

        List<VillainRangeCell> cells = orderedCodes.stream()
                .limit(enabledHandCount)
                .map(handCode -> createCell(rangeSet, handCode))
                .toList();

        rangeSet.setCells(cells);
        return rangeSet;
    }

    public double defaultPercent() {
        return DEFAULT_RANGE_PERCENT;
    }

    private double normalizePercent(Double requestedPercent) {
        if (requestedPercent == null) {
            return DEFAULT_RANGE_PERCENT;
        }
        return Math.max(1.0d, Math.min(100.0d, requestedPercent));
    }

    private VillainRangeCell createCell(VillainRangeSet rangeSet, String handCode) {
        VillainRangeCell cell = new VillainRangeCell();
        cell.setVillainRangeSet(rangeSet);
        cell.setHandCode(handCode);
        cell.setEnabled(true);
        cell.setWeightPercent(100);
        cell.setTagCode("SYNTHETIC");
        cell.setNote("Genere depuis le slider de couverture.");
        return cell;
    }

    private int score(String handCode) {
        String normalized = PokerHandCodeCatalog.normalize(handCode);
        int first = rankValue(normalized.charAt(0));
        int second = rankValue(normalized.charAt(1));

        if (normalized.length() == 2) {
            return 10_000 + (first * 120);
        }

        boolean suited = normalized.endsWith("S");
        int high = Math.max(first, second);
        int low = Math.min(first, second);
        int gap = Math.max(0, high - low - 1);
        int broadwayCount = (first >= 8 ? 1 : 0) + (second >= 8 ? 1 : 0);
        int connectorBonus = gap == 0 ? 28 : gap == 1 ? 18 : gap == 2 ? 10 : 0;
        int suitedBonus = suited ? 22 : 0;
        int aceBonus = high == 12 ? 24 : 0;
        return (high * 85)
                + (low * 52)
                + suitedBonus
                + connectorBonus
                + (broadwayCount * 14)
                + aceBonus
                - (gap * 11);
    }

    private int rankValue(char rank) {
        return switch (rank) {
            case 'A' -> 12;
            case 'K' -> 11;
            case 'Q' -> 10;
            case 'J' -> 9;
            case 'T' -> 8;
            case '9' -> 7;
            case '8' -> 6;
            case '7' -> 5;
            case '6' -> 4;
            case '5' -> 3;
            case '4' -> 2;
            case '3' -> 1;
            default -> 0;
        };
    }
}
