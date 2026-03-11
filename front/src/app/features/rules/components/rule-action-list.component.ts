import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { AbstractControl, FormArray, FormGroup } from '@angular/forms';
import { ACTION_TYPE_OPTIONS, SizingType } from '../../../core/models/referentials';

@Component({
  selector: 'app-rule-action-list',
  templateUrl: './rule-action-list.component.html',
  styleUrls: ['./rule-action-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RuleActionListComponent {
  @Input({ required: true }) actions!: FormArray;
  @Output() addAction = new EventEmitter<void>();
  @Output() removeAction = new EventEmitter<number>();

  readonly actionTypes = ACTION_TYPE_OPTIONS;
  readonly sizingTypes = Object.values(SizingType);

  asFormGroup(control: AbstractControl): FormGroup {
    return control as FormGroup;
  }

  requiresSizingValue(control: AbstractControl): boolean {
    const sizingType = this.asFormGroup(control).get('sizingType')?.value as SizingType;
    return sizingType !== SizingType.ALL_IN && sizingType !== SizingType.CATEGORY;
  }

  trackByIndex(index: number): number {
    return index;
  }
}
