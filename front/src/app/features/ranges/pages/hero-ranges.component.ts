import { Component, OnInit } from '@angular/core';
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
  PLAYER_POSITION_OPTIONS,
  SCENARIO_TYPE_OPTIONS,
  STREET_OPTIONS
} from '../../../core/models/referentials';
import { StrategicProfileSummary } from '../../../core/models/profile.models';
import { ProfilesService } from '../../../core/services/profiles.service';
import { RangesService } from '../../../core/services/ranges.service';
import { HandSelectionEvent } from '../components/poker-hand-matrix.component';

@Component({
  selector: 'app-hero-ranges',
  templateUrl: './hero-ranges.component.html',
  styleUrls: ['./hero-ranges.component.scss']
})
export class HeroRangesComponent implements OnInit {
  readonly gameTypes = GAME_TYPE_OPTIONS;
  readonly positions = PLAYER_POSITION_OPTIONS;
  readonly streets = STREET_OPTIONS;
  readonly scenarioTypes = SCENARIO_TYPE_OPTIONS;

  readonly filtersForm = this.fb.nonNullable.group({
    profileId: ['', Validators.required],
    gameType: [GAME_TYPE_OPTIONS[0], Validators.required],
    street: [STREET_OPTIONS[0], Validators.required],
    heroPosition: [PLAYER_POSITION_OPTIONS[0], Validators.required],
    scenarioType: [SCENARIO_TYPE_OPTIONS[0], Validators.required],
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

  constructor(
    private readonly fb: FormBuilder,
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
    this.rangesService.listHero(profileId).subscribe((ranges) => {
      this.rangeSummaries = ranges;
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
      .getHeroByContext(this.filtersForm.getRawValue())
      .pipe(
        catchError(() => {
          this.startDraft();
          this.feedbackMessage = 'Aucune range existante. Brouillon initialise a partir du contexte courant.';
          return EMPTY;
        })
      )
      .subscribe((range) => this.applyRange(range));
  }

  openRange(rangeId: string): void {
    this.warningMessage = null;
    this.feedbackMessage = null;
    this.rangesService.getHero(rangeId).subscribe((range) => this.applyRange(range));
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
    });
  }

  deleteRange(): void {
    if (!this.currentRangeId || !window.confirm('Supprimer cette range hero ?')) {
      return;
    }

    const profileId = this.filtersForm.controls.profileId.value;
    this.rangesService.deleteHero(this.currentRangeId).subscribe(() => {
      this.startDraft();
      this.feedbackMessage = 'Range hero supprimee.';
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
      return;
    }

    this.selectedHandCodes = this.selectedHandCodes.includes(handCode)
      ? this.selectedHandCodes.filter((code) => code !== handCode)
      : [...this.selectedHandCodes, handCode];
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

    this.warningMessage = nextCell.enabled && !nextCell.legendCode ? 'Pensez a renseigner une legende strategique pour la cellule active.' : null;
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
  }

  private applyRange(range: HeroRangeDetail): void {
    this.currentRangeId = range.id;
    this.currentRangeName = range.name;
    this.filtersForm.patchValue({
      profileId: range.profileId,
      gameType: range.gameType,
      street: range.street,
      heroPosition: range.heroPosition,
      scenarioType: range.scenarioType,
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
  }

  private suggestRangeName(): string {
    const value = this.filtersForm.getRawValue();
    return `${value.heroPosition} ${value.street} ${value.scenarioType}`;
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
}
