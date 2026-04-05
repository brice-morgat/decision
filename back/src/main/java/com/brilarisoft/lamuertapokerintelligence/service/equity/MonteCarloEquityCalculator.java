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
import java.util.Objects;
import java.util.Set;
import java.util.SplittableRandom;
import org.springframework.stereotype.Component;

@Component
public class MonteCarloEquityCalculator {

    private static final List<Character> SUITS = List.of('s', 'h', 'd', 'c');
    private static final int DEFAULT_SIMULATIONS = 5000;

    private final PokerHandEvaluator pokerHandEvaluator;

    public MonteCarloEquityCalculator(PokerHandEvaluator pokerHandEvaluator) {
        this.pokerHandEvaluator = pokerHandEvaluator;
    }

    public DecisionEquityDto calculate(List<String> heroCards, List<String> boardCards, VillainRangeSet villainRangeSet) {
        return calculate(heroCards, boardCards, villainRangeSet, DEFAULT_SIMULATIONS);
    }

    public DecisionEquityDto calculate(
            String heroHandCode,
            List<String> heroCards,
            boolean exactHeroCards,
            List<String> boardCards,
            VillainRangeSet villainRangeSet
    ) {
        return calculate(heroHandCode, heroCards, exactHeroCards, boardCards, villainRangeSet, DEFAULT_SIMULATIONS);
    }

    public DecisionEquityDto calculate(
            String heroHandCode,
            List<String> heroCards,
            boolean exactHeroCards,
            List<String> boardCards,
            VillainRangeSet villainRangeSet,
            int simulations
    ) {
        List<PokerCard> parsedBoardCards = parseBoard(boardCards);
        List<List<PokerCard>> heroCombos = exactHeroCards
                ? List.of(parseCards(heroCards, 2))
                : expandHeroHandCode(heroHandCode, parsedBoardCards);
        if (heroCombos.isEmpty()) {
            return null;
        }

        SplittableRandom random = new SplittableRandom(Objects.hash(heroHandCode, heroCards, parsedBoardCards.toString(), villainRangeSet.getName()));
        int heroWins = 0;
        int villainWins = 0;
        int ties = 0;
        int completedSimulations = 0;

        for (int iteration = 0; iteration < simulations; iteration++) {
            List<PokerCard> sampledHeroCombo = heroCombos.get(random.nextInt(heroCombos.size()));
            ensureUnique(sampledHeroCombo, parsedBoardCards);
            List<WeightedCombo> villainCombos = buildVillainCombos(villainRangeSet, sampledHeroCombo, parsedBoardCards);
            if (villainCombos.isEmpty()) {
                continue;
            }

            WeightedCombo sampledCombo = sampleVillainCombo(villainCombos, random);
            List<PokerCard> completedBoard = completeBoard(sampledHeroCombo, parsedBoardCards, sampledCombo.cards(), random);

            long heroScore = pokerHandEvaluator.evaluateSevenCards(join(sampledHeroCombo, completedBoard));
            long villainScore = pokerHandEvaluator.evaluateSevenCards(join(sampledCombo.cards(), completedBoard));
            if (heroScore > villainScore) {
                heroWins++;
            } else if (villainScore > heroScore) {
                villainWins++;
            } else {
                ties++;
            }
            completedSimulations++;
        }

        if (completedSimulations == 0) {
            return null;
        }

        return new DecisionEquityDto(
                toPercent(heroWins, completedSimulations),
                toPercent(villainWins, completedSimulations),
                toPercent(ties, completedSimulations)
        );
    }

    public DecisionEquityDto calculate(
            List<String> heroCards,
            List<String> boardCards,
            VillainRangeSet villainRangeSet,
            int simulations
    ) {
        String heroHandCode = PokerHandCodeCatalog.fromCards(heroCards);
        return calculate(heroHandCode, heroCards, true, boardCards, villainRangeSet, simulations);
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
        Set<String> deadCards = new HashSet<>();
        boardCards.stream().map(PokerCard::code).forEach(deadCards::add);
        return expandHandCode(PokerHandCodeCatalog.normalize(heroHandCode)).stream()
                .filter(combo -> combo.stream().map(PokerCard::code).noneMatch(deadCards::contains))
                .toList();
    }

    private void ensureUnique(List<PokerCard> heroCards, List<PokerCard> boardCards) {
        Set<String> usedCards = new HashSet<>();
        heroCards.stream().map(PokerCard::code).forEach(code -> {
            if (!usedCards.add(code)) {
                throw new IllegalArgumentException("Duplicate hero card: " + code);
            }
        });
        boardCards.stream().map(PokerCard::code).forEach(code -> {
            if (!usedCards.add(code)) {
                throw new IllegalArgumentException("Duplicate board card: " + code);
            }
        });
    }

    private List<WeightedCombo> buildVillainCombos(
            VillainRangeSet villainRangeSet,
            List<PokerCard> heroCards,
            List<PokerCard> boardCards
    ) {
        Set<String> deadCards = new HashSet<>();
        heroCards.stream().map(PokerCard::code).forEach(deadCards::add);
        boardCards.stream().map(PokerCard::code).forEach(deadCards::add);

        List<WeightedCombo> combos = new ArrayList<>();
        for (VillainRangeCell cell : villainRangeSet.getCells()) {
            if (cell == null || !cell.isEnabled()) {
                continue;
            }
            String handCode = PokerHandCodeCatalog.normalize(cell.getHandCode());
            if (!PokerHandCodeCatalog.isValid(handCode)) {
                continue;
            }
            int weight = cell.getWeightPercent() == null ? 100 : cell.getWeightPercent();
            expandHandCode(handCode).stream()
                    .filter(combo -> combo.stream().map(PokerCard::code).noneMatch(deadCards::contains))
                    .forEach(combo -> combos.add(new WeightedCombo(combo, Math.max(weight, 1))));
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

        List<List<PokerCard>> combos = new ArrayList<>();
        boolean suited = handCode.endsWith("S");
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

    private WeightedCombo sampleVillainCombo(List<WeightedCombo> combos, SplittableRandom random) {
        int totalWeight = combos.stream().mapToInt(WeightedCombo::weight).sum();
        int cursor = random.nextInt(totalWeight);
        for (WeightedCombo combo : combos) {
            if (cursor < combo.weight()) {
                return combo;
            }
            cursor -= combo.weight();
        }
        return combos.get(combos.size() - 1);
    }

    private List<PokerCard> completeBoard(
            List<PokerCard> heroCards,
            List<PokerCard> boardCards,
            List<PokerCard> villainCards,
            SplittableRandom random
    ) {
        if (boardCards.size() == 5) {
            return boardCards;
        }

        Set<String> deadCards = new HashSet<>();
        heroCards.stream().map(PokerCard::code).forEach(deadCards::add);
        villainCards.stream().map(PokerCard::code).forEach(deadCards::add);
        boardCards.stream().map(PokerCard::code).forEach(deadCards::add);

        List<PokerCard> deck = buildDeck().stream()
                .filter(card -> !deadCards.contains(card.code()))
                .toList();

        List<PokerCard> completedBoard = new ArrayList<>(boardCards);
        while (completedBoard.size() < 5) {
            int index = random.nextInt(deck.size());
            PokerCard nextCard = deck.get(index);
            if (deadCards.add(nextCard.code())) {
                completedBoard.add(nextCard);
            }
        }
        return completedBoard;
    }

    private List<PokerCard> join(List<PokerCard> holeCards, List<PokerCard> boardCards) {
        List<PokerCard> cards = new ArrayList<>(holeCards);
        cards.addAll(boardCards);
        return cards;
    }

    private BigDecimal toPercent(int count, int simulations) {
        return BigDecimal.valueOf(count * 100.0d / simulations).setScale(2, RoundingMode.HALF_UP);
    }

    private List<PokerCard> buildDeck() {
        List<PokerCard> deck = new ArrayList<>(52);
        for (char rank : List.of('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2')) {
            for (char suit : SUITS) {
                deck.add(PokerCard.parse("" + rank + suit));
            }
        }
        return deck;
    }

    private record WeightedCombo(List<PokerCard> cards, int weight) {
    }
}
