import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Booking } from './booking.service';
import { Property } from './property.service';

export type OwnerDashboard = { totalProperties: number; activeProperties: number; pendingProperties: number; pendingBookingRequests: number; acceptedBookings: number; completedStays: number; estimatedEarnings: number };
export type AvailabilityBlock = { id: number; propertyId: number; startDate: string; endDate: string; reason: string };
@Injectable({ providedIn: 'root' })
export class OwnerService {
  private readonly base = 'http://localhost:8080/owner';
  constructor(private http: HttpClient) {}
  dashboard(): Observable<OwnerDashboard> { return this.http.get<OwnerDashboard>(`${this.base}/dashboard`); }
  properties(): Observable<Property[]> { return this.http.get<Property[]>(`${this.base}/properties`); }
  updateProperty(id: number, value: Pick<Property, 'title' | 'description' | 'location' | 'pricePerNight'>) { return this.http.put<Property>(`${this.base}/properties/${id}`, value); }
  deactivateProperty(id: number) { return this.http.patch<void>(`${this.base}/properties/${id}/deactivate`, {}); }
  bookings(): Observable<Booking[]> { return this.http.get<Booking[]>(`${this.base}/bookings`); }
  decideBooking(id: number, status: 'ACCEPTED' | 'REJECTED') { return this.http.patch<Booking>(`${this.base}/bookings/${id}`, { status }); }
  availability(): Observable<AvailabilityBlock[]> { return this.http.get<AvailabilityBlock[]>(`${this.base}/availability`); }
  blockAvailability(value: Omit<AvailabilityBlock, 'id'>) { return this.http.post<AvailabilityBlock>(`${this.base}/availability`, value); }
  removeAvailability(id: number) { return this.http.delete<void>(`${this.base}/availability/${id}`); }
}
