import { ActionType, DecisionStatus } from './referentials';

export type ReviewIssue = 'WIN' | 'LOSS' | 'BREAKEVEN';

export interface ReviewSummary {
  id: string;
  strategyProfileId: string;
  strategyProfileName: string;
  heroHandCode: string | null;
  boardCards: string | null;
  decisionStatus: DecisionStatus | null;
  recommendedAction: ActionType | null;
  actualAction: ActionType | null;
  netResultInBigBlinds: number | null;
  issue: ReviewIssue | null;
  engineExplanation: string | null;
  updatedAt: string;
}

export interface ReviewDetail extends ReviewSummary {
  reviewNote: string | null;
  situationSummary: string | null;
  createdAt: string;
}

export interface CreateReviewPayload {
  strategyProfileId: string;
  heroHandCode: string;
  boardCards?: string;
  decisionStatus: DecisionStatus;
  recommendedAction: ActionType;
  actualAction: ActionType;
  netResultInBigBlinds: number;
  issue: ReviewIssue;
  situationSummary?: string;
  engineExplanation?: string;
  reviewNote?: string;
}
