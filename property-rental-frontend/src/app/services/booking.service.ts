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
    checkOut: string
  ): Observable<Booking> {
    return this.http.post<Booking>(`${this.apiBaseUrl}/bookings`, {
      propertyId,
      checkIn,
      checkOut,
    });
  }

  getMyBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiBaseUrl}/bookings`).pipe(
      map((arr) => arr ?? [])
    );
  }
}
