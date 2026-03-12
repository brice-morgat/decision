import { Injectable } from '@angular/core';
import { combineLatest, map, Observable, of, switchMap } from 'rxjs';
import { HeroRangeSummary, VillainRangeSummary } from '../../../core/models/range.models';
import { StrategicProfileSummary } from '../../../core/models/profile.models';
import { ProfilesService } from '../../../core/services/profiles.service';
import { RangesService } from '../../../core/services/ranges.service';

export interface SpotCardViewModel {
  label: string;
  side: 'HERO' | 'VILLAIN';
  profileId: string;
  profileName: string;
  rangeId: string;
  rangeName: string;
  gameType: string;
  street: string;
  scenarioType: string;
  heroPosition?: string | null;
  villainPosition?: string | null;
  priority: number;
}

export interface SpotCatalogViewModel {
  profiles: StrategicProfileSummary[];
  selectedProfileId: string | null;
  heroSpots: SpotCardViewModel[];
  villainSpots: SpotCardViewModel[];
}

@Injectable({ providedIn: 'root' })
export class SpotFacadeService {
  constructor(
    private readonly profilesService: ProfilesService,
    private readonly rangesService: RangesService
  ) {}

  load(selectedProfileId: string | null): Observable<SpotCatalogViewModel> {
    return this.profilesService.list().pipe(
      switchMap((profiles) => {
        if (profiles.length === 0) {
          return of({
            profiles: [],
            selectedProfileId: null,
            heroSpots: [],
            villainSpots: []
          } satisfies SpotCatalogViewModel);
        }

        const effectiveProfileId = selectedProfileId || profiles[0].id;
        return combineLatest([
          of(profiles),
          this.rangesService.listHero(effectiveProfileId),
          this.rangesService.listVillain(effectiveProfileId)
        ]).pipe(
          map(([allProfiles, heroRanges, villainRanges]) => ({
            profiles: allProfiles,
            selectedProfileId: effectiveProfileId,
            heroSpots: this.toHeroSpots(allProfiles, heroRanges),
            villainSpots: this.toVillainSpots(allProfiles, villainRanges)
          }))
        );
      })
    );
  }

  private toHeroSpots(profiles: StrategicProfileSummary[], ranges: HeroRangeSummary[]): SpotCardViewModel[] {
    return ranges
      .slice()
      .sort((left, right) => left.priority - right.priority)
      .map((range) => ({
        label: `${range.heroPosition} vs field - ${range.scenarioType}`,
        side: 'HERO',
        profileId: range.profileId,
        profileName: this.resolveProfileName(profiles, range.profileId),
        rangeId: range.id,
        rangeName: range.name,
        gameType: range.gameType,
        street: range.street,
        scenarioType: range.scenarioType,
        heroPosition: range.heroPosition,
        villainPosition: null,
        priority: range.priority
      }));
  }

  private toVillainSpots(profiles: StrategicProfileSummary[], ranges: VillainRangeSummary[]): SpotCardViewModel[] {
    return ranges
      .slice()
      .sort((left, right) => left.priority - right.priority)
      .map((range) => ({
        label: `${range.villainPosition} vs ${range.heroPosition || 'field'} - ${range.scenarioType}`,
        side: 'VILLAIN',
        profileId: range.profileId,
        profileName: this.resolveProfileName(profiles, range.profileId),
        rangeId: range.id,
        rangeName: range.name,
        gameType: range.gameType,
        street: range.street,
        scenarioType: range.scenarioType,
        heroPosition: range.heroPosition,
        villainPosition: range.villainPosition,
        priority: range.priority
      }));
  }

  private resolveProfileName(profiles: StrategicProfileSummary[], profileId: string): string {
    return profiles.find((profile) => profile.id === profileId)?.name || profileId;
  }
}
