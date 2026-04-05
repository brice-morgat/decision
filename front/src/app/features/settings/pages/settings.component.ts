import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, Validators } from '@angular/forms';
import { AppSettings } from '../../../core/models/settings.models';
import { SettingsService } from '../../../core/services/settings.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SettingsComponent {
  private readonly destroyRef = inject(DestroyRef);

  readonly form = this.fb.group({
    defaultProfileId: [''],
    autoSave: [true, Validators.required],
    telemetryEnabled: [false, Validators.required]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly settingsService: SettingsService,
    private readonly cdr: ChangeDetectorRef
  ) {
    this.settingsService.load()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((settings) => {
        this.form.patchValue(settings);
        this.cdr.markForCheck();
      });
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
    this.settingsService.update(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe();
  }
}
