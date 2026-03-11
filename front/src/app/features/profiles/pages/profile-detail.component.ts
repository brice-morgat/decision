import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, of, switchMap } from 'rxjs';
import {
  StrategicProfileDetail,
  StrategyProfileUpsertPayload
} from '../../../core/models/profile.models';
import { ProfilesService } from '../../../core/services/profiles.service';

@Component({
  selector: 'app-profile-detail',
  templateUrl: './profile-detail.component.html',
  styleUrls: ['./profile-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfileDetailComponent {
  readonly profileId = this.route.snapshot.paramMap.get('id');
  readonly isCreateMode = !this.profileId;
  readonly profile$: Observable<StrategicProfileDetail | null> = this.isCreateMode
    ? of(null)
    : this.route.paramMap.pipe(
        switchMap((params) => this.profilesService.getById(params.get('id') || ''))
      );

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly profilesService: ProfilesService
  ) {}

  saveProfile(payload: StrategyProfileUpsertPayload): void {
    const request$ = this.isCreateMode
      ? this.profilesService.create(payload)
      : this.profilesService.update(this.profileId!, payload);

    request$.subscribe((profile) => {
      void this.router.navigate(['/profiles', profile.id]);
    });
  }

  deleteProfile(): void {
    if (this.isCreateMode || !window.confirm('Supprimer ce profil ?')) {
      return;
    }

    this.profilesService.delete(this.profileId!).subscribe(() => {
      void this.router.navigate(['/profiles']);
    });
  }

  activateProfile(): void {
    if (this.isCreateMode) {
      return;
    }

    this.profilesService.activate(this.profileId!).subscribe(() => {
      void this.router.navigate(['/profiles', this.profileId!]);
    });
  }
}
