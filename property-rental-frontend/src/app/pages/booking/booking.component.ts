import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AvailabilityCheck, BookingService } from '../../services/booking.service';
import { Property, PropertyService } from '../../services/property.service';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <h2>Request a booking</h2>
      <p class="muted" *ngIf="property">{{ property.title }} · {{ property.location }}</p>
      <p class="muted" *ngIf="propertyLoading">Loading property price…</p>

      <form class="form" (ngSubmit)="checkAvailability()" #f="ngForm">
        <label>
          Check-in
          <input type="date" name="checkIn" [min]="today" required [(ngModel)]="checkIn" (ngModelChange)="availability = null" />
        </label>

        <label>
          Check-out
          <input
            type="date"
            name="checkOut"
            [min]="checkIn || today"
            required
            [(ngModel)]="checkOut"
            (ngModelChange)="availability = null"
          />
        </label>

        <label>
          Guests
          <input type="number" name="guestCount" min="1" [max]="property?.maxGuests ?? 50" required [(ngModel)]="guestCount" (ngModelChange)="availability = null" />
          <span class="muted">Maximum {{ property?.maxGuests ?? 50 }} guests</span>
        </label>

        <button type="submit" [disabled]="checkingAvailability || propertyLoading || !property || !f.form.valid || nights <= 0">
          {{ checkingAvailability ? 'Checking dates…' : 'Check availability and price' }}
        </button>
      </form>

      <section class="quote result" *ngIf="availability" [class.unavailable]="!availability.available">
        <strong>{{ availability.message }}</strong>
        <div *ngIf="availability.available"><span>{{ nights }} nights · Total due</span><strong>{{ availability.totalAmount | currency:'INR':'symbol':'1.2-2' }}</strong></div>
        <p *ngIf="availability.available">{{ availability.instantBooking ? 'Instant booking: payment confirms immediately.' : 'Request booking: owner approval is required before payment.' }}</p>
        <button *ngIf="availability.available" type="button" (click)="submitBooking()" [disabled]="loading">
          {{ loading ? 'Submitting…' : availability.instantBooking ? 'Continue to payment' : 'Request booking' }}
        </button>
      </section>

      <p class="error" *ngIf="error">{{ error }}</p>
      <p class="ok" *ngIf="success">{{ success }}</p>
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
      .form {
        display: flex;
        flex-direction: column;
        gap: 12px;
        max-width: 420px;
        margin-top: 14px;
      }
      .quote { display:grid; gap:8px; padding:14px; border:1px solid #d8e7e5; border-radius:12px; background:#f5fbfa; }
      .quote div { display:flex; justify-content:space-between; gap:12px; color:#475569; }
      .quote strong { color:#172033; white-space:nowrap; }
      label {
        display: flex;
        flex-direction: column;
        gap: 6px;
        font-weight: 650;
        color: #111827;
      }
      input {
        padding: 10px 12px;
        border-radius: 12px;
        border: 1px solid #e5e7eb;
      }
      button {
        margin-top: 6px;
        padding: 10px 14px;
        border: 0;
        border-radius: 12px;
        background: #2563eb;
        color: #fff;
        font-weight: 900;
        cursor: pointer;
      }
      button:disabled {
        background: #93c5fd;
        cursor: not-allowed;
      }
      .error {
        margin-top: 12px;
        color: #dc2626;
        font-weight: 800;
      }
      .ok {
        margin-top: 12px;
        color: #16a34a;
        font-weight: 900;
      }
    `,
  ],
})
export class BookingComponent implements OnInit {
  propertyId: number | null = null;
  property: Property | null = null;
  checkIn = '';
  checkOut = '';
  guestCount = 1;
  availability: AvailabilityCheck | null = null;
  readonly today = new Date().toISOString().slice(0, 10);
  loading = false;
  checkingAvailability = false;
  propertyLoading = true;
  error = '';
  success = '';

  constructor(
    private route: ActivatedRoute,
    private bookingService: BookingService,
    private propertyService: PropertyService,
    private router: Router
  ) {
    const id = this.route.snapshot.paramMap.get('id');
    this.propertyId = id ? Number(id) : null;
  }

  ngOnInit() {
    if (!this.propertyId) {
      this.propertyLoading = false;
      this.error = 'Invalid property id';
      return;
    }
    this.propertyService.getPropertyById(this.propertyId).subscribe({
      next: property => { this.property = property; this.propertyLoading = false; },
      error: err => { this.propertyLoading = false; this.error = err?.error?.message ?? 'Unable to load property price.'; },
    });
  }

  get nights() {
    if (!this.checkIn || !this.checkOut) return 0;
    const start = Date.parse(`${this.checkIn}T00:00:00Z`);
    const end = Date.parse(`${this.checkOut}T00:00:00Z`);
    return Number.isFinite(start) && Number.isFinite(end) && end > start ? (end - start) / 86_400_000 : 0;
  }

  checkAvailability() {
    if (!this.propertyId) {
      this.error = 'Invalid property id';
      return;
    }
    this.checkingAvailability = true;
    this.error = '';
    this.success = '';
    this.availability = null;
    this.bookingService.checkAvailability(this.propertyId, this.checkIn, this.checkOut, this.guestCount).subscribe({
      next: result => { this.availability = result; this.checkingAvailability = false; },
      error: err => { this.error = err?.error?.message ?? 'Could not check these dates.'; this.checkingAvailability = false; },
    });
  }

  submitBooking() {
    if (!this.propertyId || !this.availability?.available) return;
    this.loading = true;
    this.error = '';
    this.bookingService.createBooking(this.propertyId, this.checkIn, this.checkOut, this.guestCount).subscribe({
      next: booking => {
        this.loading = false;
        this.success = booking.status === 'PAYMENT_PENDING'
          ? 'Instant booking reserved. Review the policy and pay to confirm.'
          : 'Booking request submitted for owner approval.';
        this.router.navigate(['/my-bookings']);
      },
      error: err => {
        this.loading = false;
        this.availability = null;
        this.error = err?.error?.message ?? 'These dates are no longer available. Please check again.';
      },
    });
  }
}
