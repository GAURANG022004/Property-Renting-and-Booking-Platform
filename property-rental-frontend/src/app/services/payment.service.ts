import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type RazorpayOrder = {
  bookingId: number;
  orderId: string;
  keyId: string;
  amount: number;
  currency: string;
  propertyTitle: string;
};

export type RazorpayCheckoutResponse = {
  razorpay_order_id: string;
  razorpay_payment_id: string;
  razorpay_signature: string;
};

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly base = `${environment.apiBaseUrl}/payments`;

  constructor(private http: HttpClient) {}

  createOrder(bookingId: number): Observable<RazorpayOrder> {
    return this.http.post<RazorpayOrder>(`${this.base}/orders`, { bookingId });
  }

  verifyPayment(bookingId: number, response: RazorpayCheckoutResponse) {
    return this.http.post(`${this.base}/verify`, {
      bookingId,
      razorpayOrderId: response.razorpay_order_id,
      razorpayPaymentId: response.razorpay_payment_id,
      razorpaySignature: response.razorpay_signature,
    });
  }
}