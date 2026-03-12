import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DecisionRequest, DecisionResult } from '../models/decision.models';
import { ApiHttpService } from './api-http.service';

/**
 * Orchestrates decision evaluation calls to the backend decision engine.
 */
@Injectable({ providedIn: 'root' })
export class DecisionEngineService {
  constructor(private readonly api: ApiHttpService) {}

  evaluate(request: DecisionRequest): Observable<DecisionResult> {
    return this.api.post<DecisionResult>('/decision', request);
  }
}
