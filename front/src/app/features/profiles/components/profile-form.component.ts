import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import {
  StrategicProfileDetail,
  StrategyProfileUpsertPayload
} from '../../../core/models/profile.models';
import { GameType } from '../../../core/models/referentials';

@Component({
  selector: 'app-profile-form',
  templateUrl: './profile-form.component.html',
  styleUrls: ['./profile-form.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfileFormComponent {
  readonly gameTypes = Object.values(GameType);

  @Input() set profile(value: StrategicProfileDetail | null) {
    if (value) {
      this.form.patchValue({
        name: value.name,
        description: value.description,
        gameType: value.gameType,
        active: value.active,
        archived: value.archived,
        versionLabel: value.versionLabel
      });
      return;
    }

    this.form.reset({
      name: '',
      description: '',
      gameType: GameType.CASH,
      active: false,
      archived: false,
      versionLabel: 'v1'
    });
  }

  @Output() saveProfile = new EventEmitter<StrategyProfileUpsertPayload>();

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', [Validators.maxLength(1000)]],
    gameType: [GameType.CASH, Validators.required],
    active: [false],
    archived: [false],
    versionLabel: ['v1', [Validators.required, Validators.maxLength(32)]]
  });

  constructor(private readonly fb: FormBuilder) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saveProfile.emit(this.form.getRawValue());
  }
}
