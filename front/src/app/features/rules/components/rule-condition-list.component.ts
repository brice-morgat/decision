import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { AbstractControl, FormArray, FormGroup } from '@angular/forms';
import {
  ConditionValueType,
  RuleConditionType,
  RuleOperator,
  RULE_OPERATORS_BY_VALUE_TYPE,
  RULE_VALUE_TYPE_BY_CONDITION
} from '../../../core/models/rule.models';
import {
  ACTION_TYPE_OPTIONS,
  PLAYER_POSITION_OPTIONS,
  SCENARIO_TYPE_OPTIONS,
  STREET_OPTIONS,
  STRATEGY_LEGEND_OPTIONS
} from '../../../core/models/referentials';

@Component({
  selector: 'app-rule-condition-list',
  templateUrl: './rule-condition-list.component.html',
  styleUrls: ['./rule-condition-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RuleConditionListComponent {
  @Input({ required: true }) conditions!: FormArray;
  @Output() addCondition = new EventEmitter<void>();
  @Output() removeCondition = new EventEmitter<number>();

  readonly booleanChoices = [
    { value: 'true', label: 'Vrai' },
    { value: 'false', label: 'Faux' }
  ];
  readonly conditionTypes = Object.values(RuleConditionType);
  readonly positions = PLAYER_POSITION_OPTIONS;
  readonly streets = STREET_OPTIONS;
  readonly scenarioTypes = SCENARIO_TYPE_OPTIONS;
  readonly legends = STRATEGY_LEGEND_OPTIONS;
  readonly actionTypes = ACTION_TYPE_OPTIONS;
  readonly currentActorOptions = ['HERO', 'VILLAIN'];

  asFormGroup(control: AbstractControl): FormGroup {
    return control as FormGroup;
  }

  operatorsFor(control: AbstractControl): RuleOperator[] {
    const valueType = this.asFormGroup(control).get('valueType')?.value as ConditionValueType;
    return RULE_OPERATORS_BY_VALUE_TYPE[valueType] ?? [RuleOperator.EQUALS];
  }

  onConditionTypeChange(index: number): void {
    const group = this.asFormGroup(this.conditions.at(index));
    const conditionType = group.get('conditionType')?.value as RuleConditionType;
    const valueType = RULE_VALUE_TYPE_BY_CONDITION[conditionType];
    const operator = (RULE_OPERATORS_BY_VALUE_TYPE[valueType] ?? [RuleOperator.EQUALS])[0];

    group.patchValue(
      {
        valueType,
        operator,
        expectedValue: this.defaultValueFor(valueType, conditionType),
        secondaryValue: null
      },
      { emitEvent: false }
    );
  }

  shouldShowSecondaryValue(control: AbstractControl): boolean {
    return this.asFormGroup(control).get('operator')?.value === RuleOperator.BETWEEN;
  }

  expectedValueOptions(control: AbstractControl): Array<{ value: string; label: string }> | null {
    const group = this.asFormGroup(control);
    const conditionType = group.get('conditionType')?.value as RuleConditionType;
    const valueType = group.get('valueType')?.value as ConditionValueType;

    if (valueType === ConditionValueType.BOOLEAN) {
      return this.booleanChoices;
    }

    if (conditionType === RuleConditionType.HERO_POSITION || conditionType === RuleConditionType.VILLAIN_POSITION) {
      return this.positions.map((value) => ({ value, label: value }));
    }

    if (conditionType === RuleConditionType.STREET) {
      return this.streets.map((value) => ({ value, label: value }));
    }

    if (conditionType === RuleConditionType.SCENARIO_TYPE) {
      return this.scenarioTypes.map((value) => ({ value, label: value }));
    }

    if (conditionType === RuleConditionType.HERO_STRATEGY_LEGEND) {
      return this.legends.map((value) => ({ value, label: value }));
    }

    if (conditionType === RuleConditionType.FACING_ACTION_TYPE) {
      return this.actionTypes.map((value) => ({ value, label: value }));
    }

    if (conditionType === RuleConditionType.CURRENT_ACTOR) {
      return this.currentActorOptions.map((value) => ({ value, label: value }));
    }

    return null;
  }

  trackByIndex(index: number): number {
    return index;
  }

  private defaultValueFor(valueType: ConditionValueType, conditionType: RuleConditionType): string {
    if (valueType === ConditionValueType.BOOLEAN) {
      return 'true';
    }

    if (conditionType === RuleConditionType.HERO_POSITION || conditionType === RuleConditionType.VILLAIN_POSITION) {
      return this.positions[0];
    }

    if (conditionType === RuleConditionType.STREET) {
      return this.streets[0];
    }

    if (conditionType === RuleConditionType.SCENARIO_TYPE) {
      return this.scenarioTypes[0];
    }

    if (conditionType === RuleConditionType.HERO_STRATEGY_LEGEND) {
      return this.legends[0];
    }

    if (conditionType === RuleConditionType.FACING_ACTION_TYPE) {
      return this.actionTypes[0];
    }

    if (conditionType === RuleConditionType.CURRENT_ACTOR) {
      return this.currentActorOptions[0];
    }

    return '';
  }
}
