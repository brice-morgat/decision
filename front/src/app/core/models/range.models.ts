import { ActionType, GameType, PlayerPosition, ScenarioType, Street, StrategyLegend } from './referentials';

export interface HeroRangeContext {
  profileId: string;
  gameType: GameType;
  street: Street;
  heroPosition: PlayerPosition;
  scenarioType: ScenarioType;
  subScenarioCode?: string | null;
}

export interface HeroRangeCellDto {
  handCode: string;
  enabled: boolean;
  legendCode: StrategyLegend | null;
  colorCode?: string | null;
  note?: string | null;
}

export interface HeroRangeSummary {
  id: string;
  profileId: string;
  name: string;
  description: string;
  gameType: GameType;
  street: Street;
  heroPosition: PlayerPosition;
  scenarioType: ScenarioType;
  subScenarioCode?: string | null;
  enabled: boolean;
  priority: number;
  updatedAt: string;
}

export interface HeroRangeDetail extends HeroRangeSummary {
  notes?: string | null;
  cells: HeroRangeCellDto[];
  createdAt: string;
}

export interface HeroRangeUpsertPayload extends HeroRangeContext {
  name: string;
  description: string;
  enabled: boolean;
  priority: number;
  notes?: string | null;
}

export interface HeroRangeCellsUpdatePayload {
  cells: HeroRangeCellDto[];
}

export interface VillainRangeContext {
  profileId: string;
  gameType: GameType;
  street: Street;
  villainPosition: PlayerPosition;
  heroPosition?: PlayerPosition | null;
  scenarioType: ScenarioType;
  triggerActionCode?: ActionType | null;
  lineSignature?: string | null;
}

export interface VillainRangeCellDto {
  handCode: string;
  enabled: boolean;
  weight?: number | null;
  tagCode?: string | null;
  note?: string | null;
}

export interface VillainRangeSummary {
  id: string;
  profileId: string;
  name: string;
  description: string;
  gameType: GameType;
  street: Street;
  villainPosition: PlayerPosition;
  heroPosition?: PlayerPosition | null;
  scenarioType: ScenarioType;
  triggerActionCode?: ActionType | null;
  lineSignature?: string | null;
  enabled: boolean;
  priority: number;
  updatedAt: string;
}

export interface VillainRangeDetail extends VillainRangeSummary {
  notes?: string | null;
  cells: VillainRangeCellDto[];
  createdAt: string;
}

export interface VillainRangeUpsertPayload extends VillainRangeContext {
  name: string;
  description: string;
  enabled: boolean;
  priority: number;
  notes?: string | null;
}

export interface VillainRangeCellsUpdatePayload {
  cells: VillainRangeCellDto[];
}

export interface PokerHandMatrixCell {
  handCode: string;
  enabled: boolean;
  accentColor?: string | null;
  badge?: string | null;
  note?: string | null;
  weight?: number | null;
}
