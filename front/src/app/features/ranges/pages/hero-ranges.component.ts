import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';
import { EMPTY, catchError, switchMap } from 'rxjs';
import {
  HeroRangeCellDto,
  HeroRangeDetail,
  HeroRangeSummary,
  HeroRangeUpsertPayload,
  PokerHandMatrixCell
} from '../../../core/models/range.models';
import {
  GAME_TYPE_OPTIONS,
  PlayerPosition,
  PLAYER_POSITION_OPTIONS,
  ScenarioType,
  SCENARIO_TYPE_OPTIONS,
  STRATEGY_LEGEND_OPTIONS,
  Street,
  STREET_OPTIONS,
  StrategyLegend
} from '../../../core/models/referentials';
import { StrategicProfileSummary } from '../../../core/models/profile.models';
import { ProfilesService } from '../../../core/services/profiles.service';
import { RangesService } from '../../../core/services/ranges.service';
import { HandSelectionEvent } from '../components/poker-hand-matrix.component';

@Component({
  selector: 'app-hero-ranges',
  templateUrl: './hero-ranges.component.html',
  styleUrls: ['./hero-ranges.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeroRangesComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  readonly gameTypes = GAME_TYPE_OPTIONS;
  readonly positions = PLAYER_POSITION_OPTIONS;
  readonly streets = STREET_OPTIONS;
  readonly scenarioTypes = SCENARIO_TYPE_OPTIONS;
  readonly legendOptions = STRATEGY_LEGEND_OPTIONS;
  private readonly legendColors: Record<StrategyLegend, string> = {
    [StrategyLegend.OPEN]: '#4f7db8',
    [StrategyLegend.CALL]: '#3e8b46',
    [StrategyLegend.CALL_ONLY]: '#2e7d32',
    [StrategyLegend.THREE_BET]: '#8a63a8',
    [StrategyLegend.FOUR_BET]: '#ee5f5f',
    [StrategyLegend.SHOVE]: '#a52714',
    [StrategyLegend.DEFEND]: '#4e8d5e',
    [StrategyLegend.ISO_RAISE]: '#7b4fa3',
    [StrategyLegend.FOLD]: '#c9ced6',
    [StrategyLegend.CHECK]: '#90a4ae',
    [StrategyLegend.CHECK_CALL]: '#558b2f',
    [StrategyLegend.CHECK_FOLD]: '#b0bec5',
    [StrategyLegend.CHECK_RAISE]: '#6a1b9a',
    [StrategyLegend.BET_25]: '#ffd54f',
    [StrategyLegend.BET_50]: '#ffb300',
    [StrategyLegend.BET_75]: '#ff8f00',
    [StrategyLegend.RAISE]: '#ab47bc'
  };

  readonly filtersForm = this.fb.nonNullable.group({
    profileId: ['', Validators.required],
    gameType: [GAME_TYPE_OPTIONS[0], Validators.required],
    street: [STREET_OPTIONS[0], Validators.required],
    heroPosition: [PLAYER_POSITION_OPTIONS[0], Validators.required],
    scenarioType: ['' as ScenarioType | ''],
    subScenarioCode: ['']
  });

  readonly metadataForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', Validators.maxLength(1000)],
    enabled: [true, Validators.required],
    priority: [0, Validators.required],
    notes: ['', Validators.maxLength(2000)]
  });

  profiles: StrategicProfileSummary[] = [];
  rangeSummaries: HeroRangeSummary[] = [];
  cellsByHandCode: Record<string, HeroRangeCellDto> = {};
  selectedHandCodes: string[] = [];
  currentRangeId: string | null = null;
  currentRangeName = 'Nouvelle range hero';
  warningMessage: string | null = null;
  feedbackMessage: string | null = null;
  selectedLegendPreset: StrategyLegend = StrategyLegend.OPEN;
  selectedPaintColor = this.legendColors[StrategyLegend.OPEN];

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly profilesService: ProfilesService,
    private readonly rangesService: RangesService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.profilesService.list()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((profiles) => {
      this.profiles = profiles;
      if (!this.filtersForm.controls.profileId.value && profiles.length > 0) {
        this.filtersForm.patchValue({
          profileId: profiles[0].id,
          gameType: profiles[0].gameType
        });
        this.startDraft();
        this.loadRangeSummaries(profiles[0].id);
      }
      this.cdr.markForCheck();
    });

    this.filtersForm.controls.profileId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((profileId) => {
      if (profileId) {
        this.loadRangeSummaries(profileId);
      } else {
        this.rangeSummaries = [];
        this.cdr.markForCheck();
      }
    });

    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
      const rangeId = params.get('rangeId');
      if (rangeId) {
        this.openRange(rangeId);
      }
    });
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

  onLegendPresetSelect(preset: StrategyLegend): void {
    this.selectedLegendPreset = preset;
    this.selectedPaintColor = this.legendColors[preset] ?? this.selectedPaintColor;
    if (this.selectedHandCodes.length > 0) {
      this.applyLegendPreset(this.selectedHandCodes, false);
    }
  }

  onPaintColorChange(color: string): void {
    this.selectedPaintColor = color;
    if (this.selectedHandCodes.length > 0) {
      this.applyLegendPreset(this.selectedHandCodes, false);
    }
  }

  disableSelectedHands(): void {
    if (this.selectedHandCodes.length > 0) {
      this.applyLegendPreset(this.selectedHandCodes, true);
    }
  }

  get matrixCells(): PokerHandMatrixCell[] {
    return Object.values(this.cellsByHandCode).map((cell) => ({
      handCode: cell.handCode,
      enabled: cell.enabled,
      accentColor: cell.colorCode,
      badge: cell.legendCode,
      note: cell.note
    }));
  }

  get selectedCells(): HeroRangeCellDto[] {
    return this.selectedHandCodes.map((handCode) => this.cellsByHandCode[handCode] ?? this.buildEmptyCell(handCode));
  }

  get selectionCount(): number {
    return this.selectedHandCodes.length;
  }

  loadRangeSummaries(profileId: string): void {
    this.rangesService.listHero(profileId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((ranges) => {
        this.rangeSummaries = ranges;
        this.cdr.markForCheck();
      });
  }

  loadByContext(): void {
    if (this.filtersForm.invalid) {
      this.filtersForm.markAllAsTouched();
      return;
    }

    this.warningMessage = null;
    this.feedbackMessage = null;

    this.rangesService
      .getHeroByContext({
        ...this.filtersForm.getRawValue(),
        scenarioType: this.getOptionalScenarioType()
      })
      .pipe(
        catchError(() => {
          this.startDraft();
          this.feedbackMessage = 'Aucune range existante. Brouillon initialise a partir du contexte courant.';
          this.cdr.markForCheck();
          return EMPTY;
        })
      )
      .subscribe((range) => this.applyRange(range));
  }

  openRange(rangeId: string): void {
    this.warningMessage = null;
    this.feedbackMessage = null;
    this.rangesService.getHero(rangeId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((range) => this.applyRange(range));
  }

  newRange(): void {
    this.startDraft();
    this.feedbackMessage = 'Nouvelle range hero prete a etre enregistree.';
  }

  saveRange(): void {
    if (this.filtersForm.invalid || this.metadataForm.invalid) {
      this.filtersForm.markAllAsTouched();
      this.metadataForm.markAllAsTouched();
      return;
    }

    if (this.hasEnabledCellsWithoutLegend()) {
      this.warningMessage = 'Chaque cellule hero active doit avoir une legende strategique.';
      return;
    }

    const payload: HeroRangeUpsertPayload = {
      ...this.filtersForm.getRawValue(),
      ...this.metadataForm.getRawValue(),
      scenarioType: this.getOptionalScenarioType(),
      subScenarioCode: this.filtersForm.controls.subScenarioCode.value || null,
      notes: this.metadataForm.controls.notes.value || null
    };

    const cellsPayload = { cells: this.getPersistedCells() };
    const save$ = this.currentRangeId
      ? this.rangesService.updateHero(this.currentRangeId, payload).pipe(
          switchMap((range) => this.rangesService.updateHeroCells(range.id, cellsPayload))
        )
      : this.rangesService.createHero(payload).pipe(
          switchMap((range) => this.rangesService.updateHeroCells(range.id, cellsPayload))
        );

    save$.subscribe((range) => {
      this.applyRange(range);
      this.feedbackMessage = 'Range hero enregistree.';
      this.loadRangeSummaries(range.profileId);
      this.cdr.markForCheck();
    });
  }

  deleteRange(): void {
    if (!this.currentRangeId || !window.confirm('Supprimer cette range hero ?')) {
      return;
    }

    const profileId = this.filtersForm.controls.profileId.value;
    this.rangesService.deleteHero(this.currentRangeId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.startDraft();
        this.feedbackMessage = 'Range hero supprimee.';
        if (profileId) {
          this.loadRangeSummaries(profileId);
        } else {
          this.cdr.markForCheck();
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
      this.applyLegendPreset([handCode], false);
      return;
    }

    this.selectedHandCodes = this.selectedHandCodes.includes(handCode)
      ? this.selectedHandCodes.filter((code) => code !== handCode)
      : [...this.selectedHandCodes, handCode];
    this.applyLegendPreset(this.selectedHandCodes, false);
  }

  clearSelection(): void {
    this.selectedHandCodes = [];
    this.cdr.markForCheck();
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

    this.warningMessage = nextCell.enabled && !nextCell.legendCode ? 'Pensez a renseigner une legende strategique pour la cellule active.' : null;
    this.cdr.markForCheck();
  }

  patchSelectedCell(patch: Partial<HeroRangeCellDto>): void {
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
    this.warningMessage = null;
    this.cdr.markForCheck();
  }

  private applyRange(range: HeroRangeDetail): void {
    this.currentRangeId = range.id;
    this.currentRangeName = range.name;
    this.filtersForm.patchValue({
      profileId: range.profileId,
      gameType: range.gameType,
      street: range.street,
      heroPosition: range.heroPosition,
      scenarioType: range.scenarioType ?? '',
      subScenarioCode: range.subScenarioCode ?? ''
    });
    this.metadataForm.patchValue({
      name: range.name,
      description: range.description,
      enabled: range.enabled,
      priority: range.priority,
      notes: range.notes ?? ''
    });
    this.cellsByHandCode = range.cells.reduce<Record<string, HeroRangeCellDto>>((acc, cell) => {
      acc[cell.handCode] = cell;
      return acc;
    }, {});
    this.selectedHandCodes = range.cells[0] ? [range.cells[0].handCode] : [];
    this.cdr.markForCheck();
  }

  private startDraft(): void {
    this.currentRangeId = null;
    this.currentRangeName = 'Nouvelle range hero';
    this.metadataForm.patchValue({
      name: this.suggestRangeName(),
      description: '',
      enabled: true,
      priority: 0,
      notes: ''
    });
    this.cellsByHandCode = {};
    this.selectedHandCodes = [];
    this.cdr.markForCheck();
  }

  private suggestRangeName(): string {
    const value = this.filtersForm.getRawValue();
    return `Hero ${value.heroPosition} ${value.street}`;
  }

  private getOptionalScenarioType(): ScenarioType | null {
    return (this.filtersForm.controls.scenarioType.value as ScenarioType | '') || null;
  }

  private hasEnabledCellsWithoutLegend(): boolean {
    return this.getPersistedCells().some((cell) => cell.enabled && !cell.legendCode);
  }

  private getPersistedCells(): HeroRangeCellDto[] {
    return Object.values(this.cellsByHandCode).filter((cell) => cell.enabled || !!cell.legendCode || !!cell.note || !!cell.colorCode);
  }

  private buildCell(handCode: string): HeroRangeCellDto {
    return this.cellsByHandCode[handCode] ?? this.buildEmptyCell(handCode);
  }

  private buildEmptyCell(handCode: string): HeroRangeCellDto {
    return {
      handCode,
      enabled: false,
      legendCode: null,
      colorCode: '#93c5fd',
      note: null
    };
  }

  private applyLegendPreset(handCodes: string[], disable: boolean): void {
    if (handCodes.length === 0) {
      return;
    }

    const nextLegend = disable ? null : this.selectedLegendPreset;
    const nextColor = disable ? '#c9ced6' : this.selectedPaintColor;

    const updatedCells = { ...this.cellsByHandCode };
    handCodes.forEach((handCode) => {
      updatedCells[handCode] = {
        ...this.buildCell(handCode),
        handCode,
        enabled: !disable,
        legendCode: nextLegend,
        colorCode: nextColor
      };
    });
    this.cellsByHandCode = updatedCells;
    this.cdr.markForCheck();
  }
}
