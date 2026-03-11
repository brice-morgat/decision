export enum PlayerPosition {
  UTG = 'UTG',
  HJ = 'HJ',
  CO = 'CO',
  BTN = 'BTN',
  SB = 'SB',
  BB = 'BB'
}

export enum ActorType {
  HERO = 'HERO',
  VILLAIN = 'VILLAIN'
}

export enum ActionType {
  FOLD = 'FOLD',
  CHECK = 'CHECK',
  CALL = 'CALL',
  BET = 'BET',
  RAISE = 'RAISE',
  OPEN = 'OPEN',
  THREE_BET = 'THREE_BET',
  FOUR_BET = 'FOUR_BET',
  SHOVE = 'SHOVE',
  LIMP = 'LIMP'
}

export enum SizingType {
  BB = 'BB',
  POT_PERCENT = 'POT_PERCENT',
  ABSOLUTE = 'ABSOLUTE',
  MULTIPLIER = 'MULTIPLIER',
  CATEGORY = 'CATEGORY',
  ALL_IN = 'ALL_IN'
}

export enum DecisionStatus {
  SUCCESS = 'SUCCESS',
  NO_MATCH = 'NO_MATCH',
  INCOMPLETE_CONFIGURATION = 'INCOMPLETE_CONFIGURATION',
  CONFLICTING_RULES = 'CONFLICTING_RULES',
  INVALID_INPUT = 'INVALID_INPUT'
}

export enum GameType {
  CASH = 'CASH',
  TOURNAMENT = 'TOURNAMENT'
}

export enum ScenarioType {
  OPEN_FIRST_IN = 'OPEN_FIRST_IN',
  FACING_OPEN = 'FACING_OPEN',
  FACING_THREE_BET = 'FACING_THREE_BET',
  BB_DEFENSE = 'BB_DEFENSE',
  FLOP_CBET_SPOT = 'FLOP_CBET_SPOT',
  FACING_SECOND_BARREL = 'FACING_SECOND_BARREL',
  CHECK_TO_HERO = 'CHECK_TO_HERO',
  VILLAIN_RAISED_HERO_BET = 'VILLAIN_RAISED_HERO_BET'
}

export enum Street {
  PREFLOP = 'PREFLOP',
  FLOP = 'FLOP',
  TURN = 'TURN',
  RIVER = 'RIVER'
}

export enum StrategyLegend {
  OPEN = 'OPEN',
  CALL = 'CALL',
  CALL_ONLY = 'CALL_ONLY',
  THREE_BET = 'THREE_BET',
  FOUR_BET = 'FOUR_BET',
  SHOVE = 'SHOVE',
  DEFEND = 'DEFEND',
  ISO_RAISE = 'ISO_RAISE',
  FOLD = 'FOLD',
  CHECK = 'CHECK',
  CHECK_CALL = 'CHECK_CALL',
  CHECK_FOLD = 'CHECK_FOLD',
  CHECK_RAISE = 'CHECK_RAISE',
  BET_25 = 'BET_25',
  BET_50 = 'BET_50',
  BET_75 = 'BET_75',
  RAISE = 'RAISE'
}

export const GAME_TYPE_OPTIONS = Object.values(GameType);
export const PLAYER_POSITION_OPTIONS = Object.values(PlayerPosition);
export const STREET_OPTIONS = Object.values(Street);
export const SCENARIO_TYPE_OPTIONS = Object.values(ScenarioType);
export const STRATEGY_LEGEND_OPTIONS = Object.values(StrategyLegend);
export const ACTION_TYPE_OPTIONS = Object.values(ActionType);
