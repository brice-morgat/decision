import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, switchMap } from 'rxjs';
import { DecisionRequest, DecisionResult } from '../../../core/models/decision.models';
import { DecisionEngineService } from '../../../core/services/decision-engine.service';

/**
 * Feature facade to decouple UI actions from the decision engine service.
 */
@Injectable()
export class DecisionAssistantFacade {
  private readonly requestSubject = new BehaviorSubject<DecisionRequest | null>(null);

  readonly result$: Observable<DecisionResult | null> = this.requestSubject.pipe(
    switchMap((request) => {
      if (!request) {
        return of(null);
      }
      return this.decisionEngine.evaluate(request);
    })
  );

  constructor(private readonly decisionEngine: DecisionEngineService) {}

  submit(request: DecisionRequest): void {
    this.requestSubject.next(request);
  }
}
