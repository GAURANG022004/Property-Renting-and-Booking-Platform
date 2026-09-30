import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

export type Booking = {
  id: number;
  propertyId: number;
  propertyTitle: string;
  location: string;
  checkIn: string; // yyyy-MM-dd
  checkOut: string; // yyyy-MM-dd
  status: string;
  totalAmount: number;
  guestCount: number;
  bookingType: 'INSTANT' | 'REQUEST';
  maxGuests: number;
  checkInTime: string;
  checkOutTime: string;
  houseRules: string;
  cancellationFreeHours: number;
  refundPercentBeforeDeadline: number;
  refundPercentWithinDeadline: number;
  refundPercentAfterCheckIn: number;
  refundAmount: number;
  refundStatus: string | null;
};

@Injectable({
  providedIn: 'root',
})
export class BookingService {
  private readonly apiBaseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  createBooking(
    propertyId: number,
    checkIn: string,
    checkOut: string,
    guestCount: number
  ): Observable<Booking> {
    return this.http.post<Booking>(`${this.apiBaseUrl}/bookings`, {
      propertyId,
      checkIn,
      checkOut,
      guestCount,
    });
  }

  checkAvailability(propertyId: number, checkIn: string, checkOut: string, guestCount: number) {
    return this.http.post<AvailabilityCheck>(`${this.apiBaseUrl}/bookings/availability-check`, { propertyId, checkIn, checkOut, guestCount });
  }

  getCancellationQuote(id: number) {
    return this.http.get<CancellationQuote>(`${this.apiBaseUrl}/bookings/${id}/cancellation-quote`);
  }

  cancelBooking(id: number) {
    return this.http.patch<CancellationResult>(`${this.apiBaseUrl}/bookings/${id}/cancel`, { acknowledged: true });
  }

  getMyBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiBaseUrl}/bookings`).pipe(
      map((arr) => arr ?? [])
    );
  }

}

export type AvailabilityCheck = {
  available: boolean;
  instantBooking: boolean;
  totalAmount: number;
  guestCount: number;
  maxGuests: number;
  message: string;
};

export type CancellationQuote = {
  bookingId: number;
  eligible: boolean;
  cancellationPolicy: string;
  refundPercent: number;
  amountPaid: number;
  refundAmount: number;
  refundStatus: string;
};

export type CancellationResult = {
  bookingId: number;
  status: string;
  refundAmount: number;
  refundStatus: string;
  message: string;
};
