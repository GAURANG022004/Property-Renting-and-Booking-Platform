import { Injectable } from '@angular/core';
import { RazorpayCheckoutResponse, RazorpayOrder } from './payment.service';

type RazorpayOptions = {
  key: string;
  amount: number;
  currency: string;
  name: string;
  description: string;
  order_id: string;
  handler: (response: RazorpayCheckoutResponse) => void;
  modal: { ondismiss: () => void };
  theme: { color: string };
};

type RazorpayInstance = {
  open: () => void;
  on: (event: string, callback: (response: { error?: { description?: string } }) => void) => void;
};

type RazorpayConstructor = new (options: RazorpayOptions) => RazorpayInstance;

declare global {
  interface Window {
    Razorpay?: RazorpayConstructor;
  }
}

@Injectable({ providedIn: 'root' })
export class RazorpayCheckoutService {
  private scriptLoad?: Promise<void>;

  async open(order: RazorpayOrder): Promise<RazorpayCheckoutResponse | null> {
    await this.loadCheckout();

    return new Promise((resolve, reject) => {
      const checkout = new window.Razorpay!({
        key: order.keyId,
        amount: order.amount,
        currency: order.currency,
        name: 'Property Rental',
        description: `Full booking payment for ${order.propertyTitle}`,
        order_id: order.orderId,
        handler: resolve,
        modal: { ondismiss: () => resolve(null) },
        theme: { color: '#176b70' },
      });

      checkout.on('payment.failed', (response) => {
        reject(new Error(response.error?.description ?? 'Razorpay payment failed.'));
      });
      checkout.open();
    });
  }

  private loadCheckout(): Promise<void> {
    if (window.Razorpay) return Promise.resolve();
    if (this.scriptLoad) return this.scriptLoad;

    this.scriptLoad = new Promise<void>((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.async = true;
      script.onload = () => window.Razorpay ? resolve() : reject(new Error('Razorpay Checkout did not load.'));
      script.onerror = () => reject(new Error('Unable to load Razorpay Checkout. Check your internet connection.'));
      document.head.appendChild(script);
    }).catch((error: unknown) => {
      this.scriptLoad = undefined;
      throw error;
    });

    return this.scriptLoad!;
  }
}