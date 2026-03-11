import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { StrategicProfileSummary } from '../../../core/models/profile.models';
import {
  GAME_TYPE_OPTIONS,
  PLAYER_POSITION_OPTIONS,
  SCENARIO_TYPE_OPTIONS,
  STREET_OPTIONS
} from '../../../core/models/referentials';

@Component({
  selector: 'app-rule-filters',
  templateUrl: './rule-filters.component.html',
  styleUrls: ['./rule-filters.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RuleFiltersComponent {
  @Input({ required: true }) form!: FormGroup;
  @Input() profiles: StrategicProfileSummary[] = [];

  @Output() applyFilters = new EventEmitter<void>();
  @Output() resetFilters = new EventEmitter<void>();
  @Output() createRule = new EventEmitter<void>();

  readonly gameTypes = GAME_TYPE_OPTIONS;
  readonly streets = STREET_OPTIONS;
  readonly scenarioTypes = SCENARIO_TYPE_OPTIONS;
  readonly positions = PLAYER_POSITION_OPTIONS;
}
