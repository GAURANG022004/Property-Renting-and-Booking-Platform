import { Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { TokenService } from '../../services/token.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, NgIf],
  template: `
    <section class="page">
      <h2>Login</h2>

      <form class="form" (ngSubmit)="onSubmit()" #f="ngForm">
        <label>
          Email
          <input
            type="email"
            name="email"
            [(ngModel)]="email"
            required
            placeholder="you@example.com"
          />
        </label>

        <label>
          Password
          <input
            type="password"
            name="password"
            [(ngModel)]="password"
            required
            placeholder="Your password"
          />
        </label>

        <button type="submit" [disabled]="loading || !f.form.valid">Login</button>
      </form>

      <p class="error" *ngIf="error">{{ error }}</p>
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
        margin-top: 14px;
        max-width: 380px;
      }
      label {
        display: flex;
        flex-direction: column;
        gap: 6px;
        font-weight: 600;
        color: #111827;
      }
      input {
        padding: 10px 12px;
        border-radius: 10px;
        border: 1px solid #e5e7eb;
      }
      button {
        margin-top: 6px;
        padding: 10px 12px;
        border: 0;
        border-radius: 10px;
        background: #2563eb;
        color: white;
        cursor: pointer;
        font-weight: 700;
      }
      button:disabled {
        background: #93c5fd;
        cursor: not-allowed;
      }
      .error {
        margin-top: 12px;
        color: #dc2626;
        font-weight: 600;
      }
    `,
  ],
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  error = '';

  constructor(
    private authService: AuthService,
    private tokenService: TokenService,
    private router: Router
  ) {}

  onSubmit() {
    this.loading = true;
    this.error = '';

    this.authService.login(this.email, this.password).subscribe({
      next: (res) => {
        this.tokenService.setToken(res.token);
        this.loading = false;
        const roles = this.tokenService.getRoles();
        this.router.navigate([roles.includes('ADMIN') ? '/admin' : roles.includes('OWNER') ? '/owner' : '/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.error =
          err?.error?.message ?? 'Login failed. Please check your credentials.';
      },
    });
  }
}
