import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { HeroRangeCellDto } from '../../../core/models/range.models';
import { STRATEGY_LEGEND_OPTIONS, StrategyLegend } from '../../../core/models/referentials';
import { formatHandCode } from '../utils/poker-hand-codes';

@Component({
  selector: 'app-hero-range-cell-editor',
  templateUrl: './hero-range-cell-editor.component.html',
  styleUrls: ['./hero-range-cell-editor.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeroRangeCellEditorComponent {
  @Output() patchCell = new EventEmitter<Partial<HeroRangeCellDto>>();

  readonly legendOptions = STRATEGY_LEGEND_OPTIONS;
  readonly form = this.fb.group({
    enabled: [false, Validators.required],
    legendCode: [null as StrategyLegend | null],
    colorCode: ['#93c5fd'],
    note: ['']
  });

  private currentCells: HeroRangeCellDto[] = [];
  private currentHandCodes: string[] = [];

  constructor(private readonly fb: FormBuilder) {
    this.form.valueChanges.subscribe((value) => {
      if (this.currentHandCodes.length === 0) {
        return;
      }

      this.patchCell.emit({
        enabled: !!value.enabled,
        legendCode: value.legendCode ?? null,
        colorCode: value.colorCode || null,
        note: value.note || null
      });
    });
  }

  @Input() set cells(value: HeroRangeCellDto[]) {
    this.currentCells = value ?? [];
    this.form.patchValue(
      {
        enabled: this.commonBooleanValue('enabled') ?? false,
        legendCode: this.commonLegendCode(),
        colorCode: this.commonStringValue('colorCode') ?? '#93c5fd',
        note: this.commonStringValue('note') ?? ''
      },
      { emitEvent: false }
    );
  }

  @Input() set handCodes(value: string[]) {
    this.currentHandCodes = value ?? [];
  }

  get title(): string {
    return this.currentHandCodes.length > 1 ? 'Edition groupe hero' : 'Cellule hero';
  }

  get selectedHandLabel(): string {
    if (this.currentHandCodes.length === 0) {
      return 'Selectionnez une main';
    }

    if (this.currentHandCodes.length === 1) {
      return formatHandCode(this.currentHandCodes[0]);
    }

    return `${this.currentHandCodes.length} mains selectionnees`;
  }

  private commonBooleanValue(key: keyof HeroRangeCellDto): boolean | null {
    const values = Array.from(new Set(this.currentCells.map((cell) => cell[key])));
    return values.length === 1 ? Boolean(values[0]) : null;
  }

  private commonLegendCode(): StrategyLegend | null {
    const values = Array.from(new Set(this.currentCells.map((cell) => cell.legendCode ?? null)));
    return values.length === 1 ? (values[0] as StrategyLegend | null) : null;
  }

  private commonStringValue(key: 'colorCode' | 'note'): string | null {
    const values = Array.from(new Set(this.currentCells.map((cell) => cell[key] ?? null)));
    return values.length === 1 ? (values[0] as string | null) : null;
  }
}
