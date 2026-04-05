import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  EventEmitter,
  Input,
  Output,
  inject
} from '@angular/core';
import { ActionEvent, DecisionRequest } from '../../../core/models/decision.models';
import { StrategicProfileSummary } from '../../../core/models/profile.models';
import { HeroRangeDetail, HeroRangeSummary, VillainRangeSummary } from '../../../core/models/range.models';
import { ProfilesService } from '../../../core/services/profiles.service';
import { RangesService } from '../../../core/services/ranges.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { take } from 'rxjs';
import { ActionType, ActorType, SizingType, Street, StrategyLegend } from '../../../core/models/referentials';
import { buildHandMatrixCodes, formatHandCode, HAND_RANKS } from '../../ranges/utils/poker-hand-codes';

@Component({
  selector: 'app-decision-context-form',
  templateUrl: './decision-context-form.component.html',
  styleUrls: ['./decision-context-form.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DecisionContextFormComponent {
  private readonly destroyRef = inject(DestroyRef);

  readonly defaultVillainRangePercent = 80;
  @Input() submitting = false;
  @Input() errorMessage: string | null = null;

  readonly handMatrix = buildHandMatrixCodes();
  readonly handRanks = HAND_RANKS;
  readonly profiles$ = this.profilesService.list();

  @Output() submitDecision = new EventEmitter<DecisionRequest>();

  selectedProfileId: string | null = null;
  selectedHeroRangeId: string | null = null;
  selectedVillainRangeSetId: string | null = null;
  villainInputMode: 'PERCENT' | 'RANGE' = 'PERCENT';
  villainRangePercent = this.defaultVillainRangePercent;
  selectedHeroRange: HeroRangeDetail | null = null;
  selectedHandCode: string | null = null;
  heroRanges: HeroRangeSummary[] = [];
  villainRanges: VillainRangeSummary[] = [];
  showAdvanced = false;
  effectiveStackInBigBlinds = 100;
  potSizeInBigBlinds = 0;
  boardCards = '';
  actionSequence = '';

  constructor(
    private readonly profilesService: ProfilesService,
    private readonly rangesService: RangesService,
    private readonly cdr: ChangeDetectorRef
  ) {
    this.profiles$.pipe(take(1), takeUntilDestroyed(this.destroyRef)).subscribe((profiles) => {
      if (profiles.length > 0) {
        this.onProfileChange(profiles[0].id);
      }
    });
  }

  get activeHeroHandCodes(): Set<string> {
    return new Set(
      (this.selectedHeroRange?.cells ?? [])
        .filter((cell) => cell.enabled && !!cell.legendCode)
        .map((cell) => cell.handCode)
    );
  }

  get selectedHeroRangeName(): string {
    return this.selectedHeroRange?.name ?? 'Selectionnez une range hero';
  }

  get selectedHeroCell() {
    return this.selectedHeroRange?.cells.find((cell) => cell.handCode === this.selectedHandCode) ?? null;
  }

  get selectedHeroLegend(): StrategyLegend | null {
    return this.selectedHeroCell?.legendCode ?? null;
  }

  get enabledHandsCount(): number {
    return this.selectedHeroRange?.cells.filter((cell) => cell.enabled).length ?? 0;
  }

  get villainRangeName(): string {
    if (this.villainInputMode === 'PERCENT') {
      return `Range synthetique ${this.villainRangePercent}%`;
    }
    return this.selectedVillainRange()?.name ?? 'Aucune range vilain';
  }

  submit(profiles: StrategicProfileSummary[]): void {
    const profile = profiles.find((item) => item.id === this.selectedProfileId) || profiles[0];
    if (!profile || !this.selectedHeroRange || !this.selectedHandCode) {
      return;
    }

    this.submitDecision.emit({
      strategyProfileId: profile.id,
      heroRangeSetId: this.selectedHeroRange.id,
      villainRangeSetId: this.villainInputMode === 'RANGE' ? this.selectedVillainRangeSetId : null,
      villainRangePercent: this.villainInputMode === 'PERCENT' ? this.villainRangePercent : null,
      gameType: this.selectedHeroRange.gameType,
      heroPosition: this.selectedHeroRange.heroPosition,
      villainPosition: this.villainInputMode === 'RANGE' ? this.selectedVillainRange()?.villainPosition ?? null : null,
      street: this.selectedHeroRange.street,
      scenarioType: null,
      effectiveStackInBigBlinds: this.effectiveStackInBigBlinds,
      potSizeInBigBlinds: this.potSizeInBigBlinds,
      heroHandCode: this.selectedHandCode,
      boardCards: this.splitCsv(this.boardCards),
      actionEvents: this.parseActionSequence(this.actionSequence)
    });
  }

  onProfileChange(profileId: string): void {
    this.selectedProfileId = profileId || null;
    this.selectedHeroRangeId = null;
    this.selectedVillainRangeSetId = null;
    this.villainInputMode = 'PERCENT';
    this.villainRangePercent = this.defaultVillainRangePercent;
    this.selectedHeroRange = null;
    this.selectedHandCode = null;

    if (!this.selectedProfileId) {
      this.heroRanges = [];
      this.villainRanges = [];
      return;
    }

    this.rangesService.listHero(this.selectedProfileId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((ranges) => {
        this.heroRanges = ranges;
        if (ranges.length > 0) {
          this.onHeroRangeChange(ranges[0].id);
        } else {
          this.cdr.markForCheck();
        }
      });
    this.rangesService.listVillain(this.selectedProfileId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((ranges) => {
        this.villainRanges = ranges;
        this.cdr.markForCheck();
      });
  }

  onHeroRangeChange(heroRangeId: string): void {
    this.selectedHeroRangeId = heroRangeId;
    this.rangesService.getHero(heroRangeId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((range) => {
        this.selectedHeroRange = range;
        this.selectedHandCode = range.cells.find((cell) => cell.enabled && !!cell.legendCode)?.handCode ?? null;
        this.cdr.markForCheck();
      });
  }

  onVillainRangeChange(villainRangeSetId: string | null): void {
    this.selectedVillainRangeSetId = villainRangeSetId;
    this.villainInputMode = 'RANGE';
  }

  useVillainPercentMode(): void {
    this.villainInputMode = 'PERCENT';
    this.selectedVillainRangeSetId = null;
  }

  useVillainRangeMode(): void {
    this.villainInputMode = 'RANGE';
    if (!this.selectedVillainRangeSetId && this.villainRanges.length > 0) {
      this.selectedVillainRangeSetId = this.villainRanges[0].id;
    }
  }

  onVillainRangePercentChange(value: number | string): void {
    const parsedValue = Number(value);
    if (Number.isNaN(parsedValue)) {
      return;
    }
    this.villainRangePercent = Math.max(1, Math.min(100, Math.round(parsedValue)));
  }

  onSelectHandCode(handCode: string): void {
    this.selectedHandCode = handCode;
  }

  clearBoardCards(): void {
    this.boardCards = '';
  }

  legendFor(handCode: string): StrategyLegend | null {
    return this.selectedHeroRange?.cells.find((entry) => entry.handCode === handCode)?.legendCode ?? null;
  }

  displayHandCode(handCode: string): string {
    return formatHandCode(handCode);
  }

  resolveCellClass(handCode: string): string {
    const cell = this.selectedHeroRange?.cells.find((entry) => entry.handCode === handCode);
    const accent = cell?.colorCode || null;
    const baseClass = ['matrix-cell'];

    if (this.selectedHandCode === handCode) {
      baseClass.push('matrix-cell--selected');
    } else if (this.activeHeroHandCodes.has(handCode)) {
      baseClass.push('matrix-cell--enabled');
    }

    if (accent) {
      baseClass.push('matrix-cell--custom');
    }

    return baseClass.join(' ');
  }

  resolveAccentColor(handCode: string): string | null {
    return this.selectedHeroRange?.cells.find((entry) => entry.handCode === handCode)?.colorCode ?? null;
  }

  private selectedVillainRange(): VillainRangeSummary | null {
    return this.villainRanges.find((range) => range.id === this.selectedVillainRangeSetId) ?? null;
  }

  private splitCsv(value: string): string[] {
    return (value ?? '')
      .split(/[, ]+/)
      .map((entry) => entry.trim())
      .filter((entry) => entry.length > 0);
  }

  private parseActionSequence(raw: string): ActionEvent[] {
    const lines = raw
      .split('\n')
      .map((line) => line.trim())
      .filter((line) => line.length > 0);

    return lines
      .map((line, index) => this.toActionEvent(line, index))
      .filter((event): event is ActionEvent => event !== null);
  }

  private toActionEvent(line: string, index: number): ActionEvent | null {
    const [streetRaw, actorRaw, actionRaw, sizingRaw] = line.split(':').map((part) => part.trim().toUpperCase());
    if (!streetRaw || !actorRaw || !actionRaw) {
      return null;
    }

    if (!Object.values(Street).includes(streetRaw as Street)) {
      return null;
    }
    if (!Object.values(ActorType).includes(actorRaw as ActorType)) {
      return null;
    }
    if (!Object.values(ActionType).includes(actionRaw as ActionType)) {
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
}

