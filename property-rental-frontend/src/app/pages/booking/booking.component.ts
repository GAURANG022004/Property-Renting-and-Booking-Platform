import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookingService } from '../../services/booking.service';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <h2>Book Property</h2>
      <p class="muted">Property ID: {{ propertyId }}</p>

      <form class="form" (ngSubmit)="onSubmit()" #f="ngForm">
        <label>
          Check-in
          <input type="date" name="checkIn" required [(ngModel)]="checkIn" />
        </label>

        <label>
          Check-out
          <input
            type="date"
            name="checkOut"
            required
            [(ngModel)]="checkOut"
          />
        </label>

        <button type="submit" [disabled]="loading || !f.form.valid">
          Confirm Booking
        </button>
      </form>

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
export class BookingComponent {
  propertyId: number | null = null;
  checkIn = '';
  checkOut = '';
  loading = false;
  error = '';
  success = '';

  constructor(
    private route: ActivatedRoute,
    private bookingService: BookingService,
    private router: Router
  ) {
    const id = this.route.snapshot.paramMap.get('id');
    this.propertyId = id ? Number(id) : null;
  }

  onSubmit() {
    if (!this.propertyId) {
      this.error = 'Invalid property id';
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    this.bookingService
      .createBooking(this.propertyId, this.checkIn, this.checkOut)
      .subscribe({
        next: () => {
          this.loading = false;
          this.success = 'Booking request submitted for owner approval.';
          this.router.navigate(['/my-bookings']);
        },
        error: (err) => {
          this.loading = false;
          this.error =
            err?.error?.message ??
            'Booking failed. Make sure dates do not overlap.';
        },
      });
  }
}
