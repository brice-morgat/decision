import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DuplicateStrategyProfilePayload,
  StrategicProfileDetail,
  StrategicProfileSummary,
  StrategyProfileUpsertPayload
} from '../models/profile.models';
import { ApiHttpService } from './api-http.service';

/**
 * Centralizes CRUD and lifecycle operations for strategic profiles.
 */
@Injectable({ providedIn: 'root' })
export class ProfilesService {
  constructor(private readonly api: ApiHttpService) {}

  list(): Observable<StrategicProfileSummary[]> {
    return this.api.get<StrategicProfileSummary[]>('/profiles');
  }

  getActive(): Observable<StrategicProfileDetail> {
    return this.api.get<StrategicProfileDetail>('/profiles/active');
  }

  getById(id: string): Observable<StrategicProfileDetail> {
    return this.api.get<StrategicProfileDetail>(`/profiles/${id}`);
  }

  create(payload: StrategyProfileUpsertPayload): Observable<StrategicProfileDetail> {
    return this.api.post<StrategicProfileDetail>('/profiles', payload);
  }

  update(id: string, payload: StrategyProfileUpsertPayload): Observable<StrategicProfileDetail> {
    return this.api.put<StrategicProfileDetail>(`/profiles/${id}`, payload);
  }

  activate(id: string): Observable<StrategicProfileDetail> {
    return this.api.put<StrategicProfileDetail>(`/profiles/${id}/activate`, {});
  }

  duplicate(id: string, payload: DuplicateStrategyProfilePayload): Observable<StrategicProfileDetail> {
    return this.api.post<StrategicProfileDetail>(`/profiles/${id}/duplicate`, payload);
  }

  delete(id: string): Observable<void> {
    return this.api.delete<void>(`/profiles/${id}`);
  }
}
