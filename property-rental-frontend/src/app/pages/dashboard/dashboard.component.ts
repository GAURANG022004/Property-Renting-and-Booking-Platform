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
      <div class="hero" [class.admin-hero]="isAdmin">
        <div>
          <span class="eyebrow">{{ isAdmin ? 'ADMIN WORKSPACE' : 'YOUR RENTAL HUB' }}</span>
          <h1>{{ isAdmin ? 'Keep the marketplace moving.' : 'Welcome back.' }}</h1>
          <p>{{ isAdmin ? 'Review the live catalogue and manage your own listing activity.' : 'Plan your next stay, revisit bookings, or publish a property.' }}</p>
        </div>
        <div class="identity">
          <span class="avatar">{{ initials }}</span>
          <div><strong>{{ email }}</strong><span>{{ roleLabel }}</span></div>
        </div>
      </div>

      <p *ngIf="error" class="error">{{ error }}</p>

      <div class="metrics" *ngIf="!loading">
        <article><span>{{ isAdmin ? 'Live listings' : 'Total bookings' }}</span><strong>{{ isAdmin ? properties.length : bookings.length }}</strong><small>{{ isAdmin ? 'Visible in the catalogue' : 'Saved to your account' }}</small></article>
        <article><span>{{ isAdmin ? 'Catalogue quality' : 'Upcoming stays' }}</span><strong>{{ isAdmin ? averageRating : upcomingBookings.length }}</strong><small>{{ isAdmin ? 'Average property rating' : 'Check-ins still ahead' }}</small></article>
        <article><span>{{ isAdmin ? 'Your booking history' : 'Places to explore' }}</span><strong>{{ isAdmin ? bookings.length : properties.length }}</strong><small>{{ isAdmin ? 'Personal reservations' : 'Available listings' }}</small></article>
      </div>

      <div *ngIf="loading" class="loading">Preparing your dashboard…</div>

      <div class="content" *ngIf="!loading">
        <section class="panel primary-panel">
          <div class="panel-heading">
            <div><h2>{{ isAdmin ? 'Catalogue snapshot' : 'Your upcoming stays' }}</h2><p>{{ isAdmin ? 'A quick view of the latest properties on the platform.' : 'Everything you have coming up in one place.' }}</p></div>
            <a [routerLink]="isAdmin ? '/properties' : '/my-bookings'">View all</a>
          </div>

          <ng-container *ngIf="isAdmin; else userBookings">
            <div class="property-list" *ngIf="properties.length; else noProperties">
              <a *ngFor="let property of properties.slice(0, 4)" [routerLink]="['/properties', property.id]" class="property-row">
                <span class="property-image">{{ property.title.charAt(0) }}</span>
                <span><strong>{{ property.title }}</strong><small>{{ property.location }} · ★ {{ property.rating }}</small></span>
                <b>₹{{ property.pricePerNight }}<small>/night</small></b>
              </a>
            </div>
          </ng-container>

          <ng-template #userBookings>
            <div class="booking-list" *ngIf="upcomingBookings.length; else noBookings">
              <div *ngFor="let booking of upcomingBookings.slice(0, 3)" class="booking-row">
                <span class="calendar"><b>{{ booking.checkIn | date:'dd' }}</b><small>{{ booking.checkIn | date:'MMM' }}</small></span>
                <span><strong>{{ booking.propertyTitle }}</strong><small>{{ booking.location }} · {{ booking.checkIn }} to {{ booking.checkOut }}</small></span>
              </div>
            </div>
          </ng-template>

          <ng-template #noBookings><div class="empty">No upcoming stays yet. Start exploring when you are ready.</div></ng-template>
          <ng-template #noProperties><div class="empty">No properties are available yet.</div></ng-template>
        </section>

        <aside class="panel action-panel">
          <h2>{{ isAdmin ? 'Admin shortcuts' : 'What would you like to do?' }}</h2>
          <p>{{ isAdmin ? 'Use these essentials to keep listings current.' : 'A few quick ways to make progress.' }}</p>
          <a routerLink="/properties" class="action"><span>⌕</span><div><strong>Browse properties</strong><small>Search the latest homes</small></div><b>›</b></a>
          <a routerLink="/owner/add-property" class="action"><span>＋</span><div><strong>Add a property</strong><small>{{ isAdmin ? 'Create a new listing' : 'Share a place with guests' }}</small></div><b>›</b></a>
          <a routerLink="/my-bookings" class="action"><span>□</span><div><strong>My bookings</strong><small>Review reservation details</small></div><b>›</b></a>
        </aside>
      </div>
    </section>
  `,
  styles: [`
    .page { max-width: 1120px; margin: 0 auto; padding: 32px 20px 48px; color: #172033; }
    .hero { background: linear-gradient(120deg, #17324d, #1d6b70); color: white; border-radius: 24px; padding: 32px; display: flex; justify-content: space-between; gap: 24px; align-items: center; box-shadow: 0 18px 40px #17324d24; }
    .admin-hero { background: linear-gradient(120deg, #312e81, #7c3aed); }
    .eyebrow { font-size: 11px; letter-spacing: .12em; font-weight: 800; opacity: .75; }
    h1 { margin: 8px 0; font-size: clamp(28px, 4vw, 40px); letter-spacing: -.04em; } .hero p { margin: 0; opacity: .86; max-width: 570px; }
    .identity { display: flex; align-items: center; gap: 12px; flex-shrink: 0; } .avatar { width: 44px; height: 44px; border-radius: 50%; display:grid; place-items:center; background:#ffffff24; font-weight:800; }
    .identity div { display:flex; flex-direction:column; gap:3px; font-size: 13px; } .identity span:not(.avatar) { opacity:.75; font-size:11px; font-weight:700; }
    .metrics { display:grid; grid-template-columns:repeat(3, 1fr); gap:14px; margin: 22px 0; }.metrics article { background:#fff; border:1px solid #e8edf3; border-radius:16px; padding:18px; display:flex; flex-direction:column; gap:4px; }.metrics span, .metrics small, .panel p { color:#64748b; font-size:13px; }.metrics strong { font-size:28px; letter-spacing:-.04em; color:#172033; }
    .content { display:grid; grid-template-columns: 1.6fr 1fr; gap:18px; }.panel { background:#fff; border:1px solid #e8edf3; border-radius:18px; padding:22px; }.panel h2 { margin:0; font-size:18px; letter-spacing:-.02em; }.panel p { margin:6px 0 0; line-height:1.45; }.panel-heading { display:flex; justify-content:space-between; gap:16px; align-items:flex-start; margin-bottom:18px; }.panel-heading a { color:#2563eb; font-size:13px; font-weight:800; text-decoration:none; white-space:nowrap; }
    .booking-list, .property-list { display:flex; flex-direction:column; }.booking-row, .property-row { display:flex; align-items:center; gap:12px; padding:12px 0; border-top:1px solid #eef2f6; }.booking-row span:nth-child(2), .property-row span:nth-child(2) { display:flex; flex-direction:column; gap:4px; flex:1; }.booking-row strong, .property-row strong { font-size:14px; }.booking-row small, .property-row small { color:#64748b; font-size:12px; }.calendar { width:42px; height:42px; border-radius:10px; background:#edf8f5; color:#0f766e; display:flex; flex-direction:column; align-items:center; justify-content:center; }.calendar b { font-size:16px; }.calendar small { color:inherit; font-size:10px; font-weight:800; text-transform:uppercase; }.property-row { text-decoration:none; color:inherit; }.property-image { width:38px; height:38px; border-radius:10px; background:#e0e7ff; color:#4338ca; display:grid; place-items:center; font-weight:900; }.property-row b { font-size:13px; text-align:right; }.property-row b small { display:block; font-weight:500; }.empty { margin-top:14px; border:1px dashed #cbd5e1; border-radius:12px; padding:20px; color:#64748b; font-size:13px; text-align:center; }
    .action-panel > p { margin-bottom:12px; }.action { display:flex; align-items:center; gap:10px; padding:13px 0; border-top:1px solid #eef2f6; color:inherit; text-decoration:none; }.action > span { width:31px; height:31px; border-radius:9px; background:#f1f5f9; display:grid; place-items:center; color:#334155; font-weight:800; }.action div { display:flex; flex-direction:column; gap:3px; flex:1; }.action strong { font-size:13px; }.action small { color:#64748b; font-size:11px; }.action > b { color:#94a3b8; font-size:22px; }.loading, .error { margin:24px 0; padding:14px; border-radius:12px; }.loading { color:#475569; background:#f8fafc; }.error { color:#b91c1c; background:#fef2f2; }
    @media (max-width: 760px) { .hero { align-items:flex-start; flex-direction:column; padding:26px; }.metrics, .content { grid-template-columns:1fr; } }
  `],
})
export class DashboardComponent implements OnInit {
  bookings: Booking[] = [];
  properties: Property[] = [];
  loading = true;
  error = '';
  readonly isAdmin = this.tokenService.getRoles().includes('ADMIN');
  readonly email = this.tokenService.getEmail() || 'Signed-in member';

  constructor(private bookingService: BookingService, private propertyService: PropertyService, private tokenService: TokenService) {}

  get roleLabel() { return this.isAdmin ? 'Administrator' : 'Member'; }
  get initials() { return this.email.split('@')[0].slice(0, 2).toUpperCase(); }
  get upcomingBookings() { return this.bookings.filter((booking) => booking.checkOut >= new Date().toISOString().slice(0, 10)); }
  get averageRating() { return this.properties.length ? (this.properties.reduce((sum, property) => sum + property.rating, 0) / this.properties.length).toFixed(1) : '—'; }

  ngOnInit() {
    forkJoin({ bookings: this.bookingService.getMyBookings(), properties: this.propertyService.getProperties() }).subscribe({
      next: ({ bookings, properties }) => { this.bookings = bookings; this.properties = properties; this.loading = false; },
      error: (err) => { this.error = err?.error?.message ?? 'Some dashboard information could not be loaded.'; this.loading = false; },
    });
  }
}
