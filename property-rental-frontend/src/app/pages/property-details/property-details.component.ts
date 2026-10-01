import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PropertyService, Property } from '../../services/property.service';

@Component({
  selector: 'app-property-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <main class="page" *ngIf="property as listing; else stateTpl">
      <a class="back-link" routerLink="/properties"><span aria-hidden="true">←</span> All properties</a>

      <header class="listing-header">
        <div>
          <h1>{{ listing.title }}</h1>
          <div class="listing-meta">
            <span class="rating" *ngIf="listing.rating > 0"><span aria-hidden="true">★</span> {{ listing.rating | number:'1.1-1' }}</span>
            <span class="new-listing" *ngIf="listing.rating <= 0">New listing</span>
            <span class="meta-dot" aria-hidden="true">·</span>
            <span>{{ listing.location }}</span>
          </div>
        </div>
        <a *ngIf="listing.mapUrl" class="map-link" [href]="listing.mapUrl" target="_blank" rel="noopener noreferrer">View map <span aria-hidden="true">↗</span></a>
      </header>

      <section class="gallery" [class.single-photo]="listing.imageUrls.length === 1" *ngIf="listing.imageUrls?.length; else noPhotos">
        <img class="hero-photo" [src]="listing.imageUrls[0]" [alt]="listing.title" />
        <div class="photo-strip" *ngIf="listing.imageUrls.length > 1">
          <img *ngFor="let image of listing.imageUrls.slice(1, 5)" [src]="image" [alt]="listing.title" />
        </div>
      </section>
      <ng-template #noPhotos>
        <div class="gallery-empty">Photos have not been added to this property yet.</div>
      </ng-template>

      <div class="listing-layout">
        <div class="listing-content">
          <section class="intro section-block">
            <h2>A place to stay in {{ listing.location }}</h2>
            <p class="capacity">Up to {{ listing.maxGuests }} {{ listing.maxGuests === 1 ? 'guest' : 'guests' }}</p>
            <p class="description">{{ listing.description }}</p>
          </section>

          <section class="section-block stay-details">
            <h2>Stay details</h2>
            <div class="detail-grid">
              <div class="detail-item"><span class="detail-icon" aria-hidden="true">⌂</span><div><strong>Maximum guests</strong><span>{{ listing.maxGuests }} {{ listing.maxGuests === 1 ? 'guest' : 'guests' }}</span></div></div>
              <div class="detail-item"><span class="detail-icon" aria-hidden="true">◷</span><div><strong>Check-in</strong><span>{{ listing.checkInTime }}</span></div></div>
              <div class="detail-item"><span class="detail-icon" aria-hidden="true">◴</span><div><strong>Check-out</strong><span>{{ listing.checkOutTime }}</span></div></div>
            </div>
          </section>

          <section class="section-block cancellation">
            <h2>Cancellation policy</h2>
            <p><strong>{{ listing.refundPercentBeforeDeadline }}% refund</strong> when cancelled at least {{ listing.cancellationFreeHours }} hours before check-in.</p>
            <p><strong>{{ listing.refundPercentWithinDeadline }}% refund</strong> for cancellations closer to check-in.</p>
            <p><strong>{{ listing.refundPercentAfterCheckIn }}% refund</strong> after check-in.</p>
          </section>

          <section class="section-block rules" *ngIf="listing.houseRules">
            <h2>House rules</h2>
            <p>{{ listing.houseRules }}</p>
          </section>
        </div>

        <aside class="booking-panel">
          <div class="price-line"><strong>{{ listing.pricePerNight | currency:'INR':'symbol':'1.0-0' }}</strong><span>per night</span></div>
          <div class="panel-rating" *ngIf="listing.rating > 0"><span aria-hidden="true">★</span> {{ listing.rating | number:'1.1-1' }} <span class="rating-caption">guest rating</span></div>
          <a class="book-button" [routerLink]="['/properties', listing.id, 'book']">Reserve this property</a>
          <p class="price-note">You will choose your dates on the next step.</p>
        </aside>
      </div>

      <p *ngIf="error" class="error">{{ error }}</p>
    </main>

    <ng-template #stateTpl>
      <main class="page state-message">
        <p *ngIf="error" class="error">{{ error }}</p>
        <p *ngIf="!error">Loading property...</p>
      </main>
    </ng-template>
  `,
  styles: [
    `
      .page {
        padding: 28px 24px 64px;
        max-width: 1120px;
        margin: 0 auto;
        color: #202b2d;
      }
      .back-link {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 22px;
        color: #24666a;
        font-size: 14px;
        font-weight: 700;
        text-decoration: none;
      }
      .back-link span { font-size: 19px; }
      .listing-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
        gap: 24px;
        margin-bottom: 20px;
      }
      h1 {
        margin: 0;
        color: #172628;
        font-family: Georgia, 'Times New Roman', serif;
        font-size: 34px;
        font-weight: 600;
        line-height: 1.16;
      }
      .listing-meta {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 9px;
        margin-top: 10px;
        color: #566365;
        font-size: 14px;
      }
      .rating, .panel-rating { color: #275d5c; font-weight: 800; }
      .rating span, .panel-rating > span:first-child { color: #bd8b36; }
      .new-listing { color: #386f68; font-weight: 700; }
      .meta-dot { color: #9aa5a4; }
      .map-link {
        flex: 0 0 auto;
        color: #24666a;
        font-size: 14px;
        font-weight: 750;
        text-underline-offset: 4px;
      }
      .gallery {
        display: grid;
        grid-template-columns: minmax(0, 1.55fr) minmax(0, 1fr);
        gap: 8px;
        height: 390px;
        overflow: hidden;
        border-radius: 8px;
        background: #e9efec;
      }
      .gallery.single-photo { grid-template-columns: minmax(0, 1fr); }
      .hero-photo {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      .photo-strip {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        grid-template-rows: repeat(2, minmax(0, 1fr));
        gap: 8px;
      }
      .photo-strip img { width: 100%; height: 100%; min-height: 0; object-fit: cover; }
      .gallery-empty {
        display: grid;
        place-items: center;
        min-height: 240px;
        margin-bottom: 8px;
        border-radius: 8px;
        background: #edf2ef;
        color: #64716e;
        font-size: 14px;
      }
      .listing-layout {
        display: grid;
        grid-template-columns: minmax(0, 1fr) 330px;
        gap: 56px;
        align-items: start;
        margin-top: 30px;
      }
      .listing-content { min-width: 0; }
      .section-block { padding: 24px 0; border-bottom: 1px solid #e3e9e6; }
      .section-block:first-child { padding-top: 0; }
      .section-block h2 {
        margin: 0 0 14px;
        color: #203335;
        font-family: Georgia, 'Times New Roman', serif;
        font-size: 22px;
        font-weight: 600;
      }
      .capacity { margin: -6px 0 16px; color: #667371; font-size: 14px; }
      .description {
        margin: 0;
        color: #465553;
        font-size: 15px;
        line-height: 1.75;
      }
      .detail-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
      .detail-item { display: flex; align-items: center; gap: 12px; min-width: 0; }
      .detail-icon {
        display: grid;
        place-items: center;
        flex: 0 0 38px;
        width: 38px;
        height: 38px;
        border-radius: 50%;
        background: #edf3ef;
        color: #326b65;
        font-size: 20px;
      }
      .detail-item strong, .detail-item div > span { display: block; }
      .detail-item strong { margin-bottom: 4px; color: #344543; font-size: 13px; }
      .detail-item div > span { color: #667371; font-size: 13px; }
      .cancellation p, .rules p { margin: 8px 0; color: #596663; font-size: 14px; line-height: 1.6; }
      .cancellation p strong { color: #344543; }
      .booking-panel {
        position: sticky;
        top: 86px;
        padding: 22px;
        border: 1px solid #dce5e1;
        border-radius: 8px;
        background: #fff;
        box-shadow: 0 8px 24px rgb(28 57 49 / 7%);
      }
      .price-line { display: flex; align-items: baseline; gap: 7px; }
      .price-line strong { color: #1d302f; font-size: 28px; }
      .price-line span, .price-note { color: #687572; font-size: 13px; }
      .panel-rating { margin-top: 13px; font-size: 14px; }
      .rating-caption { color: #697572; font-size: 12px; font-weight: 500; }
      .book-button {
        display: block;
        margin-top: 20px;
        padding: 13px 16px;
        border-radius: 5px;
        background: #176c68;
        color: #fff;
        font-size: 15px;
        font-weight: 750;
        text-align: center;
        text-decoration: none;
        transition: background .18s ease;
      }
      .book-button:hover { background: #115752; }
      .price-note { margin: 12px 0 0; text-align: center; line-height: 1.5; }
      .error { color: #a83232; font-weight: 700; }
      .state-message { min-height: 240px; color: #64716e; }
      @media (max-width: 760px) {
        .page { padding: 22px 16px 44px; }
        .listing-header { align-items: flex-start; flex-direction: column; gap: 12px; }
        h1 { font-size: 29px; }
        .gallery { height: 300px; grid-template-columns: minmax(0, 1.35fr) minmax(0, 1fr); }
        .photo-strip { grid-template-columns: 1fr; grid-template-rows: repeat(4, minmax(0, 1fr)); }
        .photo-strip img:nth-child(n+2) { display: none; }
        .listing-layout { grid-template-columns: 1fr; gap: 20px; margin-top: 18px; }
        .booking-panel { position: static; grid-row: 1; }
        .detail-grid { grid-template-columns: 1fr; gap: 14px; }
      }
      @media (max-width: 420px) {
        .gallery { height: 240px; grid-template-columns: 1fr; }
        .photo-strip { display: none; }
      }
    `,
  ],
})
export class PropertyDetailsComponent {
  propertyId: string | null;
  property: Property | null = null;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private propertyService: PropertyService
  ) {
    this.propertyId = this.route.snapshot.paramMap.get('id');
  }

  ngOnInit() {
    const id = this.propertyId ? Number(this.propertyId) : NaN;
    if (!Number.isFinite(id)) {
      this.error = 'Invalid property id';
      return;
    }

    this.propertyService.getPropertyById(id).subscribe({
      next: (p) => (this.property = p),
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to load property';
      },
    });
  }
}

