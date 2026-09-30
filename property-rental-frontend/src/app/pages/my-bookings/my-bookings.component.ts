import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookingService, Booking, CancellationQuote } from '../../services/booking.service';
import { PaymentService, RazorpayCheckoutResponse, RazorpayOrder } from '../../services/payment.service';
import { RazorpayCheckoutService } from '../../services/razorpay-checkout.service';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <h2>My Bookings</h2>

      <div *ngIf="loading" class="loading">Loading...</div>
      <div *ngIf="error" class="error">{{ error }}</div>
      <div *ngIf="notice" class="notice">{{ notice }}</div>

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
          <div class="status-line"><span class="status">{{ statusLabel(b.status) }}</span><span>{{ nights(b) }} nights · {{ b.guestCount }} guests</span></div>
          <dl class="amounts">
            <div><dt>Total rent</dt><dd>{{ b.totalAmount | currency:'INR':'symbol':'1.2-2' }}</dd></div>
          </dl>
          <p class="next-step" *ngIf="b.status === 'PENDING'">Waiting for the property owner to respond.</p>
          <button class="cancel-button" *ngIf="canCancel(b)" (click)="openCancellation(b)" [disabled]="processingBookingId === b.id">
            Review cancellation
          </button>
          <p class="next-step" *ngIf="b.status === 'PAYMENT_PENDING' && b.bookingType === 'REQUEST'">The owner approved your request. Pay the total rent to confirm your booking.</p>
          <p class="next-step" *ngIf="b.status === 'PAYMENT_PENDING' && b.bookingType === 'INSTANT'">These dates are available for instant booking. Pay the total rent to confirm.</p>
          <button *ngIf="b.status === 'PAYMENT_PENDING'" (click)="openPaymentPolicy(b)" [disabled]="processingBookingId === b.id">
            Review policy and pay
          </button>
          <p class="next-step" *ngIf="b.status === 'CONFIRMED'">Your booking is confirmed and payment is complete. The owner will check you in on arrival.</p>
          <p class="next-step" *ngIf="b.status === 'ACTIVE'">You are checked in. Enjoy your stay.</p>
          <p class="next-step" *ngIf="b.status === 'COMPLETED'">Stay completed. Thank you for booking with us.</p>
          <p class="next-step cancelled" *ngIf="b.status === 'REJECTED'">Unfortunately, the owner is unable to accept your booking request for these dates. Please try different dates or explore other properties.</p>
          <p class="next-step cancelled" *ngIf="b.status === 'CANCELLED'">Booking cancelled. Refund: {{ b.refundAmount | currency:'INR':'symbol':'1.2-2' }} ({{ b.refundStatus || 'NO_PAYMENT' }}).</p>
        </div>
      </div>

      <div class="modal-backdrop" *ngIf="policyBooking" role="presentation">
        <section class="policy-modal" role="dialog" aria-modal="true" aria-labelledby="policy-title">
          <button class="close" type="button" aria-label="Close" (click)="closePaymentPolicy()">×</button>
          <h2 id="policy-title">Booking Policy</h2>
          <p>Before completing your booking at <strong>{{ policyBooking.propertyTitle }}</strong>, please review:</p>
          <ol>
            <li>Your booking is confirmed only after successful payment.</li>
            <li>Availability depends on your selected dates. Another confirmed booking may reserve them before payment completes.</li>
            <li>The total shown is due to confirm. A failed payment does not confirm the booking.</li>
            <li>Check-in is {{ policyBooking.checkInTime }} and check-out is {{ policyBooking.checkOutTime }}.</li>
            <li>Cancellation: {{ policyBooking.cancellationFreeHours }} hours or more before check-in gives {{ policyBooking.refundPercentBeforeDeadline }}% refund; closer to check-in gives {{ policyBooking.refundPercentWithinDeadline }}%; after check-in gives {{ policyBooking.refundPercentAfterCheckIn }}%.</li>
            <li>Please provide accurate guest information. Guest count is {{ policyBooking.guestCount }} and cannot exceed {{ policyBooking.maxGuests }}.</li>
            <li>House rules: {{ policyBooking.houseRules || 'Follow the property's posted rules.' }}</li>
            <li>Booking changes are subject to availability and property policies.</li>
            <li>You may review the property after completing your stay.</li>
            <li>By acknowledging, you agree to this policy and the property rules.</li>
          </ol>
          <label class="acknowledge"><input type="checkbox" [(ngModel)]="policyAcknowledged"> I have read and acknowledge the Booking Policy and the property's applicable rules.</label>
          <button type="button" [disabled]="!policyAcknowledged" (click)="proceedToPayment()">Proceed to Payment</button>
        </section>
      </div>

      <div class="modal-backdrop" *ngIf="cancellationBooking && cancellationQuote" role="presentation">
        <section class="policy-modal" role="dialog" aria-modal="true" aria-labelledby="cancellation-title">
          <button class="close" type="button" aria-label="Close" (click)="closeCancellation()">×</button>
          <h2 id="cancellation-title">Cancellation &amp; Refund</h2>
          <p><strong>{{ cancellationBooking.propertyTitle }}</strong></p>
          <p>{{ cancellationQuote.cancellationPolicy }}</p>
          <dl class="refund-summary"><div><dt>Amount paid</dt><dd>{{ cancellationQuote.amountPaid | currency:'INR':'symbol':'1.2-2' }}</dd></div><div><dt>Estimated refund ({{ cancellationQuote.refundPercent }}%)</dt><dd>{{ cancellationQuote.refundAmount | currency:'INR':'symbol':'1.2-2' }}</dd></div><div><dt>Refund status</dt><dd>{{ cancellationQuote.refundStatus }}</dd></div></dl>
          <label class="acknowledge"><input type="checkbox" [(ngModel)]="cancellationAcknowledged"> I reviewed the refund amount and want to cancel.</label>
          <p class="next-step cancelled" *ngIf="!cancellationQuote.eligible">This booking cannot be cancelled in its current state.</p>
          <button class="cancel-button" type="button" [disabled]="!cancellationQuote.eligible || !cancellationAcknowledged || processingBookingId === cancellationBooking.id" (click)="confirmCancellation()">{{ processingBookingId === cancellationBooking.id ? 'Cancelling…' : 'Confirm cancellation' }}</button>
        </section>
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
      .status-line { display:flex; align-items:center; gap:10px; margin-top:10px; color:#64748b; font-size:13px; }
      .amounts { display:grid; grid-template-columns:repeat(3,minmax(0,1fr)); gap:12px; margin:14px 0 8px; padding:12px; background:#f8faf9; border:1px solid #e7efed; border-radius:10px; }
      .amounts div { min-width:0; }
      .amounts dt { color:#64748b; font-size:12px; }
      .amounts dd { margin:4px 0 0; color:#172033; font-weight:800; }
      .next-step { margin:10px 0; color:#475569; font-size:13px; }
      .cancelled { color:#a33a2b; }
      button { border:0; border-radius:8px; padding:10px 14px; background:#176b70; color:#fff; font-weight:800; cursor:pointer; }
      .cancel-button { margin-top:4px; background:#a33a2b; }
      button:disabled { opacity:.6; cursor:wait; }
      .notice { margin-top:12px; padding:12px; color:#166534; background:#f0fdf4; border-radius:9px; }
      .modal-backdrop { position:fixed; inset:0; z-index:1000; display:grid; place-items:center; padding:18px; background:#0f172aa8; }
      .policy-modal { position:relative; width:min(640px,100%); max-height:86vh; overflow:auto; padding:24px; background:#fff; border-radius:12px; box-shadow:0 24px 80px #0004; }
      .policy-modal h2 { margin:0 40px 14px 0; }
      .policy-modal li { margin:8px 0; color:#475569; line-height:1.45; }
      .policy-modal .close { position:absolute; top:12px; right:12px; padding:2px 9px; background:#edf2f4; color:#263238; font-size:24px; }
      .acknowledge { display:flex; flex-direction:row; align-items:flex-start; gap:9px; margin:18px 0; color:#263238; font-weight:650; }
      .acknowledge input { flex:0 0 auto; margin-top:3px; }
      .refund-summary { display:grid; grid-template-columns:repeat(3,minmax(0,1fr)); gap:12px; padding:12px; background:#f7faf9; border-radius:9px; }
      .refund-summary dt { color:#64748b; font-size:12px; }
      .refund-summary dd { margin:5px 0 0; font-weight:800; }
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
      @media(max-width:640px) { .amounts { grid-template-columns:1fr; gap:8px; } .top { align-items:flex-start; flex-direction:column; } }
    `,
  ],
})
export class MyBookingsComponent {
  bookings: Booking[] = [];
  loading = false;
  error = '';
  notice = '';
  processingBookingId: number | null = null;
  policyBooking: Booking | null = null;
  policyAcknowledged = false;
  cancellationBooking: Booking | null = null;
  cancellationQuote: CancellationQuote | null = null;
  cancellationAcknowledged = false;

  constructor(
    private bookingService: BookingService,
    private paymentService: PaymentService,
    private checkout: RazorpayCheckoutService
  ) {}

  ngOnInit() {
    this.loadBookings();
  }

  loadBookings() {
    this.loading = true;

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

  nights(booking: Booking) {
    const start = Date.parse(`${booking.checkIn}T00:00:00Z`);
    const end = Date.parse(`${booking.checkOut}T00:00:00Z`);
    return Math.max(0, (end - start) / 86_400_000);
  }

  statusLabel(status: string) {
    return status.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, letter => letter.toUpperCase());
  }

  canCancel(booking: Booking) {
    return ['PENDING', 'PAYMENT_PENDING', 'CONFIRMED', 'ACTIVE'].includes(booking.status);
  }

  openCancellation(booking: Booking) {
    this.error = '';
    this.notice = '';
    this.cancellationBooking = booking;
    this.cancellationAcknowledged = false;
    this.bookingService.getCancellationQuote(booking.id).subscribe({
      next: quote => this.cancellationQuote = quote,
      error: err => { this.error = err?.error?.message ?? 'Unable to load cancellation terms.'; this.closeCancellation(); },
    });
  }

  closeCancellation() { this.cancellationBooking = null; this.cancellationQuote = null; this.cancellationAcknowledged = false; }

  confirmCancellation() {
    if (!this.cancellationBooking || !this.cancellationAcknowledged || this.processingBookingId !== null) return;
    this.processingBookingId = this.cancellationBooking.id;
    this.bookingService.cancelBooking(this.cancellationBooking.id).subscribe({
      next: result => {
        this.processingBookingId = null;
        this.notice = `${result.message} Refund: ${result.refundAmount.toFixed(2)} INR (${result.refundStatus}).`;
        this.closeCancellation();
        this.loadBookings();
      },
      error: err => { this.processingBookingId = null; this.error = err?.error?.message ?? 'Unable to cancel this booking.'; },
    });
  }

  openPaymentPolicy(booking: Booking) { this.policyBooking = booking; this.policyAcknowledged = false; }
  closePaymentPolicy() { this.policyBooking = null; this.policyAcknowledged = false; }
  proceedToPayment() {
    if (!this.policyBooking || !this.policyAcknowledged) return;
    const booking = this.policyBooking;
    this.closePaymentPolicy();
    this.pay(booking);
  }

  pay(booking: Booking) {
    if (this.processingBookingId !== null) return;
    this.error = '';
    this.notice = '';
    this.processingBookingId = booking.id;
    this.paymentService.createOrder(booking.id).subscribe({
      next: order => this.openCheckout(booking, order),
      error: err => this.paymentFailed(err?.error?.message ?? 'Unable to start payment. Check Razorpay configuration and try again.'),
    });
  }

  private openCheckout(booking: Booking, order: RazorpayOrder) {
    this.checkout.open(order).then((response: RazorpayCheckoutResponse | null) => {
      if (!response) {
        this.processingBookingId = null;
        return;
      }
      this.paymentService.verifyPayment(booking.id, response).subscribe({
        next: () => {
          this.processingBookingId = null;
          this.notice = 'Payment received. Your booking is confirmed.';
          this.loadBookings();
        },
        error: err => this.paymentFailed(err?.error?.message ?? 'Payment completed but could not be verified. Contact support before retrying.'),
      });
    }).catch(err => this.paymentFailed(err?.message ?? 'Razorpay checkout failed.'));
  }

  private paymentFailed(message: string) {
    this.error = message;
    this.processingBookingId = null;
  }
}
