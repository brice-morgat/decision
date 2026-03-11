import { ActionType, DecisionStatus } from './referentials';

export interface ReviewSummary {
  id: string;
  label: string;
  createdAt: string;
  status: DecisionStatus;
}

export interface ReviewDetail extends ReviewSummary {
  expectedAction: ActionType;
  actualAction: ActionType;
  notes?: string;
}