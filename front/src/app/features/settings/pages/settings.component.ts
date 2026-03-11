import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { AppSettings } from '../../../core/models/settings.models';
import { SettingsService } from '../../../core/services/settings.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SettingsComponent {
  readonly form = this.fb.group({
    defaultProfileId: [''],
    autoSave: [true, Validators.required],
    telemetryEnabled: [false, Validators.required]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly settingsService: SettingsService
  ) {
    this.settingsService.load().subscribe((settings) => this.form.patchValue(settings));
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const rawValue = this.form.getRawValue();
    const payload: AppSettings = {
      defaultProfileId: rawValue.defaultProfileId || undefined,
      autoSave: rawValue.autoSave ?? true,
      telemetryEnabled: rawValue.telemetryEnabled ?? false
    };
    this.settingsService.update(payload).subscribe();
  }
}
