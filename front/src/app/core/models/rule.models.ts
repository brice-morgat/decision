import {
  ActionType,
  GameType,
  PlayerPosition,
  ScenarioType,
  SizingType,
  Street,
  StrategyLegend
} from './referentials';

export enum RuleConditionType {
  HERO_WAS_PREFLOP_AGGRESSOR = 'HERO_WAS_PREFLOP_AGGRESSOR',
  VILLAIN_WAS_PREFLOP_AGGRESSOR = 'VILLAIN_WAS_PREFLOP_AGGRESSOR',
  VILLAIN_CHECKED_TO_HERO = 'VILLAIN_CHECKED_TO_HERO',
  HERO_BET_CURRENT_STREET = 'HERO_BET_CURRENT_STREET',
  VILLAIN_RAISED_HERO_BET = 'VILLAIN_RAISED_HERO_BET',
  HERO_FACING_SECOND_BARREL = 'HERO_FACING_SECOND_BARREL',
  HERO_FACING_THIRD_BARREL = 'HERO_FACING_THIRD_BARREL',
  CURRENT_STREET_BET_COUNT = 'CURRENT_STREET_BET_COUNT',
  CURRENT_STREET_RAISE_COUNT = 'CURRENT_STREET_RAISE_COUNT',
  LINE_SIGNATURE = 'LINE_SIGNATURE',
  HERO_STRATEGY_LEGEND = 'HERO_STRATEGY_LEGEND',
  FACING_ACTION_TYPE = 'FACING_ACTION_TYPE',
  CURRENT_ACTOR = 'CURRENT_ACTOR',
  POT_SIZE_BB = 'POT_SIZE_BB',
  EFFECTIVE_STACK_BB = 'EFFECTIVE_STACK_BB',
  HERO_POSITION = 'HERO_POSITION',
  VILLAIN_POSITION = 'VILLAIN_POSITION',
  SCENARIO_TYPE = 'SCENARIO_TYPE',
  STREET = 'STREET',
  HERO_HAND_CODE = 'HERO_HAND_CODE'
}

export enum ConditionValueType {
  BOOLEAN = 'BOOLEAN',
  NUMBER = 'NUMBER',
  ENUM = 'ENUM',
  STRING = 'STRING',
  LIST = 'LIST'
}

export enum RuleOperator {
  EQUALS = 'EQUALS',
  GREATER_THAN = 'GREATER_THAN',
  GREATER_OR_EQUAL = 'GREATER_OR_EQUAL',
  LESS_THAN = 'LESS_THAN',
  LESS_OR_EQUAL = 'LESS_OR_EQUAL',
  BETWEEN = 'BETWEEN',
  IN = 'IN',
  CONTAINS = 'CONTAINS'
}

export interface RuleCondition {
  conditionType: RuleConditionType;
  operator: RuleOperator;
  valueType: ConditionValueType;
  expectedValue: string;
  secondaryValue?: string | null;
  conditionOrder: number;
}

export interface RuleAction {
  actionType: ActionType;
  sizingType: SizingType;
  sizingValue?: number | null;
  executionOrder: number;
  messageTemplate?: string | null;
}

export interface DecisionRuleSummary {
  id: string;
  profileId: string;
  name: string;
  description?: string | null;
  gameType: GameType;
  street: Street;
  scenarioType: ScenarioType;
  heroPosition?: PlayerPosition | null;
  villainPosition?: PlayerPosition | null;
  enabled: boolean;
  stopOnMatch: boolean;
  priority: number;
  conditionCount: number;
  actionCount: number;
  updatedAt: string;
}

export interface DecisionRuleDetail extends DecisionRuleSummary {
  heroHandCode?: string | null;
  heroStrategyLegend?: StrategyLegend | null;
  heroRangeSetId?: string | null;
  villainRangeSetId?: string | null;
  facingActionType?: ActionType | null;
  lineSignature?: string | null;
  sizingConditionCode?: string | null;
  note?: string | null;
  conditions: RuleCondition[];
  actions: RuleAction[];
  createdAt: string;
}

export interface DecisionRuleUpsertPayload {
  profileId: string;
  name: string;
  description?: string | null;
  gameType: GameType;
  street: Street;
  scenarioType: ScenarioType;
  heroPosition?: PlayerPosition | null;
  villainPosition?: PlayerPosition | null;
  heroHandCode?: string | null;
  heroStrategyLegend?: StrategyLegend | null;
  heroRangeSetId?: string | null;
  villainRangeSetId?: string | null;
  facingActionType?: ActionType | null;
  lineSignature?: string | null;
  sizingConditionCode?: string | null;
  enabled: boolean;
  stopOnMatch: boolean;
  priority: number;
  note?: string | null;
  conditions: RuleCondition[];
  actions: RuleAction[];
}

export interface DecisionRuleFilters {
  profileId?: string | null;
  gameType?: GameType | null;
  street?: Street | null;
  scenarioType?: ScenarioType | null;
  enabled?: boolean | null;
  heroPosition?: PlayerPosition | null;
  villainPosition?: PlayerPosition | null;
  priority?: number | null;
}

export interface DuplicateDecisionRulePayload {
  name?: string | null;
}

export const RULE_CONDITION_TYPE_OPTIONS = Object.values(RuleConditionType);
export const CONDITION_VALUE_TYPE_OPTIONS = Object.values(ConditionValueType);
export const RULE_OPERATOR_OPTIONS = Object.values(RuleOperator);

export const RULE_OPERATORS_BY_VALUE_TYPE: Record<ConditionValueType, RuleOperator[]> = {
  [ConditionValueType.BOOLEAN]: [RuleOperator.EQUALS],
  [ConditionValueType.NUMBER]: [
    RuleOperator.EQUALS,
    RuleOperator.GREATER_THAN,
    RuleOperator.GREATER_OR_EQUAL,
    RuleOperator.LESS_THAN,
    RuleOperator.LESS_OR_EQUAL,
    RuleOperator.BETWEEN
  ],
  [ConditionValueType.ENUM]: [RuleOperator.EQUALS, RuleOperator.IN, RuleOperator.CONTAINS],
  [ConditionValueType.STRING]: [RuleOperator.EQUALS, RuleOperator.IN, RuleOperator.CONTAINS],
  [ConditionValueType.LIST]: [RuleOperator.IN, RuleOperator.CONTAINS]
};

export const RULE_VALUE_TYPE_BY_CONDITION: Record<RuleConditionType, ConditionValueType> = {
  [RuleConditionType.HERO_WAS_PREFLOP_AGGRESSOR]: ConditionValueType.BOOLEAN,
  [RuleConditionType.VILLAIN_WAS_PREFLOP_AGGRESSOR]: ConditionValueType.BOOLEAN,
  [RuleConditionType.VILLAIN_CHECKED_TO_HERO]: ConditionValueType.BOOLEAN,
  [RuleConditionType.HERO_BET_CURRENT_STREET]: ConditionValueType.BOOLEAN,
  [RuleConditionType.VILLAIN_RAISED_HERO_BET]: ConditionValueType.BOOLEAN,
  [RuleConditionType.HERO_FACING_SECOND_BARREL]: ConditionValueType.BOOLEAN,
  [RuleConditionType.HERO_FACING_THIRD_BARREL]: ConditionValueType.BOOLEAN,
  [RuleConditionType.CURRENT_STREET_BET_COUNT]: ConditionValueType.NUMBER,
  [RuleConditionType.CURRENT_STREET_RAISE_COUNT]: ConditionValueType.NUMBER,
  [RuleConditionType.LINE_SIGNATURE]: ConditionValueType.STRING,
  [RuleConditionType.HERO_STRATEGY_LEGEND]: ConditionValueType.ENUM,
  [RuleConditionType.FACING_ACTION_TYPE]: ConditionValueType.ENUM,
  [RuleConditionType.CURRENT_ACTOR]: ConditionValueType.ENUM,
  [RuleConditionType.POT_SIZE_BB]: ConditionValueType.NUMBER,
  [RuleConditionType.EFFECTIVE_STACK_BB]: ConditionValueType.NUMBER,
  [RuleConditionType.HERO_POSITION]: ConditionValueType.ENUM,
  [RuleConditionType.VILLAIN_POSITION]: ConditionValueType.ENUM,
  [RuleConditionType.SCENARIO_TYPE]: ConditionValueType.ENUM,
  [RuleConditionType.STREET]: ConditionValueType.ENUM,
  [RuleConditionType.HERO_HAND_CODE]: ConditionValueType.STRING
};
