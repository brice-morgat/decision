import { ChangeDetectionStrategy, Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DecisionRequest } from '../../../core/models/decision.models';
import { ProfilesService } from '../../../core/services/profiles.service';
import { PlayerPosition, ScenarioType, Street } from '../../../core/models/referentials';

@Component({
  selector: 'app-decision-context-form',
  templateUrl: './decision-context-form.component.html',
  styleUrls: ['./decision-context-form.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DecisionContextFormComponent {
  readonly positions = Object.values(PlayerPosition);
  readonly scenarioTypes = Object.values(ScenarioType);
  readonly streets = Object.values(Street);
  readonly profiles$ = this.profilesService.list();

  @Output() submitDecision = new EventEmitter<DecisionRequest>();

  readonly form = this.fb.group({
    strategyProfileId: ['', Validators.required],
    heroPosition: [PlayerPosition.BTN, Validators.required],
    villainPosition: [PlayerPosition.BB, Validators.required],
    scenarioType: [ScenarioType.OPEN_FIRST_IN, Validators.required],
    street: [Street.PREFLOP, Validators.required],
    effectiveStackInBigBlinds: [100, [Validators.required, Validators.min(0)]],
    potSizeInBigBlinds: [2.5, [Validators.required, Validators.min(0)]],
    heroCards: ['As,Kh', Validators.required],
    boardCards: ['']
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly profilesService: ProfilesService
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.submitDecision.emit({
      strategyProfileId: value.strategyProfileId!,
      heroPosition: value.heroPosition!,
      villainPosition: value.villainPosition!,
      scenarioType: value.scenarioType!,
      street: value.street!,
      effectiveStackInBigBlinds: Number(value.effectiveStackInBigBlinds),
      potSizeInBigBlinds: Number(value.potSizeInBigBlinds),
      heroCards: this.splitCsv(value.heroCards),
      boardCards: this.splitCsv(value.boardCards),
      actionEvents: []
    });
  }

  private splitCsv(value: string | null | undefined): string[] {
    return (value ?? '')
      .split(',')
      .map((entry) => entry.trim())
      .filter((entry) => entry.length > 0);
  }
}
