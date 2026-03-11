import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { APP_CONFIG } from '../config/app-config';

@Injectable({ providedIn: 'root' })
export class ApiHttpService {
  private readonly baseUrl = APP_CONFIG.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  get<T>(path: string, params?: Record<string, string | number | boolean>) {
    return this.http.get<T>(this.toUrl(path), {
      params: this.toParams(params)
    });
  }

  post<T>(path: string, body: unknown) {
    return this.http.post<T>(this.toUrl(path), body);
  }

  put<T>(path: string, body: unknown) {
    return this.http.put<T>(this.toUrl(path), body);
  }

  delete<T>(path: string) {
    return this.http.delete<T>(this.toUrl(path));
  }

  private toUrl(path: string): string {
    if (path.startsWith('/')) {
      return `${this.baseUrl}${path}`;
    }
    return `${this.baseUrl}/${path}`;
  }

  private toParams(params?: Record<string, string | number | boolean>) {
    if (!params) {
      return undefined;
    }

    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      httpParams = httpParams.set(key, String(value));
    });

    return httpParams;
  }
}