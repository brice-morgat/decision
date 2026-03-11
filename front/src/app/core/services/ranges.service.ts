import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  HeroRangeCellsUpdatePayload,
  HeroRangeContext,
  HeroRangeDetail,
  HeroRangeSummary,
  HeroRangeUpsertPayload,
  VillainRangeCellsUpdatePayload,
  VillainRangeContext,
  VillainRangeDetail,
  VillainRangeSummary,
  VillainRangeUpsertPayload
} from '../models/range.models';
import { ApiHttpService } from './api-http.service';

/**
 * Provides typed access to hero and villain range modules.
 */
@Injectable({ providedIn: 'root' })
export class RangesService {
  constructor(private readonly api: ApiHttpService) {}

  listHero(profileId: string): Observable<HeroRangeSummary[]> {
    return this.api.get<HeroRangeSummary[]>('/ranges/hero', { profileId });
  }

  getHero(id: string): Observable<HeroRangeDetail> {
    return this.api.get<HeroRangeDetail>(`/ranges/hero/${id}`);
  }

  getHeroByContext(context: HeroRangeContext): Observable<HeroRangeDetail> {
    return this.api.get<HeroRangeDetail>('/ranges/hero/context', this.cleanParams(context));
  }

  createHero(payload: HeroRangeUpsertPayload): Observable<HeroRangeDetail> {
    return this.api.post<HeroRangeDetail>('/ranges/hero', payload);
  }

  updateHero(id: string, payload: HeroRangeUpsertPayload): Observable<HeroRangeDetail> {
    return this.api.put<HeroRangeDetail>(`/ranges/hero/${id}`, payload);
  }

  updateHeroCells(id: string, payload: HeroRangeCellsUpdatePayload): Observable<HeroRangeDetail> {
    return this.api.put<HeroRangeDetail>(`/ranges/hero/${id}/cells`, payload);
  }

  deleteHero(id: string): Observable<void> {
    return this.api.delete<void>(`/ranges/hero/${id}`);
  }

  listVillain(profileId: string): Observable<VillainRangeSummary[]> {
    return this.api.get<VillainRangeSummary[]>('/ranges/villain', { profileId });
  }

  getVillain(id: string): Observable<VillainRangeDetail> {
    return this.api.get<VillainRangeDetail>(`/ranges/villain/${id}`);
  }

  getVillainByContext(context: VillainRangeContext): Observable<VillainRangeDetail> {
    return this.api.get<VillainRangeDetail>('/ranges/villain/context', this.cleanParams(context));
  }

  createVillain(payload: VillainRangeUpsertPayload): Observable<VillainRangeDetail> {
    return this.api.post<VillainRangeDetail>('/ranges/villain', payload);
  }

  updateVillain(id: string, payload: VillainRangeUpsertPayload): Observable<VillainRangeDetail> {
    return this.api.put<VillainRangeDetail>(`/ranges/villain/${id}`, payload);
  }

  updateVillainCells(id: string, payload: VillainRangeCellsUpdatePayload): Observable<VillainRangeDetail> {
    return this.api.put<VillainRangeDetail>(`/ranges/villain/${id}/cells`, payload);
  }

  deleteVillain(id: string): Observable<void> {
    return this.api.delete<void>(`/ranges/villain/${id}`);
  }

  private cleanParams<T extends object>(params: T): Record<string, string | number | boolean> {
    return Object.entries(params as Record<string, string | number | boolean | null | undefined>).reduce<Record<string, string | number | boolean>>((acc, [key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        acc[key] = value;
      }
      return acc;
    }, {});
  }
}
