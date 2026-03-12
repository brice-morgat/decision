import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { ActionEvent, DecisionRequest } from '../../../core/models/decision.models';
import { StrategicProfileSummary } from '../../../core/models/profile.models';
import { ProfilesService } from '../../../core/services/profiles.service';
import { RangesService } from '../../../core/services/ranges.service';
import { VillainRangeSummary } from '../../../core/models/range.models';
import { take } from 'rxjs';
import {
  ActionType,
  ActorType,
  GameType,
  PlayerPosition,
  ScenarioType,
  SizingType,
  Street
} from '../../../core/models/referentials';
import { buildHandMatrixCodes, formatHandCode, HAND_RANKS } from '../../ranges/utils/poker-hand-codes';

interface DecisionSpotOption {
  code: string;
  label: string;
  heroPosition: PlayerPosition;
  villainPosition: PlayerPosition;
  scenarioType: ScenarioType;
  street: Street;
  defaultFacingAction: ActionType;
}
type VillainInputMode = 'CUSTOM' | 'PERCENT';

@Component({
  selector: 'app-decision-context-form',
  templateUrl: './decision-context-form.component.html',
  styleUrls: ['./decision-context-form.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DecisionContextFormComponent {
  @Input() submitting = false;
  @Input() errorMessage: string | null = null;

  readonly handRanks = HAND_RANKS;
  readonly handMatrix = buildHandMatrixCodes();
  readonly streets = Object.values(Street);
  readonly actionTypes: ActionType[] = [ActionType.CHECK, ActionType.OPEN, ActionType.CALL, ActionType.BET, ActionType.RAISE, ActionType.THREE_BET, ActionType.SHOVE];
  readonly sizingTypes: SizingType[] = [SizingType.BB, SizingType.POT_PERCENT, SizingType.ALL_IN];
  readonly profiles$ = this.profilesService.list();
  readonly spotOptions: DecisionSpotOption[] = [
    {
      code: 'BTN_OPEN_FIRST_IN',
      label: 'BTN open first in',
      heroPosition: PlayerPosition.BTN,
      villainPosition: PlayerPosition.BB,
      scenarioType: ScenarioType.OPEN_FIRST_IN,
      street: Street.PREFLOP,
      defaultFacingAction: ActionType.CHECK
    },
    {
      code: 'BB_VS_BTN_OPEN',
      label: 'BB vs BTN open',
      heroPosition: PlayerPosition.BB,
      villainPosition: PlayerPosition.BTN,
      scenarioType: ScenarioType.FACING_OPEN,
      street: Street.PREFLOP,
      defaultFacingAction: ActionType.OPEN
    },
    {
      code: 'BB_VS_CO_OPEN',
      label: 'BB vs CO open',
      heroPosition: PlayerPosition.BB,
      villainPosition: PlayerPosition.CO,
      scenarioType: ScenarioType.FACING_OPEN,
      street: Street.PREFLOP,
      defaultFacingAction: ActionType.OPEN
    },
    {
      code: 'SB_VS_BTN_OPEN',
      label: 'SB vs BTN open',
      heroPosition: PlayerPosition.SB,
      villainPosition: PlayerPosition.BTN,
      scenarioType: ScenarioType.FACING_OPEN,
      street: Street.PREFLOP,
      defaultFacingAction: ActionType.OPEN
    },
    {
      code: 'BTN_VS_THREE_BET',
      label: 'BTN vs 3bet',
      heroPosition: PlayerPosition.BTN,
      villainPosition: PlayerPosition.BB,
      scenarioType: ScenarioType.FACING_THREE_BET,
      street: Street.PREFLOP,
      defaultFacingAction: ActionType.THREE_BET
    }
  ];

  @Output() submitDecision = new EventEmitter<DecisionRequest>();

  selectedProfileId: string | null = null;
  selectedVillainRangeSetId: string | null = null;
  useVillainAdvancedInput = false;
  villainInputMode: VillainInputMode = 'CUSTOM';
  villainRangePercent = 35;
  selectedSpotCode = 'BTN_OPEN_FIRST_IN';
  selectedHandCode = 'AKS';
  selectedFacingAction: ActionType = ActionType.OPEN;
  selectedSizingType: SizingType = SizingType.BB;
  selectedSizingValue = 2.5;
  showAdvanced = false;
  effectiveStackInBigBlinds = 100;
  potSizeInBigBlinds = 2.5;
  boardCards = '';
  actionSequence = '';
  villainRanges: VillainRangeSummary[] = [];

  constructor(
    private readonly profilesService: ProfilesService,
    private readonly rangesService: RangesService
  ) {
    this.applySpotTemplate(this.selectedSpotCode);
    this.profiles$.pipe(take(1)).subscribe((profiles) => {
      if (profiles.length > 0) {
        this.onProfileChange(profiles[0].id);
      }
    });
  }

  get selectedSpot(): DecisionSpotOption {
    return this.spotOptions.find((spot) => spot.code === this.selectedSpotCode) || this.spotOptions[0];
  }

  submit(profiles: StrategicProfileSummary[]): void {
    const profile = profiles.find((item) => item.id === this.selectedProfileId) || profiles[0];
    if (!profile || !this.selectedHandCode) {
      return;
    }

    const events = this.parseActionSequence(this.actionSequence);
    const nextOrderIndex = events.length === 0 ? 1 : Math.max(...events.map((event) => event.orderIndex)) + 1;

    events.push({
      orderIndex: nextOrderIndex,
      actorType: ActorType.VILLAIN,
      actorPosition: this.selectedSpot.villainPosition,
      street: this.selectedSpot.street,
      actionCode: this.selectedFacingAction,
      sizingType: this.selectedSizingValue ? this.selectedSizingType : null,
      sizingValue: this.selectedSizingValue || null
    });

    this.submitDecision.emit({
      strategyProfileId: profile.id,
      villainRangeSetId: this.useVillainAdvancedInput && this.villainInputMode === 'CUSTOM'
        ? this.selectedVillainRangeSetId
        : null,
      villainRangePercent: this.useVillainAdvancedInput && this.villainInputMode === 'PERCENT'
        ? this.villainRangePercent
        : null,
      gameType: profile.gameType as GameType,
      heroPosition: this.selectedSpot.heroPosition,
      villainPosition: this.selectedSpot.villainPosition,
      scenarioType: this.selectedSpot.scenarioType,
      street: this.selectedSpot.street,
      effectiveStackInBigBlinds: this.effectiveStackInBigBlinds,
      potSizeInBigBlinds: this.potSizeInBigBlinds,
      heroCards: this.heroCardsFromHandCode(this.selectedHandCode),
      boardCards: this.splitCsv(this.boardCards),
      actionEvents: events
    });
  }

  onProfileChange(profileId: string): void {
    this.selectedProfileId = profileId || null;
    this.selectedVillainRangeSetId = null;
    if (this.selectedProfileId) {
      this.rangesService.listVillain(this.selectedProfileId).subscribe((ranges) => {
        this.villainRanges = ranges;
      });
      return;
    }
    this.villainRanges = [];
  }

  onVillainRangeChange(villainRangeSetId: string | null): void {
    this.selectedVillainRangeSetId = villainRangeSetId;
  }

  onVillainInputModeChange(mode: VillainInputMode): void {
    this.villainInputMode = mode;
    if (mode === 'CUSTOM' && this.selectedVillainRangeSetId === null && this.villainRanges.length > 0) {
      this.selectedVillainRangeSetId = this.villainRanges[0].id;
    }
  }

  onSpotChange(spotCode: string): void {
    this.selectedSpotCode = spotCode;
    this.applySpotTemplate(spotCode);
  }

  onSelectHandCode(handCode: string): void {
    this.selectedHandCode = handCode;
  }

  onSelectFacingAction(actionType: ActionType): void {
    this.selectedFacingAction = actionType;
  }

  onSelectSizingType(sizingType: SizingType): void {
    this.selectedSizingType = sizingType;
    if (sizingType === SizingType.ALL_IN) {
      this.selectedSizingValue = 0;
    }
  }

  trackBySpot(_: number, spot: DecisionSpotOption): string {
    return spot.code;
  }

  displayHandCode(handCode: string): string {
    return formatHandCode(handCode);
  }

  private splitCsv(value: string): string[] {
    return (value ?? '')
      .split(/[, ]+/)
      .map((entry) => entry.trim())
      .filter((entry) => entry.length > 0);
  }

  private heroCardsFromHandCode(handCode: string): string[] {
    const firstRank = handCode[0];
    const secondRank = handCode[1];
    if (handCode.length === 2) {
      return [`${firstRank}s`, `${secondRank}h`];
    }

    const suited = handCode.endsWith('S');
    return suited
      ? [`${firstRank}s`, `${secondRank}s`]
      : [`${firstRank}s`, `${secondRank}d`];
  }

  private parseActionSequence(raw: string): ActionEvent[] {
    const lines = raw
      .split('\n')
      .map((line) => line.trim())
      .filter((line) => line.length > 0);

    if (lines.length === 0) {
      return [];
    }

    return lines
      .map((line, index) => this.toActionEvent(line, index))
      .filter((event): event is ActionEvent => event !== null);
  }

  private toActionEvent(line: string, index: number): ActionEvent | null {
    const [streetRaw, actorRaw, actionRaw, sizingRaw] = line.split(':').map((part) => part.trim().toUpperCase());

    if (!streetRaw || !actorRaw || !actionRaw) {
      return null;
    }

    if (!this.streets.includes(streetRaw as Street)) {
      return null;
    }

    if (!Object.values(ActorType).includes(actorRaw as ActorType)) {
      return null;
    }

    if (!this.actionTypes.includes(actionRaw as ActionType)) {
      return null;
    }

    const sizingValue = sizingRaw ? Number(sizingRaw) : null;

    return {
      orderIndex: index + 1,
      actorType: actorRaw as ActorType,
      street: streetRaw as Street,
      actionCode: actionRaw as ActionType,
      sizingType: sizingValue !== null && !Number.isNaN(sizingValue) ? SizingType.BB : null,
      sizingValue: sizingValue !== null && !Number.isNaN(sizingValue) ? sizingValue : null
    };
  }

  private applySpotTemplate(spotCode: string): void {
    const spot = this.spotOptions.find((option) => option.code === spotCode);
    if (!spot) {
      return;
    }

    this.selectedFacingAction = spot.defaultFacingAction;
  }
}
