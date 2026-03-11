import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Observable } from 'rxjs';
import { ReviewSummary } from '../../../core/models/review.models';
import { ReviewsService } from '../../../core/services/reviews.service';

@Component({
  selector: 'app-reviews-history',
  templateUrl: './reviews-history.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReviewsHistoryComponent {
  readonly reviews$: Observable<ReviewSummary[]> = this.reviewsService.list();

  constructor(private readonly reviewsService: ReviewsService) {}
}