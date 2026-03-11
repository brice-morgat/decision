import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, switchMap } from 'rxjs';
import { StrategicProfileSummary } from '../../../core/models/profile.models';
import { ProfilesService } from '../../../core/services/profiles.service';

@Component({
  selector: 'app-profiles-list',
  templateUrl: './profiles-list.component.html',
  styleUrls: ['./profiles-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfilesListComponent {
  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  readonly profiles$: Observable<StrategicProfileSummary[]> = this.refresh$.pipe(
    switchMap(() => this.profilesService.list())
  );

  constructor(
    private readonly profilesService: ProfilesService,
    private readonly router: Router
  ) {}

  createProfile(): void {
    void this.router.navigate(['/profiles/new']);
  }

  editProfile(profileId: string): void {
    void this.router.navigate(['/profiles', profileId]);
  }

  activateProfile(profileId: string): void {
    this.profilesService.activate(profileId).subscribe(() => this.refresh$.next());
  }

  duplicateProfile(profile: StrategicProfileSummary): void {
    const name = `${profile.name} Copy`;
    this.profilesService.duplicate(profile.id, { name, activateCopy: false })
      .subscribe(() => this.refresh$.next());
  }

  deleteProfile(profile: StrategicProfileSummary): void {
    if (!window.confirm(`Supprimer le profil ${profile.name} ?`)) {
      return;
    }

    this.profilesService.delete(profile.id).subscribe(() => this.refresh$.next());
  }

  trackById(_: number, profile: StrategicProfileSummary): string {
    return profile.id;
  }
}
