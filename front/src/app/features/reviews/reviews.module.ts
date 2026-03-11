import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { ReviewDetailComponent } from './pages/review-detail.component';
import { ReviewsHistoryComponent } from './pages/reviews-history.component';

@NgModule({
  declarations: [ReviewsHistoryComponent, ReviewDetailComponent],
  imports: [
    SharedModule,
    RouterModule.forChild([
      { path: '', component: ReviewsHistoryComponent },
      { path: ':id', component: ReviewDetailComponent }
    ])
  ]
})
export class ReviewsModule {}