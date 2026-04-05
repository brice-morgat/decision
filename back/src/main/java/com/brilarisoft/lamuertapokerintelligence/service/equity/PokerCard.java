package com.brilarisoft.lamuertapokerintelligence.service.equity;

record PokerCard(int rank, char suit, String code) {

    static PokerCard parse(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new IllegalArgumentException("Card code cannot be blank");
        }
        String normalized = rawCode.trim().toUpperCase();
        if (normalized.length() != 2) {
            throw new IllegalArgumentException("Invalid card code: " + rawCode);
        }
        char rankChar = normalized.charAt(0);
        char suitChar = Character.toLowerCase(normalized.charAt(1));
        int rank = switch (rankChar) {
            case 'A' -> 14;
            case 'K' -> 13;
            case 'Q' -> 12;
            case 'J' -> 11;
            case 'T' -> 10;
            case '9' -> 9;
            case '8' -> 8;
            case '7' -> 7;
            case '6' -> 6;
            case '5' -> 5;
            case '4' -> 4;
            case '3' -> 3;
            case '2' -> 2;
            default -> throw new IllegalArgumentException("Invalid card rank: " + rawCode);
        };
        if ("shdc".indexOf(suitChar) < 0) {
            throw new IllegalArgumentException("Invalid card suit: " + rawCode);
        }
        return new PokerCard(rank, suitChar, String.valueOf(rankChar) + suitChar);
    }
}
