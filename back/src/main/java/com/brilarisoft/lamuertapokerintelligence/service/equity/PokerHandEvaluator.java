package com.brilarisoft.lamuertapokerintelligence.service.equity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PokerHandEvaluator {

    public long evaluateSevenCards(List<PokerCard> cards) {
        if (cards == null || cards.size() != 7) {
            throw new IllegalArgumentException("Seven cards are required");
        }

        long best = Long.MIN_VALUE;
        for (int a = 0; a < cards.size() - 4; a++) {
            for (int b = a + 1; b < cards.size() - 3; b++) {
                for (int c = b + 1; c < cards.size() - 2; c++) {
                    for (int d = c + 1; d < cards.size() - 1; d++) {
                        for (int e = d + 1; e < cards.size(); e++) {
                            long score = evaluateFiveCards(List.of(
                                    cards.get(a),
                                    cards.get(b),
                                    cards.get(c),
                                    cards.get(d),
                                    cards.get(e)
                            ));
                            best = Math.max(best, score);
                        }
                    }
                }
            }
        }
        return best;
    }

    private long evaluateFiveCards(List<PokerCard> cards) {
        List<Integer> ranksDescending = cards.stream()
                .map(PokerCard::rank)
                .sorted(Comparator.reverseOrder())
                .toList();
        Map<Integer, Long> rankCounts = cards.stream()
                .collect(java.util.stream.Collectors.groupingBy(PokerCard::rank, java.util.stream.Collectors.counting()));
        boolean flush = cards.stream().map(PokerCard::suit).distinct().count() == 1;
        Integer straightHigh = straightHigh(ranksDescending);

        if (flush && straightHigh != null) {
            return encode(8, List.of(straightHigh));
        }

        List<Map.Entry<Integer, Long>> groups = new ArrayList<>(rankCounts.entrySet());
        groups.sort(Comparator
                .comparingLong((Map.Entry<Integer, Long> entry) -> entry.getValue()).reversed()
                .thenComparing(Map.Entry<Integer, Long>::getKey, Comparator.reverseOrder()));

        if (groups.get(0).getValue() == 4L) {
            int kicker = groups.stream().filter(entry -> entry.getValue() == 1L).findFirst().orElseThrow().getKey();
            return encode(7, List.of(groups.get(0).getKey(), kicker));
        }

        if (groups.get(0).getValue() == 3L && groups.size() > 1 && groups.get(1).getValue() >= 2L) {
            return encode(6, List.of(groups.get(0).getKey(), groups.get(1).getKey()));
        }

        if (flush) {
            return encode(5, ranksDescending);
        }

        if (straightHigh != null) {
            return encode(4, List.of(straightHigh));
        }

        if (groups.get(0).getValue() == 3L) {
            List<Integer> kickers = groups.stream()
                    .filter(entry -> entry.getValue() == 1L)
                    .map(Map.Entry::getKey)
                    .sorted(Comparator.reverseOrder())
                    .toList();
            return encode(3, List.of(groups.get(0).getKey(), kickers.get(0), kickers.get(1)));
        }

        if (groups.get(0).getValue() == 2L && groups.size() > 1 && groups.get(1).getValue() == 2L) {
            List<Integer> pairs = groups.stream()
                    .filter(entry -> entry.getValue() == 2L)
                    .map(Map.Entry::getKey)
                    .sorted(Comparator.reverseOrder())
                    .toList();
            int kicker = groups.stream().filter(entry -> entry.getValue() == 1L).findFirst().orElseThrow().getKey();
            return encode(2, List.of(pairs.get(0), pairs.get(1), kicker));
        }

        if (groups.get(0).getValue() == 2L) {
            List<Integer> kickers = groups.stream()
                    .filter(entry -> entry.getValue() == 1L)
                    .map(Map.Entry::getKey)
                    .sorted(Comparator.reverseOrder())
                    .toList();
            return encode(1, List.of(groups.get(0).getKey(), kickers.get(0), kickers.get(1), kickers.get(2)));
        }

        return encode(0, ranksDescending);
    }

    private Integer straightHigh(List<Integer> descendingRanks) {
        List<Integer> uniqueRanks = descendingRanks.stream().distinct().sorted(Comparator.reverseOrder()).toList();
        if (uniqueRanks.contains(14)) {
            List<Integer> wheel = new ArrayList<>(uniqueRanks);
            wheel.add(1);
            uniqueRanks = wheel;
        }

        int consecutive = 1;
        for (int index = 0; index < uniqueRanks.size() - 1; index++) {
            if (uniqueRanks.get(index) - 1 == uniqueRanks.get(index + 1)) {
                consecutive++;
                if (consecutive == 5) {
                    return uniqueRanks.get(index - 3);
                }
            } else {
                consecutive = 1;
            }
        }
        return null;
    }

    private long encode(int category, List<Integer> kickers) {
        long value = category;
        for (Integer kicker : kickers) {
            value = (value * 15) + kicker;
        }
        return value;
    }
}
