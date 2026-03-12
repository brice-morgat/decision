import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';
import { EMPTY, catchError, switchMap } from 'rxjs';
import {
  PokerHandMatrixCell,
  VillainRangeScope,
  VillainRangeCellDto,
  VillainRangeDetail,
  VillainRangeSummary,
  VillainRangeUpsertPayload
} from '../../../core/models/range.models';
import {
  ActionType,
  ACTION_TYPE_OPTIONS,
  GAME_TYPE_OPTIONS,
  PlayerPosition,
  PLAYER_POSITION_OPTIONS,
  ScenarioType,
  SCENARIO_TYPE_OPTIONS,
  Street,
  STREET_OPTIONS
} from '../../../core/models/referentials';
import { StrategicProfileSummary } from '../../../core/models/profile.models';
import { ProfilesService } from '../../../core/services/profiles.service';
import { RangesService } from '../../../core/services/ranges.service';
import { HandSelectionEvent } from '../components/poker-hand-matrix.component';
import { buildHandMatrixCodes } from '../utils/poker-hand-codes';

type VillainEditionMode = 'STANDARD' | 'ADVANCED';
type VillainPreset = 'TIGHT' | 'STANDARD' | 'LOOSE';
type VillainTagPreset = 'VALUE' | 'CALL' | 'BLUFF' | 'DISABLE';

interface VillainQuickSpot {
  code: string;
  label: string;
  villainPosition: PlayerPosition;
  heroPosition: PlayerPosition | null;
  scenarioType: ScenarioType;
  street: Street;
  triggerActionCode: ActionType | null;
}

@Component({
  selector: 'app-villain-ranges',
  templateUrl: './villain-ranges.component.html',
  styleUrls: ['./villain-ranges.component.scss']
})
export class VillainRangesComponent implements OnInit {
  private static readonly HAND_CODES = buildHandMatrixCodes().flat();

  readonly gameTypes = GAME_TYPE_OPTIONS;
  readonly positions = PLAYER_POSITION_OPTIONS;
  readonly streets = STREET_OPTIONS;
  readonly scenarioTypes = SCENARIO_TYPE_OPTIONS;
  readonly actionTypes = ACTION_TYPE_OPTIONS;
  readonly villainEditionModes: VillainEditionMode[] = ['STANDARD', 'ADVANCED'];
  readonly villainPresets: VillainPreset[] = ['TIGHT', 'STANDARD', 'LOOSE'];
  readonly quickSpots: VillainQuickSpot[] = [
    {
      code: 'BB_VS_BTN_OPEN',
      label: 'BB vs BTN open',
      villainPosition: PlayerPosition.BTN,
      heroPosition: PlayerPosition.BB,
      scenarioType: ScenarioType.FACING_OPEN,
      street: Street.PREFLOP,
      triggerActionCode: ActionType.OPEN
    },
    {
      code: 'BB_VS_CO_OPEN',
      label: 'BB vs CO open',
      villainPosition: PlayerPosition.CO,
      heroPosition: PlayerPosition.BB,
      scenarioType: ScenarioType.FACING_OPEN,
      street: Street.PREFLOP,
      triggerActionCode: ActionType.OPEN
    },
    {
      code: 'SB_VS_BTN_OPEN',
      label: 'SB vs BTN open',
      villainPosition: PlayerPosition.BTN,
      heroPosition: PlayerPosition.SB,
      scenarioType: ScenarioType.FACING_OPEN,
      street: Street.PREFLOP,
      triggerActionCode: ActionType.OPEN
    },
    {
      code: 'BTN_OPEN_FIRST_IN',
      label: 'BTN open first in',
      villainPosition: PlayerPosition.BB,
      heroPosition: PlayerPosition.BTN,
      scenarioType: ScenarioType.OPEN_FIRST_IN,
      street: Street.PREFLOP,
      triggerActionCode: ActionType.CHECK
    }
  ];
  readonly tagPresets: Array<{ code: VillainTagPreset; label: string; color: string; weight: number }> = [
    { code: 'VALUE', label: 'Value', color: '#4f7db8', weight: 100 },
    { code: 'CALL', label: 'Call', color: '#3e8b46', weight: 70 },
    { code: 'BLUFF', label: 'Bluff', color: '#8a63a8', weight: 35 },
    { code: 'DISABLE', label: 'Fold', color: '#c9ced6', weight: 0 }
  ];

  readonly filtersForm = this.fb.nonNullable.group({
    profileId: ['', Validators.required],
    gameType: [GAME_TYPE_OPTIONS[0], Validators.required],
    street: [STREET_OPTIONS[0], Validators.required],
    villainPosition: [PLAYER_POSITION_OPTIONS[0], Validators.required],
    heroPosition: [''],
    scenarioType: [SCENARIO_TYPE_OPTIONS[0], Validators.required],
    triggerActionCode: [''],
    lineSignature: ['']
  });

  readonly metadataForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', Validators.maxLength(1000)],
    enabled: [true, Validators.required],
    priority: [0, Validators.required],
    notes: ['', Validators.maxLength(2000)]
  });

  readonly standardForm = this.fb.nonNullable.group({
    mode: ['STANDARD' as VillainEditionMode, Validators.required],
    preset: ['STANDARD' as VillainPreset, Validators.required],
    rangeWidthPercent: [40, [Validators.min(5), Validators.max(90)]],
    aggressionPercent: [50, [Validators.min(0), Validators.max(100)]],
    applyHeroPositions: [[] as PlayerPosition[]],
    applyVillainPositions: [[PLAYER_POSITION_OPTIONS[0]] as PlayerPosition[]],
    applyTriggerActions: [[] as ActionType[]]
  });

  profiles: StrategicProfileSummary[] = [];
  rangeSummaries: VillainRangeSummary[] = [];
  cellsByHandCode: Record<string, VillainRangeCellDto> = {};
  selectedHandCodes: string[] = [];
  currentRangeId: string | null = null;
  currentRangeName = 'Nouvelle range vilain';
  feedbackMessage: string | null = null;
  selectedQuickSpotCode = 'BB_VS_BTN_OPEN';
  selectedTagPreset: VillainTagPreset = 'VALUE';

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly profilesService: ProfilesService,
    private readonly rangesService: RangesService
  ) {}

  ngOnInit(): void {
    this.profilesService.list().subscribe((profiles) => {
      this.profiles = profiles;
      if (!this.filtersForm.controls.profileId.value && profiles.length > 0) {
        this.filtersForm.patchValue({
          profileId: profiles[0].id,
          gameType: profiles[0].gameType
        });
        this.onQuickSpotSelect(this.selectedQuickSpotCode);
        this.loadRangeSummaries(profiles[0].id);
      }
    });

    this.filtersForm.controls.profileId.valueChanges.subscribe((profileId) => {
      if (profileId) {
        this.loadRangeSummaries(profileId);
      } else {
        this.rangeSummaries = [];
      }
    });

    this.route.queryParamMap.subscribe((params) => {
      const rangeId = params.get('rangeId');
      if (rangeId) {
        this.openRange(rangeId);
      }
    });
  }

  get matrixCells(): PokerHandMatrixCell[] {
    return Object.values(this.cellsByHandCode).map((cell) => ({
      handCode: cell.handCode,
      enabled: cell.enabled,
      badge: cell.tagCode,
      note: cell.note,
      weight: cell.weight,
      accentColor: this.resolveTagColor(cell.tagCode)
    }));
  }

  get selectedCells(): VillainRangeCellDto[] {
    return this.selectedHandCodes.map((handCode) => this.cellsByHandCode[handCode] ?? this.buildEmptyCell(handCode));
  }

  get selectionCount(): number {
    return this.selectedHandCodes.length;
  }

  get activeCellsCount(): number {
    return this.getPersistedCells().length;
  }

  get isAdvancedMode(): boolean {
    return this.standardForm.controls.mode.value === 'ADVANCED';
  }

  onProfileQuickSelect(profileId: string): void {
    const profile = this.profiles.find((item) => item.id === profileId);
    if (!profile) {
      return;
    }

    this.filtersForm.patchValue({
      profileId: profile.id,
      gameType: profile.gameType
    });
  }

  onQuickSpotSelect(spotCode: string): void {
    this.selectedQuickSpotCode = spotCode;
    const spot = this.quickSpots.find((item) => item.code === spotCode);
    if (!spot) {
      return;
    }

    this.filtersForm.patchValue({
      street: spot.street,
      villainPosition: spot.villainPosition,
      heroPosition: spot.heroPosition ?? '',
      scenarioType: spot.scenarioType,
      triggerActionCode: spot.triggerActionCode ?? ''
    });
  }

  onTagPresetSelect(preset: VillainTagPreset): void {
    this.selectedTagPreset = preset;
    if (this.selectedHandCodes.length > 0) {
      this.applyTagPreset(this.selectedHandCodes);
    }
  }

  onEditionMode(mode: VillainEditionMode): void {
    this.standardForm.controls.mode.setValue(mode);
  }

  toggleVillainPosition(position: PlayerPosition): void {
    const current = this.standardForm.controls.applyVillainPositions.value;
    const next = current.includes(position) ? current.filter((item) => item !== position) : [...current, position];
    this.standardForm.controls.applyVillainPositions.setValue(next);
  }

  toggleHeroPosition(position: PlayerPosition): void {
    const current = this.standardForm.controls.applyHeroPositions.value;
    const next = current.includes(position) ? current.filter((item) => item !== position) : [...current, position];
    this.standardForm.controls.applyHeroPositions.setValue(next);
  }

  toggleTriggerAction(action: ActionType): void {
    const current = this.standardForm.controls.applyTriggerActions.value;
    const next = current.includes(action) ? current.filter((item) => item !== action) : [...current, action];
    this.standardForm.controls.applyTriggerActions.setValue(next);
  }

  loadRangeSummaries(profileId: string): void {
    this.rangesService.listVillain(profileId).subscribe((ranges) => {
      this.rangeSummaries = ranges;
    });
  }

  loadByContext(): void {
    if (this.filtersForm.invalid) {
      this.filtersForm.markAllAsTouched();
      return;
    }

    this.feedbackMessage = null;
    this.rangesService
      .getVillainByContext({
        ...this.filtersForm.getRawValue(),
        heroPosition: this.getOptionalHeroPosition(),
        triggerActionCode: this.getOptionalTriggerActionCode(),
        lineSignature: this.filtersForm.controls.lineSignature.value || null
      })
      .pipe(
        catchError(() => {
          this.startDraft();
          this.feedbackMessage = 'Aucune range vilain existante. Brouillon initialise.';
          return EMPTY;
        })
      )
      .subscribe((range) => this.applyRange(range));
  }

  openRange(rangeId: string): void {
    this.feedbackMessage = null;
    this.rangesService.getVillain(rangeId).subscribe((range) => this.applyRange(range));
  }

  newRange(): void {
    this.startDraft();
    this.feedbackMessage = 'Nouvelle range vilain prete a etre enregistree.';
  }

  applyStandardPreset(): void {
    const preset = this.standardForm.controls.preset.value;
    const baseWidth = preset === 'TIGHT' ? 25 : preset === 'LOOSE' ? 55 : 40;
    const baseAggression = preset === 'TIGHT' ? 65 : preset === 'LOOSE' ? 40 : 50;

    const rangeWidth = this.standardForm.controls.rangeWidthPercent.value;
    const aggression = this.standardForm.controls.aggressionPercent.value;
    const effectiveWidth = Math.round((baseWidth + rangeWidth) / 2);
    const effectiveAggression = Math.round((baseAggression + aggression) / 2);

    const minScore = 100 - effectiveWidth;
    const raiseThreshold = 60 + Math.round((effectiveAggression - 50) / 2);
    const callThreshold = minScore;

    const generatedCells: Record<string, VillainRangeCellDto> = {};
    VillainRangesComponent.HAND_CODES.forEach((handCode) => {
      const score = this.scoreHandCode(handCode);
      const enabled = score >= minScore;

      if (!enabled) {
        return;
      }

      generatedCells[handCode] = {
        handCode,
        enabled: true,
        weight: Math.max(10, Math.min(100, 20 + score)),
        tagCode: score >= raiseThreshold ? 'VALUE' : score >= callThreshold ? 'CALL' : 'BLUFF',
        note: null
      };
    });

    this.cellsByHandCode = generatedCells;
    this.selectedHandCodes = Object.keys(generatedCells).slice(0, 1);
    this.feedbackMessage = `Mode standard applique (${preset.toLowerCase()}) - ${Object.keys(generatedCells).length} mains activees.`;
  }

  saveRange(): void {
    if (this.filtersForm.invalid || this.metadataForm.invalid) {
      this.filtersForm.markAllAsTouched();
      this.metadataForm.markAllAsTouched();
      return;
    }

    const payload: VillainRangeUpsertPayload = {
      ...this.filtersForm.getRawValue(),
      ...this.metadataForm.getRawValue(),
      heroPosition: this.getOptionalHeroPosition(),
      triggerActionCode: this.getOptionalTriggerActionCode(),
      lineSignature: this.filtersForm.controls.lineSignature.value || null,
      notes: this.metadataForm.controls.notes.value || null,
      scopes: this.isAdvancedMode ? [] : this.buildStandardScopes()
    };

    const cellsPayload = { cells: this.getPersistedCells() };
    const save$ = this.isAdvancedMode
      ? (this.currentRangeId
      ? this.rangesService.updateVillain(this.currentRangeId, payload).pipe(
          switchMap((range) => this.rangesService.updateVillainCells(range.id, cellsPayload))
        )
      : this.rangesService.createVillain(payload).pipe(
          switchMap((range) => this.rangesService.updateVillainCells(range.id, cellsPayload))
        ))
      : this.rangesService.bulkVillain(payload).pipe(
          switchMap((range) => this.rangesService.updateVillainCells(range.id, cellsPayload))
        );

    save$.subscribe((range) => {
      this.applyRange(range);
      this.feedbackMessage = 'Range vilain enregistree.';
      this.loadRangeSummaries(range.profileId);
    });
  }

  deleteRange(): void {
    if (!this.currentRangeId || !window.confirm('Supprimer cette range vilain ?')) {
      return;
    }

    const profileId = this.filtersForm.controls.profileId.value;
    this.rangesService.deleteVillain(this.currentRangeId).subscribe(() => {
      this.startDraft();
      this.feedbackMessage = 'Range vilain supprimee.';
      if (profileId) {
        this.loadRangeSummaries(profileId);
      }
    });
  }

  selectHandCode(event: HandSelectionEvent): void {
    const { handCode, additive } = event;
    if (!this.cellsByHandCode[handCode]) {
      this.cellsByHandCode = {
        ...this.cellsByHandCode,
        [handCode]: this.buildEmptyCell(handCode)
      };
    }

    if (!additive) {
      this.selectedHandCodes = [handCode];
      this.applyTagPreset([handCode]);
      return;
    }

    this.selectedHandCodes = this.selectedHandCodes.includes(handCode)
      ? this.selectedHandCodes.filter((code) => code !== handCode)
      : [...this.selectedHandCodes, handCode];
    this.applyTagPreset(this.selectedHandCodes);
  }

  clearSelection(): void {
    this.selectedHandCodes = [];
  }

  toggleHandCode(handCode: string): void {
    const nextCell = { ...this.buildCell(handCode), enabled: !this.buildCell(handCode).enabled };
    this.cellsByHandCode = {
      ...this.cellsByHandCode,
      [handCode]: nextCell
    };

    if (!this.selectedHandCodes.includes(handCode)) {
      this.selectedHandCodes = [handCode];
    }
  }

  patchSelectedCell(patch: Partial<VillainRangeCellDto>): void {
    if (this.selectedHandCodes.length === 0) {
      return;
    }

    const updatedCells = { ...this.cellsByHandCode };
    this.selectedHandCodes.forEach((handCode) => {
      updatedCells[handCode] = {
        ...this.buildCell(handCode),
        ...patch,
        handCode
      };
    });
    this.cellsByHandCode = updatedCells;
  }

  private applyRange(range: VillainRangeDetail): void {
    this.currentRangeId = range.id;
    this.currentRangeName = range.name;
    this.filtersForm.patchValue({
      profileId: range.profileId,
      gameType: range.gameType,
      street: range.street,
      villainPosition: range.villainPosition,
      heroPosition: range.heroPosition ?? '',
      scenarioType: range.scenarioType,
      triggerActionCode: range.triggerActionCode ?? '',
      lineSignature: range.lineSignature ?? ''
    });
    this.metadataForm.patchValue({
      name: range.name,
      description: range.description,
      enabled: range.enabled,
      priority: range.priority,
      notes: range.notes ?? ''
    });
    this.cellsByHandCode = range.cells.reduce<Record<string, VillainRangeCellDto>>((acc, cell) => {
      acc[cell.handCode] = cell;
      return acc;
    }, {});
    this.selectedHandCodes = range.cells[0] ? [range.cells[0].handCode] : [];
  }

  private startDraft(): void {
    this.currentRangeId = null;
    this.currentRangeName = 'Nouvelle range vilain';
    this.metadataForm.patchValue({
      name: this.suggestRangeName(),
      description: '',
      enabled: true,
      priority: 0,
      notes: ''
    });
    this.cellsByHandCode = {};
    this.selectedHandCodes = [];
    this.standardForm.patchValue({
      mode: 'STANDARD',
      preset: 'STANDARD',
      rangeWidthPercent: 40,
      aggressionPercent: 50,
      applyHeroPositions: [],
      applyVillainPositions: [this.filtersForm.controls.villainPosition.value],
      applyTriggerActions: []
    });
  }

  private suggestRangeName(): string {
    const value = this.filtersForm.getRawValue();
    return `${value.villainPosition} ${value.street} ${value.scenarioType}`;
  }

  private getOptionalHeroPosition(): PlayerPosition | null {
    return (this.filtersForm.controls.heroPosition.value as PlayerPosition | '') || null;
  }

  private getOptionalTriggerActionCode(): ActionType | null {
    return (this.filtersForm.controls.triggerActionCode.value as ActionType | '') || null;
  }

  private getPersistedCells(): VillainRangeCellDto[] {
    return Object.values(this.cellsByHandCode).filter((cell) => cell.enabled || !!cell.tagCode || !!cell.note || cell.weight != null);
  }

  private buildCell(handCode: string): VillainRangeCellDto {
    return this.cellsByHandCode[handCode] ?? this.buildEmptyCell(handCode);
  }

  private buildEmptyCell(handCode: string): VillainRangeCellDto {
    return {
      handCode,
      enabled: false,
      weight: 100,
      tagCode: null,
      note: null
    };
  }

  private buildStandardScopes(): VillainRangeScope[] {
    const heroPositions = this.standardForm.controls.applyHeroPositions.value;
    const villainPositions = this.standardForm.controls.applyVillainPositions.value;
    const triggerActions = this.standardForm.controls.applyTriggerActions.value;
    const lineSignature = this.filtersForm.controls.lineSignature.value || null;

    const resolvedVillainPositions = villainPositions.length > 0
      ? villainPositions
      : [this.filtersForm.controls.villainPosition.value];
    const heroCandidates = heroPositions.length > 0 ? heroPositions : [null];
    const triggerCandidates = triggerActions.length > 0 ? triggerActions : [null];

    const scopes: VillainRangeScope[] = [];
    resolvedVillainPositions.forEach((villainPosition) => {
      heroCandidates.forEach((heroPosition) => {
        triggerCandidates.forEach((triggerAction) => {
          scopes.push({
            heroPosition,
            villainPosition,
            triggerActionCode: triggerAction,
            lineSignature,
            scopeWeight: 0
          });
        });
      });
    });

    return scopes;
  }

  private scoreHandCode(handCode: string): number {
    const rankWeights: Record<string, number> = {
      A: 13, K: 12, Q: 11, J: 10, T: 9,
      '9': 8, '8': 7, '7': 6, '6': 5, '5': 4, '4': 3, '3': 2, '2': 1
    };

    if (handCode.length === 2) {
      return Math.min(100, (rankWeights[handCode[0]] ?? 0) * 6 + 20);
    }

    const firstRank = handCode[0];
    const secondRank = handCode[1];
    const suited = handCode.endsWith('S');
    const base = (rankWeights[firstRank] ?? 0) * 3 + (rankWeights[secondRank] ?? 0) * 2;
    const suitedBonus = suited ? 12 : 0;
    const connectorBonus = Math.abs((rankWeights[firstRank] ?? 0) - (rankWeights[secondRank] ?? 0)) === 1 ? 8 : 0;
    return Math.min(100, base + suitedBonus + connectorBonus);
  }

  private applyTagPreset(handCodes: string[]): void {
    if (handCodes.length === 0) {
      return;
    }

    const preset = this.tagPresets.find((item) => item.code === this.selectedTagPreset);
    if (!preset) {
      return;
    }

    const isDisable = preset.code === 'DISABLE';
    const updatedCells = { ...this.cellsByHandCode };
    handCodes.forEach((handCode) => {
      updatedCells[handCode] = {
        ...this.buildCell(handCode),
        handCode,
        enabled: !isDisable,
        tagCode: isDisable ? null : preset.code,
        weight: isDisable ? null : preset.weight
      };
    });
    this.cellsByHandCode = updatedCells;
  }

  private resolveTagColor(tagCode: string | null | undefined): string {
    if (!tagCode) {
      return '#eadccf';
    }
    return this.tagPresets.find((item) => item.code === tagCode)?.color ?? '#eadccf';
  }
}
