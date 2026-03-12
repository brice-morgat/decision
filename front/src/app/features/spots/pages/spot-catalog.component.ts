import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { BehaviorSubject, switchMap } from 'rxjs';
import { SpotFacadeService } from '../services/spot-facade.service';

@Component({
  selector: 'app-spot-catalog',
  templateUrl: './spot-catalog.component.html',
  styleUrls: ['./spot-catalog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpotCatalogComponent {
  private readonly selectedProfileId$ = new BehaviorSubject<string | null>(null);

  readonly profileControl = new FormControl<string | null>(null);
  readonly vm$ = this.selectedProfileId$.pipe(
    switchMap((profileId) => this.spotFacade.load(profileId))
  );

  constructor(
    private readonly router: Router,
    private readonly spotFacade: SpotFacadeService
  ) {
    this.profileControl.valueChanges.subscribe((profileId) => {
      this.selectedProfileId$.next(profileId);
    });
  }

  openHeroAdvanced(rangeId: string): void {
    void this.router.navigate(['/ranges/hero'], { queryParams: { rangeId } });
  }

  openVillainAdvanced(rangeId: string): void {
    void this.router.navigate(['/ranges/villain'], { queryParams: { rangeId } });
  }

  openAssistant(): void {
    void this.router.navigate(['/assistant']);
  }
}
