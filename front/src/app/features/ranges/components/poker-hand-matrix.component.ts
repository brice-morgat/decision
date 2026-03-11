import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { PokerHandMatrixCell } from '../../../core/models/range.models';
import { buildHandMatrixCodes, formatHandCode, HAND_RANKS } from '../utils/poker-hand-codes';

export interface HandSelectionEvent {
  handCode: string;
  additive: boolean;
}

@Component({
  selector: 'app-poker-hand-matrix',
  templateUrl: './poker-hand-matrix.component.html',
  styleUrls: ['./poker-hand-matrix.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PokerHandMatrixComponent {
  @Input() cells: PokerHandMatrixCell[] = [];
  @Input() selectedHandCodes: string[] = [];
  @Input() disabled = false;

  @Output() selectHandCode = new EventEmitter<HandSelectionEvent>();
  @Output() toggleHandCode = new EventEmitter<string>();

  readonly handRanks = HAND_RANKS;
  readonly matrix = buildHandMatrixCodes();

  getCell(handCode: string): PokerHandMatrixCell | undefined {
    return this.cells.find((cell) => cell.handCode === handCode);
  }

  trackByHandCode(_index: number, handCode: string): string {
    return handCode;
  }

  displayHandCode(handCode: string): string {
    return formatHandCode(handCode);
  }

  cellTooltip(handCode: string): string {
    const cell = this.getCell(handCode);
    if (!cell) {
      return this.displayHandCode(handCode);
    }

    const parts = [this.displayHandCode(handCode)];
    if (cell.badge) {
      parts.push(`Label: ${cell.badge}`);
    }
    if (cell.weight != null) {
      parts.push(`Poids: ${cell.weight}%`);
    }
    if (cell.note) {
      parts.push(`Note: ${cell.note}`);
    }
    return parts.join(' - ');
  }

  isSelected(handCode: string): boolean {
    return this.selectedHandCodes.includes(handCode);
  }

  onSelect(event: MouseEvent, handCode: string): void {
    if (this.disabled) {
      return;
    }

    this.selectHandCode.emit({
      handCode,
      additive: event.ctrlKey || event.metaKey || event.shiftKey
    });
  }

  onToggle(event: MouseEvent, handCode: string): void {
    event.stopPropagation();
    if (this.disabled) {
      return;
    }

    this.toggleHandCode.emit(handCode);
  }

  hasMetadata(handCode: string): boolean {
    const cell = this.getCell(handCode);
    return !!cell?.badge || !!cell?.note || cell?.weight != null;
  }
}
