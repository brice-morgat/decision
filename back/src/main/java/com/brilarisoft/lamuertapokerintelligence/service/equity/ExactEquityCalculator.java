package com.brilarisoft.lamuertapokerintelligence.service.equity;

import com.brilarisoft.lamuertapokerintelligence.domain.range.PokerHandCodeCatalog;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeCell;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionEquityDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ExactEquityCalculator {

    private static final List<Character> SUITS = List.of('s', 'h', 'd', 'c');

    private final PokerHandEvaluator pokerHandEvaluator;

    public ExactEquityCalculator(PokerHandEvaluator pokerHandEvaluator) {
        this.pokerHandEvaluator = pokerHandEvaluator;
    }

    public DecisionEquityDto calculate(
            String heroHandCode,
            List<String> heroCards,
            boolean exactHeroCards,
            List<String> boardCards,
            VillainRangeSet villainRangeSet
    ) {
        List<PokerCard> parsedBoardCards = parseBoard(boardCards);
        List<List<PokerCard>> heroCombos = exactHeroCards
                ? List.of(parseCards(heroCards, 2))
                : expandHeroHandCode(heroHandCode, parsedBoardCards);
        if (heroCombos.isEmpty()) {
            return null;
        }

        List<WeightedCombo> villainCombos = buildVillainCombos(villainRangeSet, parsedBoardCards);
        if (villainCombos.isEmpty()) {
            return null;
        }

        long heroWins = 0L;
        long villainWins = 0L;
        long ties = 0L;
        long totalWeight = 0L;

        for (List<PokerCard> heroCombo : heroCombos) {
            Set<String> heroDead = toDeadCards(heroCombo, parsedBoardCards);
            for (WeightedCombo villainCombo : villainCombos) {
                if (overlaps(heroDead, villainCombo.cards())) {
                    continue;
                }

                List<PokerCard> availableDeck = buildRemainingDeck(heroCombo, villainCombo.cards(), parsedBoardCards);
                int missingBoardCards = 5 - parsedBoardCards.size();
                OutcomeCounter counter = new OutcomeCounter();
                enumerateBoards(
                        availableDeck,
                        0,
                        missingBoardCards,
                        new ArrayList<>(),
                        parsedBoardCards,
                        heroCombo,
                        villainCombo.cards(),
                        counter
                );

                long comboWeight = villainCombo.weight();
                heroWins += counter.heroWins * comboWeight;
                villainWins += counter.villainWins * comboWeight;
                ties += counter.ties * comboWeight;
                totalWeight += counter.total * comboWeight;
            }
        }

        if (totalWeight == 0L) {
            return null;
        }

        return new DecisionEquityDto(
                toPercent(heroWins, totalWeight),
                toPercent(villainWins, totalWeight),
                toPercent(ties, totalWeight)
        );
    }

    public long estimateOutcomeCount(
            String heroHandCode,
            List<String> heroCards,
            boolean exactHeroCards,
            List<String> boardCards,
            VillainRangeSet villainRangeSet
    ) {
        List<PokerCard> parsedBoardCards = parseBoard(boardCards);
        List<List<PokerCard>> heroCombos = exactHeroCards
                ? List.of(parseCards(heroCards, 2))
                : expandHeroHandCode(heroHandCode, parsedBoardCards);
        if (heroCombos.isEmpty()) {
            return 0L;
        }

        List<WeightedCombo> villainCombos = buildVillainCombos(villainRangeSet, parsedBoardCards);
        if (villainCombos.isEmpty()) {
            return 0L;
        }

        long count = 0L;
        int missingBoardCards = 5 - parsedBoardCards.size();
        for (List<PokerCard> heroCombo : heroCombos) {
            Set<String> heroDead = toDeadCards(heroCombo, parsedBoardCards);
            for (WeightedCombo villainCombo : villainCombos) {
                if (overlaps(heroDead, villainCombo.cards())) {
                    continue;
                }
                int remainingDeckSize = 52 - parsedBoardCards.size() - heroCombo.size() - villainCombo.cards().size();
                count += combinations(remainingDeckSize, missingBoardCards);
            }
        }
        return count;
    }

    private List<PokerCard> parseCards(List<String> cards, int expectedSize) {
        if (cards == null || cards.size() != expectedSize) {
            throw new IllegalArgumentException("Expected " + expectedSize + " cards");
        }
        return cards.stream().map(PokerCard::parse).toList();
    }

    private List<PokerCard> parseBoard(List<String> cards) {
        if (cards == null) {
            return List.of();
        }
        if (cards.size() > 5) {
            throw new IllegalArgumentException("Board cannot contain more than five cards");
        }
        return cards.stream().map(PokerCard::parse).toList();
    }

    private List<List<PokerCard>> expandHeroHandCode(String heroHandCode, List<PokerCard> boardCards) {
        if (heroHandCode == null || !PokerHandCodeCatalog.isValid(heroHandCode)) {
            return List.of();
        }
        Set<String> deadCards = toDeadCards(List.of(), boardCards);
        return expandHandCode(PokerHandCodeCatalog.normalize(heroHandCode)).stream()
                .filter(combo -> combo.stream().map(PokerCard::code).noneMatch(deadCards::contains))
                .toList();
    }

    private List<WeightedCombo> buildVillainCombos(VillainRangeSet villainRangeSet, List<PokerCard> boardCards) {
        Set<String> deadCards = toDeadCards(List.of(), boardCards);
        List<WeightedCombo> combos = new ArrayList<>();
        for (VillainRangeCell cell : villainRangeSet.getCells()) {
            if (cell == null || !cell.isEnabled()) {
                continue;
            }
            String handCode = PokerHandCodeCatalog.normalize(cell.getHandCode());
            if (!PokerHandCodeCatalog.isValid(handCode)) {
                continue;
            }
            long weight = cell.getWeightPercent() == null ? 100L : Math.max(cell.getWeightPercent(), 1);
            expandHandCode(handCode).stream()
                    .filter(combo -> combo.stream().map(PokerCard::code).noneMatch(deadCards::contains))
                    .forEach(combo -> combos.add(new WeightedCombo(combo, weight)));
        }
        return combos;
    }

    private List<List<PokerCard>> expandHandCode(String handCode) {
        char firstRank = handCode.charAt(0);
        char secondRank = handCode.charAt(1);
        if (handCode.length() == 2) {
            List<List<PokerCard>> combos = new ArrayList<>();
            for (int left = 0; left < SUITS.size() - 1; left++) {
                for (int right = left + 1; right < SUITS.size(); right++) {
                    combos.add(List.of(
                            PokerCard.parse("" + firstRank + SUITS.get(left)),
                            PokerCard.parse("" + secondRank + SUITS.get(right))
                    ));
                }
            }
            return combos;
        }

        boolean suited = handCode.endsWith("S");
        List<List<PokerCard>> combos = new ArrayList<>();
        for (char firstSuit : SUITS) {
            for (char secondSuit : SUITS) {
                if (suited && firstSuit != secondSuit) {
                    continue;
                }
                if (!suited && firstSuit == secondSuit) {
                    continue;
                }
                combos.add(List.of(
                        PokerCard.parse("" + firstRank + firstSuit),
                        PokerCard.parse("" + secondRank + secondSuit)
                ));
            }
        }
        return combos;
    }

    private void enumerateBoards(
            List<PokerCard> availableDeck,
            int start,
            int remaining,
            List<PokerCard> current,
            List<PokerCard> boardCards,
            List<PokerCard> heroCards,
            List<PokerCard> villainCards,
            OutcomeCounter counter
    ) {
        if (remaining == 0) {
            List<PokerCard> completeBoard = new ArrayList<>(boardCards);
            completeBoard.addAll(current);
            long heroScore = pokerHandEvaluator.evaluateSevenCards(join(heroCards, completeBoard));
            long villainScore = pokerHandEvaluator.evaluateSevenCards(join(villainCards, completeBoard));
            if (heroScore > villainScore) {
                counter.heroWins++;
            } else if (villainScore > heroScore) {
                counter.villainWins++;
            } else {
                counter.ties++;
            }
            counter.total++;
            return;
        }

        for (int index = start; index <= availableDeck.size() - remaining; index++) {
            current.add(availableDeck.get(index));
            enumerateBoards(availableDeck, index + 1, remaining - 1, current, boardCards, heroCards, villainCards, counter);
            current.remove(current.size() - 1);
        }
    }

    private List<PokerCard> buildRemainingDeck(
            List<PokerCard> heroCards,
            List<PokerCard> villainCards,
            List<PokerCard> boardCards
    ) {
        Set<String> deadCards = toDeadCards(heroCards, boardCards);
        villainCards.stream().map(PokerCard::code).forEach(deadCards::add);
        List<PokerCard> deck = new ArrayList<>(52);
        for (char rank : List.of('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2')) {
            for (char suit : SUITS) {
                PokerCard card = PokerCard.parse("" + rank + suit);
                if (!deadCards.contains(card.code())) {
                    deck.add(card);
                }
            }
        }
        return deck;
    }

    private Set<String> toDeadCards(List<PokerCard> first, List<PokerCard> second) {
        Set<String> deadCards = new HashSet<>();
        first.stream().map(PokerCard::code).forEach(deadCards::add);
        second.stream().map(PokerCard::code).forEach(deadCards::add);
        return deadCards;
    }

    private boolean overlaps(Set<String> deadCards, List<PokerCard> combo) {
        return combo.stream().map(PokerCard::code).anyMatch(deadCards::contains);
    }

    private List<PokerCard> join(List<PokerCard> holeCards, List<PokerCard> boardCards) {
        List<PokerCard> cards = new ArrayList<>(holeCards);
        cards.addAll(boardCards);
        return cards;
    }

    private BigDecimal toPercent(long count, long total) {
        return BigDecimal.valueOf(count * 100.0d / total).setScale(2, RoundingMode.HALF_UP);
    }

    private long combinations(int n, int k) {
        if (k < 0 || k > n) {
            return 0L;
        }
        if (k == 0 || k == n) {
            return 1L;
        }
        int effectiveK = Math.min(k, n - k);
        long result = 1L;
        for (int step = 1; step <= effectiveK; step++) {
            result = (result * (n - effectiveK + step)) / step;
        }
        return result;
    }

    private record WeightedCombo(List<PokerCard> cards, long weight) {
    }

    private static final class OutcomeCounter {
        private long heroWins;
        private long villainWins;
        private long ties;
        private long total;
    }
}
