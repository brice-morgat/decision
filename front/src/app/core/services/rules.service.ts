import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DecisionRuleDetail,
  DecisionRuleFilters,
  DecisionRuleSummary,
  DecisionRuleUpsertPayload,
  DuplicateDecisionRulePayload
} from '../models/rule.models';
import { ApiHttpService } from './api-http.service';

@Injectable({ providedIn: 'root' })
export class RulesService {
  constructor(private readonly api: ApiHttpService) {}

  list(filters?: DecisionRuleFilters): Observable<DecisionRuleSummary[]> {
    return this.api.get<DecisionRuleSummary[]>('/rules', this.cleanParams(filters));
  }

  getById(id: string): Observable<DecisionRuleDetail> {
    return this.api.get<DecisionRuleDetail>(`/rules/${id}`);
  }

  create(payload: DecisionRuleUpsertPayload): Observable<DecisionRuleDetail> {
    return this.api.post<DecisionRuleDetail>('/rules', payload);
  }

  update(id: string, payload: DecisionRuleUpsertPayload): Observable<DecisionRuleDetail> {
    return this.api.put<DecisionRuleDetail>(`/rules/${id}`, payload);
  }

  duplicate(id: string, payload: DuplicateDecisionRulePayload): Observable<DecisionRuleDetail> {
    return this.api.post<DecisionRuleDetail>(`/rules/${id}/duplicate`, payload);
  }

  delete(id: string): Observable<void> {
    return this.api.delete<void>(`/rules/${id}`);
  }

  private cleanParams<T extends object | undefined>(params: T): Record<string, string | number | boolean> {
    return Object.entries((params ?? {}) as Record<string, string | number | boolean | null | undefined>).reduce<Record<string, string | number | boolean>>((acc, [key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        acc[key] = value;
      }
      return acc;
    }, {});
  }
}
