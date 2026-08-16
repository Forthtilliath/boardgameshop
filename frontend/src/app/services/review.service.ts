import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import type { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import type { CreateReviewRequest, Review } from '../models/review.model';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getReviews(gameId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.baseUrl}/games/${gameId}/reviews`);
  }

  canReview(gameId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/games/${gameId}/reviews/can-review`);
  }

  createReview(gameId: number, request: CreateReviewRequest): Observable<Review> {
    return this.http.post<Review>(`${this.baseUrl}/games/${gameId}/reviews`, request);
  }
}
