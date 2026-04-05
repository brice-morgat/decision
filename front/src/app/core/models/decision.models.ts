import {
  ActionType,
  ActorType,
  GameType,
  DecisionStatus,
  PlayerPosition,
  ScenarioType,
  SizingType,
  Street
} from './referentials';

export interface ActionEvent {
  orderIndex: number;
  actorType: ActorType;
  actorPosition?: PlayerPosition | null;
  street: Street;
  actionCode: ActionType;
  sizingType?: SizingType | null;
  sizingValue?: number | null;
  potSizeBefore?: number | null;
  potSizeAfter?: number | null;
  stackBefore?: number | null;
  stackAfter?: number | null;
  note?: string | null;
}

export interface DecisionRequest {
  strategyProfileId: string;
  heroRangeSetId: string;
  villainRangeSetId?: string | null;
  villainRangePercent?: number | null;
  gameType?: GameType | null;
  heroPosition?: PlayerPosition | null;
  villainPosition?: PlayerPosition | null;
  street?: Street | null;
  scenarioType?: ScenarioType | null;
  effectiveStackInBigBlinds?: number | null;
  potSizeInBigBlinds?: number | null;
  heroHandCode: string;
  heroCards?: string[];
  boardCards?: string[];
  actionEvents?: ActionEvent[];
}

export interface MatchedRuleCandidate {
  ruleId: string;
  ruleName: string;
  priority: number;
  matchedConditionCount: number;
}

export interface DecisionAnalysis {
  decisionSource?: string | null;
  heroHandCode?: string | null;
  heroLegend?: string | null;
  villainCoveragePercent?: number | null;
  heroHandStrengthScore?: number | null;
  insights?: string[] | null;
}

export interface DecisionEquity {
  heroEquityPercent?: number | null;
  villainEquityPercent?: number | null;
  tiePercent?: number | null;
}

export interface DecisionResult {
  status: DecisionStatus;
  recommendedAction?: ActionType | null;
  recommendedSizingValue?: number | null;
  matchedRuleId?: string | null;
  matchedRuleName?: string | null;
  explanation: string;
  analysis?: DecisionAnalysis | null;
  equity?: DecisionEquity | null;
  trace?: string[] | null;
  warnings?: string[] | null;
  matchedCandidates?: MatchedRuleCandidate[] | null;
}
