import { Pipe, PipeTransform } from '@angular/core';

const LABEL_MAP: Record<string, string> = {
  UTG: 'UTG',
  HJ: 'Hijack',
  CO: 'Cutoff',
  BTN: 'Button',
  SB: 'Small blind',
  BB: 'Big blind',
  CASH: 'Cash game',
  TOURNAMENT: 'Tournoi',
  PREFLOP: 'Preflop',
  FLOP: 'Flop',
  TURN: 'Turn',
  RIVER: 'River',
  OPEN_FIRST_IN: 'Open first in',
  FACING_OPEN: 'Face a un open',
  FACING_THREE_BET: 'Face a un 3-bet',
  BB_DEFENSE: 'Defense de BB',
  FLOP_CBET_SPOT: 'Spot de c-bet flop',
  FACING_SECOND_BARREL: 'Face a un second barrel',
  CHECK_TO_HERO: 'Check vers hero',
  VILLAIN_RAISED_HERO_BET: 'Vilain raise le bet de hero',
  OPEN: 'Open',
  CALL: 'Call',
  CALL_ONLY: 'Call only',
  THREE_BET: '3-bet',
  FOUR_BET: '4-bet',
  SHOVE: 'All-in',
  DEFEND: 'Defense',
  ISO_RAISE: 'Iso raise',
  FOLD: 'Fold',
  CHECK: 'Check',
  CHECK_CALL: 'Check / Call',
  CHECK_FOLD: 'Check / Fold',
  CHECK_RAISE: 'Check / Raise',
  BET_25: 'Bet 25%',
  BET_50: 'Bet 50%',
  BET_75: 'Bet 75%',
  RAISE: 'Raise',
  LIMP: 'Limp',
  BET: 'Bet',
  HERO: 'Hero',
  VILLAIN: 'Vilain',
  HERO_WAS_PREFLOP_AGGRESSOR: 'Hero was preflop aggressor',
  VILLAIN_WAS_PREFLOP_AGGRESSOR: 'Vilain was preflop aggressor',
  VILLAIN_CHECKED_TO_HERO: 'Vilain checked to hero',
  HERO_BET_CURRENT_STREET: 'Hero bet on current street',
  HERO_FACING_SECOND_BARREL: 'Hero facing second barrel',
  HERO_FACING_THIRD_BARREL: 'Hero facing third barrel',
  CURRENT_STREET_BET_COUNT: 'Bet count on street',
  CURRENT_STREET_RAISE_COUNT: 'Raise count on street',
  LINE_SIGNATURE: 'Line signature',
  HERO_STRATEGY_LEGEND: 'Hero range label',
  FACING_ACTION_TYPE: 'Facing action',
  CURRENT_ACTOR: 'Current actor',
  POT_SIZE_BB: 'Pot size (bb)',
  EFFECTIVE_STACK_BB: 'Effective stack (bb)',
  HERO_POSITION: 'Hero position',
  VILLAIN_POSITION: 'Vilain position',
  SCENARIO_TYPE: 'Scenario',
  STREET: 'Street',
  HERO_HAND_CODE: 'Hero hand code',
  BOOLEAN: 'Boolean',
  NUMBER: 'Numeric',
  ENUM: 'Enum',
  STRING: 'Text',
  LIST: 'List',
  EQUALS: 'Equals',
  GREATER_THAN: 'Greater than',
  GREATER_OR_EQUAL: 'Greater or equal',
  LESS_THAN: 'Less than',
  LESS_OR_EQUAL: 'Less or equal',
  BETWEEN: 'Between',
  IN: 'In list',
  CONTAINS: 'Contains',
  POT_PERCENT: 'Pot %',
  ABSOLUTE: 'Absolute',
  MULTIPLIER: 'Multiplier',
  CATEGORY: 'Category',
  ALL_IN: 'All-in',
  BB_UNIT: 'Big blind'
};

@Pipe({
  name: 'readableCode'
})
export class ReadableCodePipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) {
      return '';
    }

    return LABEL_MAP[value] ?? this.humanize(value);
  }

  private humanize(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map((token) => token.charAt(0).toUpperCase() + token.slice(1))
      .join(' ');
  }
}
