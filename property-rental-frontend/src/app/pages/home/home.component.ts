import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PropertyService, Property } from '../../services/property.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="header">
        <h2>Explore Properties</h2>
        <p class="muted">Search by location and filter by price/rating.</p>
      </header>

      <form class="filters" (ngSubmit)="load()" #f="ngForm">
        <label>
          Location
          <input
            name="location"
            [(ngModel)]="filters.location"
            placeholder="e.g., Mumbai"
          />
        </label>

        <label>
          Min Price
          <input
            name="minPrice"
            type="number"
            step="1"
            [(ngModel)]="filters.minPrice"
            placeholder="e.g., 1000"
          />
        </label>

        <label>
          Max Price
          <input
            name="maxPrice"
            type="number"
            step="1"
            [(ngModel)]="filters.maxPrice"
            placeholder="e.g., 5000"
          />
        </label>

        <label>
          Min Rating
          <input
            name="minRating"
            type="number"
            step="0.1"
            [(ngModel)]="filters.minRating"
            placeholder="e.g., 3.5"
          />
        </label>

        <div class="actions">
          <button type="submit" [disabled]="loading">Search</button>
          <button type="button" class="ghost" (click)="reset()">Reset</button>
        </div>
      </form>

      <div *ngIf="error" class="error">{{ error }}</div>

      <div *ngIf="loading" class="loading">Loading properties...</div>

      <div *ngIf="!loading" class="grid">
        <a
          *ngFor="let p of properties"
          class="card"
          [routerLink]="['/properties', p.id]"
        >
          <div class="img-wrap">
            <img
              *ngIf="p.imageUrls?.length"
              [src]="p.imageUrls[0]"
              [alt]="p.title"
            />
            <div *ngIf="!p.imageUrls?.length" class="img-placeholder"></div>
          </div>

          <div class="card-body">
            <h3 class="title">{{ p.title }}</h3>
            <p class="location">{{ p.location }}</p>
            <p class="price"><strong>{{ p.pricePerNight }}</strong> / night</p>
            <p class="rating">Rating: {{ p.rating }}</p>
          </div>
        </a>

        <div *ngIf="properties.length === 0" class="empty">
          No properties found for the selected filters.
        </div>
      </div>
    </section>
  `,
  styles: [
    `
      .page {
        padding: 20px;
        max-width: 1100px;
        margin: 0 auto;
      }
      .header {
        margin-bottom: 16px;
      }
      .muted {
        color: #6b7280;
        margin-top: 4px;
      }

      .filters {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 12px;
        padding: 14px;
        border: 1px solid #e5e7eb;
        border-radius: 16px;
        background: #fff;
        margin-bottom: 18px;
      }
      label {
        display: flex;
        flex-direction: column;
        gap: 6px;
        font-weight: 650;
        color: #111827;
        font-size: 14px;
      }
      input {
        padding: 10px 12px;
        border-radius: 12px;
        border: 1px solid #e5e7eb;
      }
      .actions {
        grid-column: 1 / -1;
        display: flex;
        gap: 12px;
        align-items: center;
        margin-top: 4px;
      }
      button {
        padding: 10px 14px;
        border-radius: 12px;
        border: 0;
        background: #2563eb;
        color: white;
        font-weight: 800;
        cursor: pointer;
      }
      button:disabled {
        background: #93c5fd;
        cursor: not-allowed;
      }
      .ghost {
        background: #fff;
        border: 1px solid #e5e7eb;
        color: #111827;
      }

      .grid {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 14px;
      }
      .card {
        border: 1px solid #e5e7eb;
        border-radius: 16px;
        overflow: hidden;
        background: #fff;
        text-decoration: none;
        color: inherit;
        display: block;
      }
      .img-wrap {
        height: 180px;
        background: #f3f4f6;
      }
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }
      .img-placeholder {
        width: 100%;
        height: 100%;
        background: #e5e7eb;
      }
      .card-body {
        padding: 12px 12px 14px;
      }
      .title {
        margin: 0 0 6px;
        font-size: 16px;
        font-weight: 900;
      }
      .location {
        margin: 0 0 10px;
        color: #6b7280;
        font-weight: 600;
      }
      .price {
        margin: 0;
        font-weight: 800;
      }
      .rating {
        margin: 4px 0 0;
        color: #374151;
        font-weight: 650;
      }

      .loading {
        padding: 10px 0;
      }
      .error {
        margin: 10px 0;
        color: #dc2626;
        font-weight: 700;
      }
      .empty {
        grid-column: 1 / -1;
        padding: 18px;
        text-align: center;
        color: #6b7280;
        font-weight: 650;
      }
    `,
  ],
})
export class HomeComponent {
  properties: Property[] = [];
  loading = false;
  error = '';

  filters: {
    location?: string;
    minPrice?: number;
    maxPrice?: number;
    minRating?: number;
  } = {};

  constructor(private propertyService: PropertyService) {}

  ngOnInit() {
    this.load();
  }

  reset() {
    this.filters = {};
    this.load();
  }

  load() {
    this.loading = true;
    this.error = '';

    this.propertyService.getProperties({
      location: this.filters.location,
      minPrice: this.filters.minPrice,
      maxPrice: this.filters.maxPrice,
      minRating: this.filters.minRating,
    }).subscribe({
      next: (props) => {
        this.properties = props;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to load properties';
        this.loading = false;
      },
    });
  }
}

