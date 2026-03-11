import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { VillainRangeCellDto } from '../../../core/models/range.models';
import { formatHandCode } from '../utils/poker-hand-codes';

@Component({
  selector: 'app-villain-range-cell-editor',
  templateUrl: './villain-range-cell-editor.component.html',
  styleUrls: ['./villain-range-cell-editor.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VillainRangeCellEditorComponent {
  @Output() patchCell = new EventEmitter<Partial<VillainRangeCellDto>>();

  readonly form = this.fb.group({
    enabled: [false, Validators.required],
    weight: [100],
    tagCode: [''],
    note: ['']
  });

  private currentCells: VillainRangeCellDto[] = [];
  private currentHandCodes: string[] = [];

  constructor(private readonly fb: FormBuilder) {
    this.form.valueChanges.subscribe((value) => {
      if (this.currentHandCodes.length === 0) {
        return;
      }

      this.patchCell.emit({
        enabled: !!value.enabled,
        weight: value.weight == null ? null : Number(value.weight),
        tagCode: value.tagCode || null,
        note: value.note || null
      });
    });
  }

  @Input() set cells(value: VillainRangeCellDto[]) {
    this.currentCells = value ?? [];
    this.form.patchValue(
      {
        enabled: this.commonBooleanValue('enabled') ?? false,
        weight: this.commonNumberValue('weight') ?? 100,
        tagCode: this.commonStringValue('tagCode') ?? '',
        note: this.commonStringValue('note') ?? ''
      },
      { emitEvent: false }
    );
  }

  @Input() set handCodes(value: string[]) {
    this.currentHandCodes = value ?? [];
  }

  get title(): string {
    return this.currentHandCodes.length > 1 ? 'Edition groupe vilain' : 'Cellule vilain';
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

  private commonBooleanValue(key: keyof VillainRangeCellDto): boolean | null {
    const values = Array.from(new Set(this.currentCells.map((cell) => cell[key])));
    return values.length === 1 ? Boolean(values[0]) : null;
  }

  private commonNumberValue(key: keyof VillainRangeCellDto): number | null {
    const values = Array.from(new Set(this.currentCells.map((cell) => cell[key] ?? null)));
    return values.length === 1 ? (values[0] as number | null) : null;
  }

  private commonStringValue(key: keyof VillainRangeCellDto): string | null {
    const values = Array.from(new Set(this.currentCells.map((cell) => cell[key] ?? null)));
    return values.length === 1 ? (values[0] as string | null) : null;
  }
}
