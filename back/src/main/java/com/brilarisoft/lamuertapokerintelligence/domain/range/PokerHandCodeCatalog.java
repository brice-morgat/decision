package com.brilarisoft.lamuertapokerintelligence.domain.range;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Canonical 169-hand range catalog used by both hero and villain ranges.
 */
public final class PokerHandCodeCatalog {

    private static final List<String> RANKS = List.of("A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2");
    private static final Set<String> HAND_CODES = buildHandCodes();

    private PokerHandCodeCatalog() {
    }

    public static boolean isValid(String handCode) {
        return handCode != null && HAND_CODES.contains(normalize(handCode));
    }

    public static String normalize(String handCode) {
        return handCode == null ? null : handCode.trim().toUpperCase();
    }

    public static Set<String> all() {
        return HAND_CODES;
    }

    public static String fromCards(List<String> cards) {
        if (cards == null || cards.size() != 2) {
            return null;
        }
        String first = cards.get(0);
        String second = cards.get(1);
        if (first == null || second == null || first.length() < 2 || second.length() < 2) {
            return null;
        }

        String firstRank = String.valueOf(Character.toUpperCase(first.charAt(0)));
        String secondRank = String.valueOf(Character.toUpperCase(second.charAt(0)));
        char firstSuit = Character.toLowerCase(first.charAt(1));
        char secondSuit = Character.toLowerCase(second.charAt(1));

        int firstIndex = RANKS.indexOf(firstRank);
        int secondIndex = RANKS.indexOf(secondRank);
        if (firstIndex < 0 || secondIndex < 0) {
            return null;
        }
        if (firstIndex == secondIndex) {
            return firstRank + secondRank;
        }
        boolean suited = firstSuit == secondSuit;
        if (firstIndex < secondIndex) {
            return firstRank + secondRank + (suited ? "S" : "O");
        }
        return secondRank + firstRank + (suited ? "S" : "O");
    }

    private static Set<String> buildHandCodes() {
        Set<String> handCodes = new LinkedHashSet<>();
        for (int row = 0; row < RANKS.size(); row++) {
            for (int column = 0; column < RANKS.size(); column++) {
                String firstRank = RANKS.get(row);
                String secondRank = RANKS.get(column);
                if (row == column) {
                    handCodes.add(firstRank + secondRank);
                } else if (row < column) {
                    handCodes.add(firstRank + secondRank + "S");
                } else {
                    handCodes.add(secondRank + firstRank + "O");
                }
            }
        }
        return Set.copyOf(handCodes);
    }
}
