import { ChangeDetectionStrategy, Component } from '@angular/core';
import { DecisionRequest } from '../../../core/models/decision.models';
import { DecisionAssistantFacade } from '../services/decision-assistant.facade';

/**
 * Container page that ties the decision context form to the decision result.
 */
@Component({
  selector: 'app-decision-assistant-page',
  templateUrl: './decision-assistant-page.component.html',
  styleUrls: ['./decision-assistant-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [DecisionAssistantFacade]
})
export class DecisionAssistantPageComponent {
  readonly result$ = this.facade.result$;
  readonly loading$ = this.facade.loading$;
  readonly error$ = this.facade.error$;

  constructor(private readonly facade: DecisionAssistantFacade) {}

  onSubmit(request: DecisionRequest): void {
    this.facade.submit(request);
  }
}
