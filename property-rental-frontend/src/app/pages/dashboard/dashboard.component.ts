import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Booking, BookingService } from '../../services/booking.service';
import { Property, PropertyService } from '../../services/property.service';
import { TokenService } from '../../services/token.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="page">
      <div class="hero">
        <div>
          <span class="eyebrow">YOUR RENTAL HUB</span>
          <h1>Welcome back.</h1>
          <p>Plan your next stay, revisit bookings, and discover a place that fits.</p>
        </div>
        <div class="identity">
          <span class="avatar">{{ initials }}</span>
          <div><strong>{{ email }}</strong><span>{{ roleLabel }}</span></div>
        </div>
      </div>

      <p *ngIf="error" class="error">{{ error }}</p>

      <div class="metrics" *ngIf="!loading">
        <article><span>Total bookings</span><strong>{{ bookings.length }}</strong><small>Saved to your account</small></article>
        <article><span>Upcoming stays</span><strong>{{ upcomingBookings.length }}</strong><small>Check-outs still ahead</small></article>
        <article><span>Available properties</span><strong>{{ properties.length }}</strong><small>Approved places to explore</small></article>
      </div>

      <div *ngIf="loading" class="loading">Preparing your dashboard…</div>

      <div class="content" *ngIf="!loading">
        <section class="panel primary-panel">
          <div class="panel-heading">
            <div><h2>Your upcoming stays</h2><p>Everything you have coming up in one place.</p></div>
            <a routerLink="/my-bookings">View all</a>
          </div>

          <div class="booking-list" *ngIf="upcomingBookings.length; else noBookings">
            <div *ngFor="let booking of upcomingBookings" class="booking-row">
              <span class="calendar"><b>{{ booking.checkIn | date:'dd' }}</b><small>{{ booking.checkIn | date:'MMM' }}</small></span>
              <span><strong>{{ booking.propertyTitle }}</strong><small>{{ booking.location }} · {{ booking.checkIn }} to {{ booking.checkOut }}</small></span>
            </div>
          </div>

          <ng-template #noBookings><div class="empty">No upcoming stays yet. Start exploring when you are ready.</div></ng-template>
        </section>

        <aside class="panel action-panel">
          <h2>Quick links</h2>
          <p>Pick up where you left off.</p>
          <a routerLink="/properties" class="action"><span>⌕</span><div><strong>Browse properties</strong><small>Search the latest homes</small></div><b>›</b></a>
          <a routerLink="/my-bookings" class="action"><span>□</span><div><strong>My bookings</strong><small>Review reservation details</small></div><b>›</b></a>
        </aside>
      </div>

      <section class="panel listings-panel" *ngIf="!loading">
        <div class="panel-heading">
          <div><h2>Available properties</h2><p>All approved listings currently available in the catalogue.</p></div>
          <a routerLink="/properties">Browse and filter</a>
        </div>
        <div class="property-grid" *ngIf="properties.length; else noProperties">
          <a *ngFor="let property of properties" [routerLink]="['/properties', property.id]" class="property-card">
            <div class="property-photo">
              <img *ngIf="property.imageUrls?.length" [src]="property.imageUrls[0]" [alt]="property.title" loading="lazy">
              <span *ngIf="!property.imageUrls?.length" class="property-placeholder">{{ property.title.charAt(0) }}</span>
            </div>
            <div class="property-info">
              <h3>{{ property.title }}</h3>
              <p>{{ property.location }}</p>
              <div class="property-meta"><span>★ {{ property.rating }}</span><strong>₹{{ property.pricePerNight }} <small>/ night</small></strong></div>
            </div>
          </a>
        </div>
        <ng-template #noProperties><div class="empty">No approved properties are available yet.</div></ng-template>
      </section>
    </section>
  `,
  styles: [`
    .page { max-width: 1120px; margin: 0 auto; padding: 32px 20px 48px; color: #172033; }
    .hero { background: linear-gradient(120deg, #17324d, #1d6b70); color: white; border-radius: 24px; padding: 32px; display: flex; justify-content: space-between; gap: 24px; align-items: center; box-shadow: 0 18px 40px #17324d24; }
    .eyebrow { font-size: 11px; letter-spacing: .12em; font-weight: 800; opacity: .75; }
    h1 { margin: 8px 0; font-size: clamp(28px, 4vw, 40px); letter-spacing: -.04em; } .hero p { margin: 0; opacity: .86; max-width: 570px; }
    .identity { display: flex; align-items: center; gap: 12px; flex-shrink: 0; } .avatar { width: 44px; height: 44px; border-radius: 50%; display:grid; place-items:center; background:#ffffff24; font-weight:800; }
    .identity div { display:flex; flex-direction:column; gap:3px; font-size: 13px; } .identity span:not(.avatar) { opacity:.75; font-size:11px; font-weight:700; }
    .metrics { display:grid; grid-template-columns:repeat(3, 1fr); gap:14px; margin: 22px 0; }.metrics article { background:#fff; border:1px solid #e8edf3; border-radius:16px; padding:18px; display:flex; flex-direction:column; gap:4px; }.metrics span, .metrics small, .panel p { color:#64748b; font-size:13px; }.metrics strong { font-size:28px; letter-spacing:-.04em; color:#172033; }
    .content { display:grid; grid-template-columns: 1.6fr 1fr; gap:18px; }.panel { background:#fff; border:1px solid #e8edf3; border-radius:18px; padding:22px; }.panel h2 { margin:0; font-size:18px; letter-spacing:-.02em; }.panel p { margin:6px 0 0; line-height:1.45; }.panel-heading { display:flex; justify-content:space-between; gap:16px; align-items:flex-start; margin-bottom:18px; }.panel-heading a { color:#2563eb; font-size:13px; font-weight:800; text-decoration:none; white-space:nowrap; }
    .booking-list { display:flex; flex-direction:column; }.booking-row { display:flex; align-items:center; gap:12px; padding:12px 0; border-top:1px solid #eef2f6; }.booking-row span:nth-child(2) { display:flex; flex-direction:column; gap:4px; flex:1; }.booking-row strong { font-size:14px; }.booking-row small { color:#64748b; font-size:12px; }.calendar { width:42px; height:42px; border-radius:10px; background:#edf8f5; color:#0f766e; display:flex; flex-direction:column; align-items:center; justify-content:center; }.calendar b { font-size:16px; }.calendar small { color:inherit; font-size:10px; font-weight:800; text-transform:uppercase; }.empty { margin-top:14px; border:1px dashed #cbd5e1; border-radius:12px; padding:20px; color:#64748b; font-size:13px; text-align:center; }
    .listings-panel { margin-top:18px; }.property-grid { display:grid; grid-template-columns:repeat(3,minmax(0,1fr)); gap:14px; }.property-card { min-width:0; overflow:hidden; color:inherit; text-decoration:none; border:1px solid #e8edf3; border-radius:12px; }.property-photo { aspect-ratio:16/10; background:#edf2f4; }.property-photo img { width:100%; height:100%; object-fit:cover; display:block; }.property-placeholder { height:100%; display:grid; place-items:center; color:#176b70; background:#dcecea; font-size:32px; font-weight:800; }.property-info { padding:13px; }.property-info h3 { margin:0; font-size:15px; }.property-info p { margin:5px 0 12px; }.property-meta { display:flex; justify-content:space-between; align-items:center; gap:8px; font-size:13px; }.property-meta span { color:#b45309; }.property-meta strong { white-space:nowrap; }.property-meta small { color:#64748b; font-weight:500; }
    .action-panel > p { margin-bottom:12px; }.action { display:flex; align-items:center; gap:10px; padding:13px 0; border-top:1px solid #eef2f6; color:inherit; text-decoration:none; }.action > span { width:31px; height:31px; border-radius:9px; background:#f1f5f9; display:grid; place-items:center; color:#334155; font-weight:800; }.action div { display:flex; flex-direction:column; gap:3px; flex:1; }.action strong { font-size:13px; }.action small { color:#64748b; font-size:11px; }.action > b { color:#94a3b8; font-size:22px; }.loading, .error { margin:24px 0; padding:14px; border-radius:12px; }.loading { color:#475569; background:#f8fafc; }.error { color:#b91c1c; background:#fef2f2; }
    @media (max-width: 760px) { .hero { align-items:flex-start; flex-direction:column; padding:26px; }.metrics, .content { grid-template-columns:1fr; } }
  `],
})
export class DashboardComponent implements OnInit {
  bookings: Booking[] = [];
  properties: Property[] = [];
  loading = true;
  error = '';
  readonly email = this.tokenService.getEmail() || 'Signed-in member';

  constructor(private bookingService: BookingService, private propertyService: PropertyService, private tokenService: TokenService) {}

  get roleLabel() { return 'Tenant'; }
  get initials() { return this.email.split('@')[0].slice(0, 2).toUpperCase(); }
  get upcomingBookings() { return this.bookings.filter((booking) => booking.checkOut >= new Date().toISOString().slice(0, 10)); }

  ngOnInit() {
    forkJoin({ bookings: this.bookingService.getMyBookings(), properties: this.propertyService.getProperties() }).subscribe({
      next: ({ bookings, properties }) => { this.bookings = bookings; this.properties = properties; this.loading = false; },
      error: (err) => { this.error = err?.error?.message ?? 'Some dashboard information could not be loaded.'; this.loading = false; },
    });
  }
}
