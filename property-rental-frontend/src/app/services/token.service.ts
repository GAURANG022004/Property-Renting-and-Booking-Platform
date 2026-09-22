import { Injectable } from '@angular/core';

type JwtPayload = {
  uid?: number;
  sub?: string;
  roles?: string[];
  exp?: number;
  iat?: number;
};

@Injectable({
  providedIn: 'root',
})
export class TokenService {
  private readonly tokenKey = 'auth_token';

  setToken(token: string) {
    localStorage.setItem(this.tokenKey, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  clear() {
    localStorage.removeItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;

    const payload = this.decodePayload(token);
    if (!payload?.exp) return true;

    // exp is seconds since epoch
    const nowSec = Math.floor(Date.now() / 1000);
    return payload.exp > nowSec;
  }

  getRoles(): string[] {
    const token = this.getToken();
    if (!token) return [];

    const payload = this.decodePayload(token);
    return payload?.roles ?? [];
  }

  getEmail(): string {
    const token = this.getToken();
    if (!token) return '';

    return this.decodePayload(token)?.sub ?? '';
  }

  private decodePayload(token: string): JwtPayload | null {
    try {
      const parts = token.split('.');
      if (parts.length < 2) return null;

      const base64Url = parts[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');

      const jsonPayload = decodeURIComponent(
        atob(padded)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );

      return JSON.parse(jsonPayload) as JwtPayload;
    } catch {
      return null;
    }
  }
}
