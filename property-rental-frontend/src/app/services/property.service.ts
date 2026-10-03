import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

export type Property = {
  id: number;
  title: string;
  description: string;
  location: string;
  pricePerNight: number;
  rating: number;
  mapUrl: string | null;
  maxGuests: number;
  checkInTime: string;
  checkOutTime: string;
  houseRules: string;
  cancellationFreeHours: number;
  refundPercentBeforeDeadline: number;
  refundPercentWithinDeadline: number;
  refundPercentAfterCheckIn: number;
  approvalStatus: string;
  ownerEmail: string;
  imageUrls: string[];
};

export type PropertyCreateRequest = {
  title: string;
  description: string;
  location: string;
  pricePerNight: number;
  mapUrl: string;
  maxGuests: number;
  checkInTime: string;
  checkOutTime: string;
  houseRules: string;
  cancellationFreeHours: number;
  refundPercentBeforeDeadline: number;
  refundPercentWithinDeadline: number;
  refundPercentAfterCheckIn: number;
};

type PropertyFilters = {
  location?: string;
  minPrice?: number;
  maxPrice?: number;
  minRating?: number;
};

@Injectable({
  providedIn: 'root',
})
export class PropertyService {
  private readonly apiBaseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getProperties(filters: PropertyFilters = {}): Observable<Property[]> {
    let params = new HttpParams();
    if (filters.location) params = params.set('location', filters.location);
    if (filters.minPrice != null) params = params.set('minPrice', String(filters.minPrice));
    if (filters.maxPrice != null) params = params.set('maxPrice', String(filters.maxPrice));
    if (filters.minRating != null) params = params.set('minRating', String(filters.minRating));

    return this.http
      .get<Property[]>(`${this.apiBaseUrl}/properties`, { params })
      .pipe(
        map((props) =>
          props.map((p) => ({
            ...p,
            imageUrls: (p.imageUrls ?? []).map((u) => this.toAbsoluteImageUrl(u)),
          }))
        )
      );
  }

  getPropertyById(id: number): Observable<Property> {
    return this.http.get<Property>(`${this.apiBaseUrl}/properties/${id}`).pipe(
      map((p) => ({
        ...p,
        imageUrls: (p.imageUrls ?? []).map((u) => this.toAbsoluteImageUrl(u)),
      }))
    );
  }

  createProperty(request: PropertyCreateRequest): Observable<Property> {
    return this.http.post<Property>(`${this.apiBaseUrl}/properties`, request).pipe(
      map((p) => ({
        ...p,
        imageUrls: (p.imageUrls ?? []).map((u) => this.toAbsoluteImageUrl(u)),
      }))
    );
  }

  uploadPropertyImages(propertyId: number, files: File[]): Observable<string[]> {
    const formData = new FormData();
    files.forEach((f) => formData.append('images', f));

    return this.http.post<string[]>(`${this.apiBaseUrl}/properties/${propertyId}/images`, formData);
  }

  replacePropertyImages(propertyId: number, files: File[]): Observable<string[]> {
    const formData = new FormData();
    files.forEach((f) => formData.append('images', f));

    return this.http.put<string[]>(`${this.apiBaseUrl}/properties/${propertyId}/images`, formData).pipe(
      map((urls) => urls.map((url) => this.toAbsoluteImageUrl(url)))
    );
  }

  private toAbsoluteImageUrl(url: string): string {
    if (!url) return url;
    if (url.startsWith('http://') || url.startsWith('https://')) return url;
    if (url.startsWith('/')) return `${this.apiBaseUrl}${url}`;
    return `${this.apiBaseUrl}/${url}`;
  }
}
