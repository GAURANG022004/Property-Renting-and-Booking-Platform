import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PropertyService, Property } from '../../services/property.service';

@Component({
  selector: 'app-property-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="page" *ngIf="property; else loadingTpl">
      <header class="top">
        <h2 class="title">{{ property.title }}</h2>
        <p class="muted">{{ property.location }}</p>
      </header>

      <div class="gallery">
        <img
          *ngFor="let img of property.imageUrls"
          class="gallery-img"
          [src]="img"
          [alt]="property.title"
        />
        <div class="gallery-empty" *ngIf="!property.imageUrls?.length">
          No images uploaded yet.
        </div>
      </div>

      <div class="details">
        <p class="description">{{ property.description }}</p>
        <a *ngIf="property.mapUrl" class="map-link" [href]="property.mapUrl" target="_blank" rel="noopener noreferrer">Open in Google Maps</a>
        <section class="house-rules">
          <h3>Stay information</h3>
          <p><strong>Maximum guests:</strong> {{ property.maxGuests }}</p>
          <p><strong>Check-in:</strong> {{ property.checkInTime }} · <strong>Check-out:</strong> {{ property.checkOutTime }}</p>
          <p><strong>Cancellation:</strong> {{ property.cancellationFreeHours }} hours or more before check-in: {{ property.refundPercentBeforeDeadline }}% refund; closer to check-in: {{ property.refundPercentWithinDeadline }}%; after check-in: {{ property.refundPercentAfterCheckIn }}%.</p>
          <p *ngIf="property.houseRules"><strong>House rules:</strong> {{ property.houseRules }}</p>
        </section>
        <div class="stats">
          <div class="stat">
            <div class="label">Price</div>
            <div class="value">
              <strong>{{ property.pricePerNight }}</strong> / night
            </div>
          </div>
          <div class="stat">
            <div class="label">Rating</div>
            <div class="value">
              <strong>{{ property.rating }}</strong>
            </div>
          </div>
        </div>

        <a
          class="btn"
          [routerLink]="['/properties', property.id, 'book']"
        >
          Book this property
        </a>
      </div>

      <div *ngIf="error" class="error">{{ error }}</div>
    </section>

    <ng-template #loadingTpl>
      <section class="page">
        <p class="muted">Loading property...</p>
      </section>
    </ng-template>
  `,
  styles: [
    `
      .page {
        padding: 20px;
        max-width: 980px;
        margin: 0 auto;
      }
      .top {
        margin-bottom: 14px;
      }
      .title {
        margin: 0;
        font-size: 26px;
        font-weight: 950;
      }
      .muted {
        color: #6b7280;
        margin-top: 6px;
        font-weight: 650;
      }

      .gallery {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 10px;
        margin-bottom: 16px;
      }
      .gallery-img {
        width: 100%;
        height: 170px;
        object-fit: cover;
        border-radius: 14px;
        border: 1px solid #e5e7eb;
      }
      .gallery-empty {
        grid-column: 1 / -1;
        padding: 18px;
        text-align: center;
        color: #6b7280;
        font-weight: 650;
        border: 1px dashed #e5e7eb;
        border-radius: 14px;
      }

      .details {
        border: 1px solid #e5e7eb;
        border-radius: 16px;
        padding: 16px;
        background: #fff;
      }

      .description {
        margin: 0 0 16px;
        color: #374151;
        font-weight: 600;
        line-height: 1.5;
      }
      .map-link {
        display: inline-block;
        margin-bottom: 16px;
        color: #176b70;
        font-weight: 800;
        text-decoration: underline;
        text-underline-offset: 3px;
      }
      .house-rules { margin:0 0 16px; padding:14px; border:1px solid #e5eceb; border-radius:10px; background:#f8fbfa; }
      .house-rules h3 { margin:0 0 8px; font-size:15px; }
      .house-rules p { margin:6px 0; color:#475569; line-height:1.45; }
      .stats {
        display: flex;
        gap: 20px;
        margin-bottom: 14px;
      }
      .stat .label {
        color: #6b7280;
        font-weight: 700;
        margin-bottom: 4px;
      }
      .stat .value {
        font-weight: 950;
      }

      .btn {
        display: inline-block;
        padding: 12px 16px;
        border-radius: 14px;
        background: #2563eb;
        color: #fff;
        text-decoration: none;
        font-weight: 900;
      }

      .error {
        margin-top: 12px;
        color: #dc2626;
        font-weight: 800;
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

