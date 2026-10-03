import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Review, ReviewService } from '../../services/review.service';

@Component({
  selector: 'app-owner-reviews',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <main class="page">
      <a routerLink="/owner" class="back-link">← Owner workspace</a>
      <header>
        <span class="eyebrow">PROPERTY FEEDBACK</span>
        <h1>Guest reviews</h1>
        <p>Read feedback for your properties and respond to guests.</p>
      </header>

      <p class="error" *ngIf="error">{{ error }}</p>
      <p class="success" *ngIf="success">{{ success }}</p>
      <p class="state" *ngIf="loading">Loading reviews…</p>
      <p class="state" *ngIf="!loading && reviews.length === 0">There are no reviews for your properties yet.</p>

      <article class="review-card" *ngFor="let review of reviews">
        <div class="review-heading">
          <div>
            <h2>{{ review.propertyTitle }}</h2>
            <p>By {{ review.userName }} · {{ review.reviewDate }}</p>
          </div>
          <strong class="score">★ {{ review.rating }} / 5</strong>
        </div>
        <p class="comment">{{ review.comment }}</p>
        <form (ngSubmit)="saveResponse(review)">
          <label [for]="'response-' + review.id">{{ review.ownerResponse ? 'Your response' : 'Respond to this review' }}</label>
          <textarea
            [id]="'response-' + review.id"
            [name]="'response-' + review.id"
            [(ngModel)]="responses[review.id]"
            rows="3"
            maxlength="2000"
            required
            placeholder="Write a response to your guest."
          ></textarea>
          <button type="submit" [disabled]="savingReviewId === review.id">
            {{ savingReviewId === review.id ? 'Saving…' : review.ownerResponse ? 'Update response' : 'Post response' }}
          </button>
        </form>
      </article>
    </main>
  `,
  styles: [`
    .page{max-width:900px;margin:auto;padding:32px 20px 56px;color:#172033}
    .back-link{display:inline-block;margin-bottom:18px;color:#176b70;font-weight:700;text-decoration:none}
    header{margin-bottom:24px;padding:24px;border-radius:18px;background:#153b4b;color:#fff}
    .eyebrow{font-size:11px;font-weight:800;letter-spacing:.12em}
    h1{margin:6px 0;font-size:30px}
    header p{margin:0;opacity:.85}
    .review-card{margin:14px 0;padding:20px;border:1px solid #e2e8f0;border-radius:14px;background:#fff}
    .review-heading{display:flex;justify-content:space-between;gap:12px;align-items:flex-start}
    .review-heading h2{margin:0;font-size:19px}
    .review-heading p{margin:5px 0;color:#64748b;font-size:13px}
    .score{color:#8a641e;white-space:nowrap}
    .comment{margin:14px 0;color:#334155;line-height:1.6;white-space:pre-wrap}
    form{display:grid;gap:9px;padding-top:12px;border-top:1px solid #e2e8f0}
    label{font-size:13px;font-weight:750}
    textarea{width:100%;box-sizing:border-box;padding:10px 12px;border:1px solid #cbd5e1;border-radius:9px;font:inherit;resize:vertical}
    button{justify-self:start;border:0;border-radius:9px;padding:10px 14px;background:#146b75;color:#fff;font-weight:750;cursor:pointer}
    button:disabled{opacity:.6;cursor:wait}
    .state{padding:18px;color:#64748b}
    .error,.success{padding:12px;border-radius:9px}
    .error{color:#b42318;background:#fef2f2}
    .success{color:#166534;background:#f0fdf4}
    @media(max-width:560px){.review-heading{flex-direction:column}}
  `],
})
export class OwnerReviewsComponent implements OnInit {
  reviews: Review[] = [];
  responses: Record<number, string> = {};
  loading = true;
  error = '';
  success = '';
  savingReviewId: number | null = null;

  constructor(private reviewService: ReviewService) {}

  ngOnInit() {
    this.reviewService.getOwnerReviews().subscribe({
      next: reviews => {
        this.reviews = reviews;
        for (const review of reviews) this.responses[review.id] = review.ownerResponse ?? '';
        this.loading = false;
      },
      error: err => {
        this.error = err?.error?.message ?? 'Unable to load your property reviews.';
        this.loading = false;
      },
    });
  }

  saveResponse(review: Review) {
    const response = this.responses[review.id]?.trim();
    if (!response || this.savingReviewId !== null) return;

    this.error = '';
    this.success = '';
    this.savingReviewId = review.id;
    this.reviewService.respondToReview(review.id, response).subscribe({
      next: updated => {
        this.reviews = this.reviews.map(item => item.id === updated.id ? updated : item);
        this.responses[updated.id] = updated.ownerResponse ?? '';
        this.savingReviewId = null;
        this.success = `Your response to the review for ${updated.propertyTitle} was saved.`;
      },
      error: err => {
        this.error = err?.error?.message ?? 'Unable to save your response.';
        this.savingReviewId = null;
      },
    });
  }
}
