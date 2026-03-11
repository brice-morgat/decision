import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { DecisionResult } from '../../../core/models/decision.models';

@Component({
  selector: 'app-decision-result-card',
  templateUrl: './decision-result-card.component.html',
  styleUrls: ['./decision-result-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DecisionResultCardComponent {
  @Input() result: DecisionResult | null = null;
}