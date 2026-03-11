import {
  ActionType,
  DecisionStatus,
  PlayerPosition,
  ScenarioType,
  SizingType,
  Street
} from './referentials';

export interface ActionEvent {
  orderIndex: number;
  actorType: 'HERO' | 'VILLAIN';
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
  heroPosition: PlayerPosition;
  villainPosition: PlayerPosition;
  street: Street;
  scenarioType: ScenarioType;
  effectiveStackInBigBlinds: number;
  potSizeInBigBlinds: number;
  heroCards: string[];
  boardCards?: string[];
  actionEvents?: ActionEvent[];
}

export interface MatchedRuleCandidate {
  ruleId: string;
  ruleName: string;
  priority: number;
  matchedConditionCount: number;
  structuralSpecificity: number;
  chosen: boolean;
}

export interface DecisionResult {
  status: DecisionStatus;
  recommendedAction?: ActionType | null;
  recommendedSizingValue?: number | null;
  matchedRuleId?: string | null;
  matchedRuleName?: string | null;
  explanation: string;
  trace: string[];
  warnings: string[];
  matchedCandidates: MatchedRuleCandidate[];
}
