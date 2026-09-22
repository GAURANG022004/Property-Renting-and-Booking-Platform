import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PropertyCreateRequest, PropertyService } from '../../services/property.service';

@Component({
  selector: 'app-owner-add-property',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <h2>Add Property</h2>
      <p class="muted">New listings are submitted for administrator approval before appearing publicly.</p>

      <form class="form" (ngSubmit)="onSubmit()" #f="ngForm">
        <label>
          Title
          <input name="title" [(ngModel)]="request.title" required />
        </label>

        <label>
          Description
          <textarea
            name="description"
            [(ngModel)]="request.description"
            required
            rows="4"
          ></textarea>
        </label>

        <label>
          Location
          <input name="location" [(ngModel)]="request.location" required />
        </label>

        <div class="row">
          <label>
            Price Per Night
            <input
              name="pricePerNight"
              type="number"
              step="1"
              [(ngModel)]="request.pricePerNight"
              required
            />
          </label>

          <label>
            Rating
            <input
              name="rating"
              type="number"
              step="0.1"
              [(ngModel)]="request.rating"
              required
            />
          </label>
        </div>

        <label class="upload">
          Upload Images
          <input
            type="file"
            (change)="onFilesSelected($event)"
            multiple
            accept="image/*"
          />
          <span class="hint">Select multiple images (optional but recommended).</span>
        </label>

        <div class="actions">
          <button type="submit" [disabled]="loading || !f.form.valid">
            {{ loading ? 'Submitting...' : 'Create & Upload' }}
          </button>
          <button type="button" class="ghost" (click)="reset()">Reset</button>
        </div>
      </form>

      <div *ngIf="error" class="error">{{ error }}</div>
      <div *ngIf="success" class="ok">{{ success }}</div>
    </section>
  `,
  styles: [
    `
      .page {
        padding: 20px;
        max-width: 820px;
        margin: 0 auto;
      }
      .muted {
        color: #6b7280;
      }
      .form {
        display: flex;
        flex-direction: column;
        gap: 12px;
        margin-top: 14px;
        background: #fff;
        border: 1px solid #e5e7eb;
        border-radius: 18px;
        padding: 14px;
      }
      label {
        display: flex;
        flex-direction: column;
        gap: 6px;
        font-weight: 650;
        color: #111827;
      }
      input,
      textarea {
        padding: 10px 12px;
        border-radius: 12px;
        border: 1px solid #e5e7eb;
      }
      textarea {
        resize: vertical;
      }
      .row {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 12px;
      }
      .upload .hint {
        color: #6b7280;
        font-weight: 600;
        font-size: 13px;
      }
      .actions {
        display: flex;
        gap: 12px;
        align-items: center;
        margin-top: 8px;
      }
      button {
        padding: 10px 14px;
        border-radius: 12px;
        border: 0;
        background: #2563eb;
        color: #fff;
        font-weight: 900;
        cursor: pointer;
      }
      button:disabled {
        background: #93c5fd;
        cursor: not-allowed;
      }
      .ghost {
        background: #fff;
        border: 1px solid #e5e7eb;
        color: #111827;
      }
      .error {
        margin-top: 12px;
        color: #dc2626;
        font-weight: 850;
      }
      .ok {
        margin-top: 12px;
        color: #16a34a;
        font-weight: 850;
      }
    `,
  ],
})
export class OwnerAddPropertyComponent {
  loading = false;
  error = '';
  success = '';

  request: PropertyCreateRequest = {
    title: '',
    description: '',
    location: '',
    pricePerNight: 0,
    rating: 0,
  };

  selectedFiles: File[] = [];

  constructor(
    private propertyService: PropertyService,
    private router: Router
  ) {}

  onFilesSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const files = input.files ? Array.from(input.files) : [];
    this.selectedFiles = files;
  }

  reset() {
    this.error = '';
    this.success = '';
    this.request = {
      title: '',
      description: '',
      location: '',
      pricePerNight: 0,
      rating: 0,
    };
    this.selectedFiles = [];
  }

  onSubmit() {
    this.loading = true;
    this.error = '';
    this.success = '';

    this.propertyService.createProperty(this.request).subscribe({
      next: (created) => {
        if (this.selectedFiles.length === 0) {
          this.loading = false;
          this.success = 'Property created.';
          this.router.navigate(['/owner']);
          return;
        }

        this.propertyService
          .uploadPropertyImages(created.id, this.selectedFiles)
          .subscribe({
            next: () => {
              this.loading = false;
              this.success = 'Property created and images uploaded!';
              this.router.navigate(['/owner']);
            },
            error: (err) => {
              this.loading = false;
              this.error =
                err?.error?.message ??
                'Property created, but image upload failed.';
              this.router.navigate(['/owner']);
            },
          });
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message ?? 'Failed to create property';
      },
    });
  }
}
