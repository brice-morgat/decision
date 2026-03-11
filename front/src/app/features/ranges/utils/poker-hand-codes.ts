export const HAND_RANKS = ['A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2'] as const;

export type HandRank = (typeof HAND_RANKS)[number];

export function buildHandMatrixCodes(): string[][] {
  return HAND_RANKS.map((rowRank, rowIndex) =>
    HAND_RANKS.map((columnRank, columnIndex) => buildHandCode(rowRank, columnRank, rowIndex, columnIndex))
  );
}

export function buildHandCode(firstRank: HandRank, secondRank: HandRank, rowIndex: number, columnIndex: number): string {
  if (rowIndex === columnIndex) {
    return `${firstRank}${secondRank}`;
  }

  return rowIndex < columnIndex ? `${firstRank}${secondRank}S` : `${secondRank}${firstRank}O`;
}

export function formatHandCode(handCode: string): string {
  if (handCode.length < 3) {
    return handCode;
  }

  return `${handCode.slice(0, 2)}${handCode.slice(2).toLowerCase()}`;
}
