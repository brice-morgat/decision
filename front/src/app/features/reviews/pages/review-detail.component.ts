import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, switchMap } from 'rxjs';
import { ReviewDetail } from '../../../core/models/review.models';
import { ReviewsService } from '../../../core/services/reviews.service';

@Component({
  selector: 'app-review-detail',
  templateUrl: './review-detail.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReviewDetailComponent {
  readonly review$: Observable<ReviewDetail> = this.route.paramMap.pipe(
    switchMap((params) => this.reviewsService.getById(params.get('id') || ''))
  );

  constructor(
    private readonly route: ActivatedRoute,
    private readonly reviewsService: ReviewsService
  ) {}
}