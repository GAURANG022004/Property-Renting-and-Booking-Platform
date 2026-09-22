import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookingService, Booking } from '../../services/booking.service';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="page">
      <h2>My Bookings</h2>

      <div *ngIf="loading" class="loading">Loading...</div>
      <div *ngIf="error" class="error">{{ error }}</div>

      <div *ngIf="!loading && bookings.length === 0" class="empty">
        No bookings yet.
      </div>

      <div *ngIf="!loading && bookings.length > 0" class="list">
        <div *ngFor="let b of bookings" class="item">
          <div class="top">
            <h3 class="title">{{ b.propertyTitle }}</h3>
            <span class="badge">{{ b.location }}</span>
          </div>

          <div class="dates">
            {{ b.checkIn }} to {{ b.checkOut }}
          </div>
          <span class="status">{{ b.status }}</span>
        </div>
      </div>
    </section>
  `,
  styles: [
    `
      .page {
        padding: 20px;
      }
      .muted {
        color: #6b7280;
      }
      .loading {
        margin-top: 12px;
        color: #6b7280;
        font-weight: 700;
      }
      .error {
        margin-top: 12px;
        color: #dc2626;
        font-weight: 800;
      }
      .empty {
        margin-top: 12px;
        padding: 18px;
        text-align: center;
        color: #6b7280;
        font-weight: 650;
        border: 1px dashed #e5e7eb;
        border-radius: 16px;
      }
      .list {
        display: flex;
        flex-direction: column;
        gap: 12px;
        margin-top: 14px;
      }
      .item {
        border: 1px solid #e5e7eb;
        border-radius: 16px;
        padding: 14px 14px;
        background: #fff;
      }
      .top {
        display: flex;
        gap: 12px;
        align-items: baseline;
        justify-content: space-between;
      }
      .title {
        margin: 0;
        font-size: 16px;
        font-weight: 900;
      }
      .badge {
        color: #6b7280;
        font-weight: 700;
      }
      .dates {
        margin-top: 10px;
        color: #111827;
        font-weight: 750;
      }
      .status { display:inline-block; margin-top:10px; padding:4px 8px; border-radius:999px; background:#eef2ff; color:#4338ca; font-size:12px; font-weight:800; }
    `,
  ],
})
export class MyBookingsComponent {
  bookings: Booking[] = [];
  loading = false;
  error = '';

  constructor(private bookingService: BookingService) {}

  ngOnInit() {
    this.loading = true;
    this.error = '';

    this.bookingService.getMyBookings().subscribe({
      next: (arr) => {
        this.bookings = arr ?? [];
        this.loading = false;
      },
      error: (err) => {
        this.error =
          err?.error?.message ?? 'Failed to load booking history';
        this.loading = false;
      },
    });
  }
}
