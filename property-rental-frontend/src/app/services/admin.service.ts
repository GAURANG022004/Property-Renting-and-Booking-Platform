import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Booking } from './booking.service';
import { Property } from './property.service';

export type AdminDashboard = { totalUsers: number; activeUsers: number; totalOwners: number; totalProperties: number; pendingProperties: number; totalBookings: number; cancelledBookings: number; openComplaints: number };
export type AdminUser = { id: number; firstName: string; lastName: string; email: string; phoneNumber: string; roles: string[]; active: boolean };
export type Category = { id: number; name: string; description: string };
export type Complaint = { id: number; reportedByEmail: string; subject: string; description: string; status: string; resolutionNote: string };
export type Setting = { key: string; value: string };

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly base = 'http://localhost:8080/admin';
  constructor(private http: HttpClient) {}
  dashboard(): Observable<AdminDashboard> { return this.http.get<AdminDashboard>(`${this.base}/dashboard`); }
  users(): Observable<AdminUser[]> { return this.http.get<AdminUser[]>(`${this.base}/users`); }
  owners(): Observable<AdminUser[]> { return this.http.get<AdminUser[]>(`${this.base}/owners`); }
  setUserStatus(id: number, active: boolean) { return this.http.patch<AdminUser>(`${this.base}/users/${id}/status`, { active }); }
  setUserRole(id: number, role: 'OWNER' | 'TENANT') { return this.http.patch<AdminUser>(`${this.base}/users/${id}/role`, { role }); }
  properties(): Observable<Property[]> { return this.http.get<Property[]>(`${this.base}/properties`); }
  moderateProperty(id: number, status: 'APPROVED' | 'REJECTED') { return this.http.patch<Property>(`${this.base}/properties/${id}/approval`, { status }); }
  deleteProperty(id: number) { return this.http.delete<void>(`${this.base}/properties/${id}`); }
  bookings(): Observable<Booking[]> { return this.http.get<Booking[]>(`${this.base}/bookings`); }
  cancelBooking(id: number) { return this.http.patch<Booking>(`${this.base}/bookings/${id}/cancel`, {}); }
  categories(): Observable<Category[]> { return this.http.get<Category[]>(`${this.base}/categories`); }
  saveCategory(category: Partial<Category>) { return category.id ? this.http.put<Category>(`${this.base}/categories/${category.id}`, category) : this.http.post<Category>(`${this.base}/categories`, category); }
  deleteCategory(id: number) { return this.http.delete<void>(`${this.base}/categories/${id}`); }
  complaints(): Observable<Complaint[]> { return this.http.get<Complaint[]>(`${this.base}/complaints`); }
  resolveComplaint(id: number, status: string, resolutionNote: string) { return this.http.patch<Complaint>(`${this.base}/complaints/${id}`, { status, resolutionNote }); }
  settings(): Observable<Setting[]> { return this.http.get<Setting[]>(`${this.base}/settings`); }
  saveSetting(setting: Setting) { return this.http.put<Setting>(`${this.base}/settings`, setting); }
}
