import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReviewDetail, ReviewSummary } from '../models/review.models';
import { ApiHttpService } from './api-http.service';

@Injectable({ providedIn: 'root' })
export class ReviewsService {
  constructor(private readonly api: ApiHttpService) {}

  list(): Observable<ReviewSummary[]> {
    return this.api.get<ReviewSummary[]>('/reviews');
  }

  getById(id: string): Observable<ReviewDetail> {
    return this.api.get<ReviewDetail>(`/reviews/${id}`);
  }
}