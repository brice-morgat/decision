import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AppSettings } from '../models/settings.models';
import { ApiHttpService } from './api-http.service';

@Injectable({ providedIn: 'root' })
export class SettingsService {
  constructor(private readonly api: ApiHttpService) {}

  load(): Observable<AppSettings> {
    return this.api.get<AppSettings>('/settings');
  }

  update(settings: AppSettings): Observable<AppSettings> {
    return this.api.put<AppSettings>('/settings', settings);
  }
}