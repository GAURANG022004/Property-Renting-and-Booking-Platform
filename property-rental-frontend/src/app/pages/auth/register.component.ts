import { Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, NgIf],
  template: `
    <section class="page">
      <h2>Register</h2>
      <p class="muted">Create your renter account. All fields are required.</p>

      <form class="form" (ngSubmit)="onSubmit()" #f="ngForm">
        <div class="name-row">
          <label>
            First name
            <input
              type="text"
              name="firstName"
              [(ngModel)]="firstName"
              required
              maxlength="50"
              autocomplete="given-name"
              placeholder="Gaurang"
            />
          </label>

          <label>
            Last name
            <input
              type="text"
              name="lastName"
              [(ngModel)]="lastName"
              required
              maxlength="50"
              autocomplete="family-name"
              placeholder="Shah"
            />
          </label>
        </div>

        <label>
          Email
          <input
            type="email"
            name="email"
            [(ngModel)]="email"
            required
            autocomplete="email"
            placeholder="you@example.com"
          />
        </label>

        <label>
          Phone number
          <input
            type="tel"
            name="phoneNumber"
            [(ngModel)]="phoneNumber"
            required
            minlength="7"
            maxlength="20"
            pattern="[+0-9() -]{7,20}"
            autocomplete="tel"
            placeholder="+91 98765 43210"
          />
        </label>

        <label>
          Password
          <input
            type="password"
            name="password"
            [(ngModel)]="password"
            required
            minlength="8"
            autocomplete="new-password"
            placeholder="At least 8 characters"
          />
        </label>

        <button type="submit" [disabled]="loading || !f.form.valid">
          {{ loading ? 'Creating account...' : 'Create Account' }}
        </button>
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
      .name-row {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px;
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
        background: #16a34a;
        color: white;
        cursor: pointer;
        font-weight: 700;
      }
      button:disabled {
        background: #86efac;
        cursor: not-allowed;
      }
      .error {
        margin-top: 12px;
        color: #dc2626;
        font-weight: 600;
      }
      @media (max-width: 480px) {
        .name-row { grid-template-columns: 1fr; }
      }
    `,
  ],
})
export class RegisterComponent {
  firstName = '';
  lastName = '';
  email = '';
  phoneNumber = '';
  password = '';
  loading = false;
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    this.loading = true;
    this.error = '';

    this.authService.register({
      firstName: this.firstName.trim(),
      lastName: this.lastName.trim(),
      email: this.email.trim(),
      password: this.password,
      phoneNumber: this.phoneNumber.trim(),
    }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message ?? 'Registration failed';
      },
    });
  }
}
