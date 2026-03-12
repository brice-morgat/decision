import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, finalize, take } from 'rxjs';
import { DecisionRequest, DecisionResult } from '../../../core/models/decision.models';
import { DecisionEngineService } from '../../../core/services/decision-engine.service';

/**
 * Feature facade to decouple UI actions from the decision engine service.
 */
@Injectable()
export class DecisionAssistantFacade {
  private readonly resultSubject = new BehaviorSubject<DecisionResult | null>(null);
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  private readonly errorSubject = new BehaviorSubject<string | null>(null);

  readonly result$: Observable<DecisionResult | null> = this.resultSubject.asObservable();
  readonly loading$: Observable<boolean> = this.loadingSubject.asObservable();
  readonly error$: Observable<string | null> = this.errorSubject.asObservable();

  constructor(private readonly decisionEngine: DecisionEngineService) {}

  submit(request: DecisionRequest): void {
    this.errorSubject.next(null);
    this.loadingSubject.next(true);

    this.decisionEngine.evaluate(request)
      .pipe(
        take(1),
        finalize(() => this.loadingSubject.next(false))
      )
      .subscribe({
        next: (result) => this.resultSubject.next(result),
        error: (error: unknown) => {
          this.resultSubject.next(null);
          this.errorSubject.next(this.toErrorMessage(error));
        }
      });
  }

  private toErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const response = error.error as {
        message?: string;
        details?: Array<{ field?: string; message?: string }>;
      };

      const details = (response?.details ?? [])
        .map((detail) => `${detail.field ?? 'field'}: ${detail.message ?? 'invalid'}`)
        .join(' · ');

      if (details.length > 0) {
        return `${response?.message ?? 'Request failed'} (${details})`;
      }

      return response?.message ?? 'Request failed';
    }

    return 'Erreur inattendue pendant le calcul de decision';
  }
}
