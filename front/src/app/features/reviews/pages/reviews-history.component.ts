import { ChangeDetectionStrategy, Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import { BehaviorSubject, Observable, take } from 'rxjs';
import { ActionType, DecisionStatus } from '../../../core/models/referentials';
import { CreateReviewPayload, ReviewIssue, ReviewSummary } from '../../../core/models/review.models';
import { ProfilesService } from '../../../core/services/profiles.service';
import { ReviewsService } from '../../../core/services/reviews.service';

@Component({
  selector: 'app-reviews-history',
  templateUrl: './reviews-history.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReviewsHistoryComponent {
  readonly reviews$ = new BehaviorSubject<ReviewSummary[]>([]);
  readonly loading$ = new BehaviorSubject<boolean>(false);
  readonly error$ = new BehaviorSubject<string | null>(null);
  readonly profiles$ = this.profilesService.list();
  readonly actionTypes = Object.values(ActionType);
  readonly decisionStatuses = Object.values(DecisionStatus);
  readonly issues: ReviewIssue[] = ['WIN', 'LOSS', 'BREAKEVEN'];

  readonly form = this.fb.group({
    strategyProfileId: this.fb.control('', [Validators.required]),
    heroHandCode: this.fb.control('', [Validators.required]),
    boardCards: this.fb.control(''),
    decisionStatus: this.fb.control(DecisionStatus.SUCCESS, [Validators.required]),
    recommendedAction: this.fb.control(ActionType.CALL, [Validators.required]),
    actualAction: this.fb.control(ActionType.CALL, [Validators.required]),
    netResultInBigBlinds: this.fb.control(0, [Validators.required]),
    issue: this.fb.control<ReviewIssue>('BREAKEVEN', [Validators.required]),
    situationSummary: this.fb.control(''),
    engineExplanation: this.fb.control(''),
    reviewNote: this.fb.control('')
  });

  constructor(
    private readonly reviewsService: ReviewsService,
    private readonly profilesService: ProfilesService,
    private readonly fb: NonNullableFormBuilder
  ) {
    this.reload();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.error$.next(null);
    this.loading$.next(true);
    const value = this.form.getRawValue();

    const payload: CreateReviewPayload = {
      strategyProfileId: value.strategyProfileId,
      heroHandCode: value.heroHandCode.toUpperCase(),
      boardCards: value.boardCards || undefined,
      decisionStatus: value.decisionStatus,
      recommendedAction: value.recommendedAction,
      actualAction: value.actualAction,
      netResultInBigBlinds: Number(value.netResultInBigBlinds),
      issue: value.issue,
      situationSummary: value.situationSummary || undefined,
      engineExplanation: value.engineExplanation || undefined,
      reviewNote: value.reviewNote || undefined
    };

    this.reviewsService.create(payload)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.form.patchValue({
            heroHandCode: '',
            boardCards: '',
            netResultInBigBlinds: 0,
            situationSummary: '',
            engineExplanation: '',
            reviewNote: ''
          });
          this.reload();
        },
        error: (error: unknown) => {
          this.loading$.next(false);
          this.error$.next(this.toErrorMessage(error));
        }
      });
  }

  private reload(): void {
    this.loading$.next(true);
    this.error$.next(null);
    this.reviewsService.list()
      .pipe(take(1))
      .subscribe({
        next: (reviews) => {
          this.reviews$.next(reviews);
          this.loading$.next(false);
        },
        error: (error: unknown) => {
          this.loading$.next(false);
          this.error$.next(this.toErrorMessage(error));
        }
      });
  }

  private toErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return error.error?.message ?? 'Erreur API';
    }
    return 'Erreur inattendue';
  }
}
