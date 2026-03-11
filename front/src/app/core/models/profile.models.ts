import { GameType } from './referentials';

export interface StrategicProfileSummary {
  id: string;
  name: string;
  description: string;
  gameType: GameType;
  active: boolean;
  archived: boolean;
  versionLabel: string;
  updatedAt: string;
}

export interface StrategicProfileDetail extends StrategicProfileSummary {
  createdAt: string;
}

export interface StrategyProfileUpsertPayload {
  name: string;
  description: string;
  gameType: GameType;
  active: boolean;
  archived: boolean;
  versionLabel: string;
}

export interface DuplicateStrategyProfilePayload {
  name: string;
  activateCopy: boolean;
}
