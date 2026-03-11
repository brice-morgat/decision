import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { EMPTY, catchError, switchMap } from 'rxjs';
import {
  PokerHandMatrixCell,
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
  SCENARIO_TYPE_OPTIONS,
  STREET_OPTIONS
} from '../../../core/models/referentials';
import { StrategicProfileSummary } from '../../../core/models/profile.models';
import { ProfilesService } from '../../../core/services/profiles.service';
import { RangesService } from '../../../core/services/ranges.service';
import { HandSelectionEvent } from '../components/poker-hand-matrix.component';

@Component({
  selector: 'app-villain-ranges',
  templateUrl: './villain-ranges.component.html',
  styleUrls: ['./villain-ranges.component.scss']
})
export class VillainRangesComponent implements OnInit {
  readonly gameTypes = GAME_TYPE_OPTIONS;
  readonly positions = PLAYER_POSITION_OPTIONS;
  readonly streets = STREET_OPTIONS;
  readonly scenarioTypes = SCENARIO_TYPE_OPTIONS;
  readonly actionTypes = ACTION_TYPE_OPTIONS;

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

  profiles: StrategicProfileSummary[] = [];
  rangeSummaries: VillainRangeSummary[] = [];
  cellsByHandCode: Record<string, VillainRangeCellDto> = {};
  selectedHandCodes: string[] = [];
  currentRangeId: string | null = null;
  currentRangeName = 'Nouvelle range vilain';
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
      badge: cell.tagCode,
      note: cell.note,
      weight: cell.weight
    }));
  }

  get selectedCells(): VillainRangeCellDto[] {
    return this.selectedHandCodes.map((handCode) => this.cellsByHandCode[handCode] ?? this.buildEmptyCell(handCode));
  }

  get selectionCount(): number {
    return this.selectedHandCodes.length;
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
      notes: this.metadataForm.controls.notes.value || null
    };

    const cellsPayload = { cells: this.getPersistedCells() };
    const save$ = this.currentRangeId
      ? this.rangesService.updateVillain(this.currentRangeId, payload).pipe(
          switchMap((range) => this.rangesService.updateVillainCells(range.id, cellsPayload))
        )
      : this.rangesService.createVillain(payload).pipe(
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
}
