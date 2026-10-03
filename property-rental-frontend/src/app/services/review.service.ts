import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type Review = {
  id: number;
  propertyId: number;
  propertyTitle: string;
  bookingId: number;
  userName: string;
  rating: number;
  comment: string;
  ownerResponse: string | null;
  reviewDate: string;
  approved: boolean;
};

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private readonly apiBaseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getMyReviews(): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiBaseUrl}/reviews/my-reviews`);
  }

  getPropertyReviews(propertyId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiBaseUrl}/reviews/property/${propertyId}`);
  }

  getOwnerReviews(): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiBaseUrl}/owner/reviews`);
  }

  respondToReview(reviewId: number, response: string): Observable<Review> {
    return this.http.put<Review>(`${this.apiBaseUrl}/owner/reviews/${reviewId}/response`, { response });
  }

  createReview(bookingId: number, rating: number, comment: string): Observable<Review> {
    return this.http.post<Review>(`${this.apiBaseUrl}/reviews`, { bookingId, rating, comment });
  }
}
