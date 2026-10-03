import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { OwnerService, OwnerDashboard, AvailabilityBlock, AvailabilityWindow } from '../../services/owner.service';
import { Property, PropertyService } from '../../services/property.service';
import { Booking } from '../../services/booking.service';
import { forkJoin, map, of, switchMap } from 'rxjs';

@Component({
  selector: 'app-owner-workspace', standalone: true, imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="page"><header><div><span class="eyebrow">OWNER WORKSPACE</span><h1>Manage your rentals</h1><p>Keep listings, booking requests, and unavailable dates in one place.</p></div><a routerLink="/owner/add-property" class="button">Add property</a></header>
    <p class="error" *ngIf="error">{{ error }}</p><p class="success" *ngIf="success">{{ success }}</p><div class="loading" *ngIf="loading">Loading your workspace…</div>
    <ng-container *ngIf="!loading"><div class="metrics"><article><small>Properties</small><b>{{ dashboard.totalProperties }}</b></article><article><small>Approved</small><b>{{ dashboard.activeProperties }}</b></article><article><small>Pending requests</small><b>{{ dashboard.pendingBookingRequests }}</b></article><article><small>Estimated earnings</small><b>₹{{ dashboard.estimatedEarnings }}</b></article></div>
    <nav><button [class.selected]="tab === 'properties'" (click)="tab='properties'">Listings</button><button [class.selected]="tab === 'bookings'" (click)="tab='bookings'">Booking requests</button><button [class.selected]="tab === 'windows'" (click)="tab='windows'">Available periods</button><button [class.selected]="tab === 'availability'" (click)="tab='availability'">Blocked dates</button></nav>
    <section *ngIf="tab === 'properties'" class="panel"><h2>Your listings</h2><div class="empty" *ngIf="!properties.length">You have not created a listing yet.</div><article class="row" *ngFor="let property of properties"><div><strong>{{ property.title }}</strong><small>{{ property.location }} · ₹{{ property.pricePerNight }}/night</small></div><span class="status">{{ property.approvalStatus }}</span><button (click)="startEditing(property)">Edit</button><button *ngIf="property.approvalStatus !== 'INACTIVE'" class="danger" (click)="deactivate(property)" [disabled]="deactivatingPropertyId === property.id">{{ deactivatingPropertyId === property.id ? 'Deactivating…' : 'Deactivate' }}</button><button *ngIf="property.approvalStatus === 'INACTIVE'" (click)="activate(property)" [disabled]="activatingPropertyId === property.id">{{ activatingPropertyId === property.id ? 'Reactivating…' : 'Reactivate' }}</button></article><form *ngIf="editing" class="edit-form" (ngSubmit)="updateProperty()"><div class="edit-header"><div><span class="eyebrow small">Edit listing</span><h3>{{ editing.title || 'Property details' }}</h3></div><button type="button" class="secondary-button" (click)="cancelEditing()">Cancel</button></div><div class="form-grid"><label class="field full"><span>Property title</span><input name="editTitle" [(ngModel)]="editing!.title" required></label><label class="field full"><span>Description</span><textarea name="editDescription" rows="4" [(ngModel)]="editing!.description" required></textarea></label><label class="field"><span>Location</span><input name="editLocation" [(ngModel)]="editing!.location" required></label><label class="field"><span>Price per night (₹)</span><input name="editPrice" type="number" min="0" step="0.01" [(ngModel)]="editing!.pricePerNight" required></label><label class="field full"><span>Map link</span><input name="editMapUrl" type="url" [(ngModel)]="editing!.mapUrl" placeholder="https://maps.app.goo.gl/..." pattern="https://((www[.])?google[.][a-z.]+/maps.*|maps[.]google[.][a-z.]+.*|maps[.]app[.]goo[.]gl/.*|goo[.]gl/maps/.*)"></label><label class="field"><span>Max guests</span><input name="editMaxGuests" type="number" min="1" max="50" [(ngModel)]="editing!.maxGuests" required></label><label class="field"><span>Check-in time</span><input name="editCheckInTime" type="time" [(ngModel)]="editing!.checkInTime" required></label><label class="field"><span>Check-out time</span><input name="editCheckOutTime" type="time" [(ngModel)]="editing!.checkOutTime" required></label><label class="field full"><span>House rules</span><textarea name="editHouseRules" rows="3" [(ngModel)]="editing!.houseRules" placeholder="Smoking, parties, pets, etc."></textarea></label><input name="editCancellationHours" type="number" min="0" max="720" [(ngModel)]="editing!.cancellationFreeHours" required><input name="editRefundBefore" type="number" min="0" max="100" [(ngModel)]="editing!.refundPercentBeforeDeadline" required><input name="editRefundWithin" type="number" min="0" max="100" [(ngModel)]="editing!.refundPercentWithinDeadline" required><label class="field"><span>Refund after check-in (%)</span><input name="editRefundAfter" type="number" min="0" max="100" [(ngModel)]="editing!.refundPercentAfterCheckIn" required></label></div><label class="field full image-replacement"><span>Property images</span><input name="editImages" type="file" accept="image/*" multiple (change)="onImagesSelected($event)"><small>Current gallery: {{ editing!.imageUrls.length }} image(s). Select image(s) to replace it, or leave empty to keep it unchanged.</small><small *ngIf="selectedImageFiles.length">{{ selectedImageFiles.length }} replacement image(s) selected.</small></label><div class="form-actions"><button type="button" class="secondary-button danger" (click)="cancelEditing()">Cancel</button><button type="submit" [disabled]="savingPropertyId === editing.id">{{ savingPropertyId === editing.id ? 'Saving…' : 'Save listing' }}</button></div></form></section>
    <section *ngIf="tab === 'bookings'" class="panel"><h2>Booking requests and history</h2><div class="empty" *ngIf="!bookings.length">No bookings for your properties yet.</div><article class="row booking-row" *ngFor="let booking of bookings"><div><strong>{{ booking.propertyTitle }}</strong><small>{{ booking.checkIn }} to {{ booking.checkOut }} · {{ booking.location }}</small><small>Total rent ₹{{ booking.totalAmount | number:'1.2-2' }}</small></div><span class="status">{{ statusLabel(booking.status) }}</span><ng-container *ngIf="booking.status === 'PENDING'"><button (click)="decide(booking, 'ACCEPTED')" [disabled]="processingBookingId === booking.id">{{ processingBookingId === booking.id ? 'Updating…' : 'Accept request' }}</button><button class="danger" (click)="decide(booking, 'REJECTED')" [disabled]="processingBookingId === booking.id">Reject</button></ng-container><button *ngIf="booking.status === 'CONFIRMED'" (click)="checkInGuest(booking)" [disabled]="processingBookingId === booking.id">{{ processingBookingId === booking.id ? 'Checking in…' : 'Check in guest' }}</button><button *ngIf="booking.status === 'ACTIVE'" (click)="completeCheckout(booking)" [disabled]="processingBookingId === booking.id">{{ processingBookingId === booking.id ? 'Completing…' : 'Check out guest' }}</button></article></section>
    <section *ngIf="tab === 'windows'" class="panel"><h2>Available periods</h2><p>Tenants whose stay dates fit inside one of these periods can book instantly after availability checks.</p><form class="block-form" (ngSubmit)="addWindow()"><select name="windowPropertyId" [(ngModel)]="windowDraft.propertyId" required><option [ngValue]="null">Choose a property</option><option *ngFor="let property of properties" [ngValue]="property.id">{{ property.title }}</option></select><input name="windowStart" type="date" [(ngModel)]="windowDraft.startDate" required><input name="windowEnd" type="date" [(ngModel)]="windowDraft.endDate" required><button [disabled]="!windowDraft.propertyId">Add available period</button></form><div class="empty" *ngIf="!availabilityWindows.length">No instant-booking periods defined. Requests will need your approval.</div><article class="row" *ngFor="let item of availabilityWindows"><div><strong>{{ item.propertyTitle }}</strong><small>{{ item.startDate }} to {{ item.endDate }}</small></div><button class="danger" (click)="removeWindow(item.id)">Remove period</button></article></section>
    <section *ngIf="tab === 'availability'" class="panel"><h2>Block unavailable dates</h2><form class="block-form" (ngSubmit)="addBlock()"><select name="propertyId" [(ngModel)]="block.propertyId" required><option [ngValue]="null">Choose a property</option><option *ngFor="let property of properties" [ngValue]="property.id">{{ property.title }}</option></select><input name="start" type="date" [(ngModel)]="block.startDate" required><input name="end" type="date" [(ngModel)]="block.endDate" required><input name="reason" placeholder="Reason (optional)" [(ngModel)]="block.reason"><button [disabled]="!block.propertyId">Block dates</button></form><div class="empty" *ngIf="!availability.length">No unavailable periods saved.</div><article class="row" *ngFor="let item of availability"><div><strong>{{ propertyName(item.propertyId) }}</strong><small>{{ item.startDate }} to {{ item.endDate }}{{ item.reason ? ' · ' + item.reason : '' }}</small></div><button class="danger" (click)="removeBlock(item.id)">Remove</button></article></section></ng-container></section>`,
  styles: [`
    .page{max-width:1120px;margin:auto;padding:32px 20px;color:#172033}header{display:flex;justify-content:space-between;gap:16px;align-items:center;background:#153b4b;color:white;padding:28px;border-radius:22px}h1{margin:5px 0;font-size:32px}header p{margin:0;opacity:.85}.eyebrow{font-size:11px;font-weight:800;letter-spacing:.12em}.eyebrow.small{font-size:10px}.button,button{border:0;border-radius:10px;padding:10px 14px;background:#146b75;color:white;font-weight:750;cursor:pointer;text-decoration:none;transition:all .2s ease}.button{background:white;color:#153b4b}.secondary-button{background:#e2e8f0;color:#0f172a}.secondary-button.danger{background:#fee2e2;color:#b42318}.button:hover,button:hover{filter:brightness(0.98)}button:disabled{opacity:.55;cursor:not-allowed}.metrics{display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin:20px 0}.metrics article,.panel{border:1px solid #e2e8f0;background:white;border-radius:14px;padding:16px}.metrics small,.row small{display:block;color:#64748b}.metrics b{font-size:25px}nav{display:flex;gap:8px;margin:18px 0}nav button{background:#e8eef2;color:#334155}nav .selected{background:#153b4b;color:white}.panel h2{margin-top:0}.row{display:flex;align-items:center;gap:10px;border-top:1px solid #edf2f7;padding:13px 0}.row>div{flex:1}.booking-row small{margin-top:4px}.status{font-size:12px;font-weight:800;background:#eef6ff;padding:5px 8px;border-radius:999px}.danger{background:#b42318}.empty{padding:18px;color:#64748b;text-align:center}.block-form{display:grid;grid-template-columns:1.5fr 1fr 1fr 1.5fr auto;gap:8px;margin-bottom:14px}.block-form input,.block-form select,.field input,.field textarea{padding:11px 12px;border:1px solid #cbd5e1;border-radius:10px;background:#fff;font-size:15px;box-sizing:border-box}.block-form input,.block-form select{width:100%}.edit-form{display:flex;flex-direction:column;gap:18px;margin-top:20px;padding:20px;border:1px solid #dfe7f0;border-radius:16px;background:linear-gradient(180deg,#ffffff 0%,#f8fafc 100%)}.edit-header{display:flex;justify-content:space-between;align-items:center;gap:12px}.edit-header h3{margin:6px 0 0;font-size:22px}.form-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px}.field{display:flex;flex-direction:column;gap:8px;font-weight:600;color:#0f172a}.field.full{grid-column:1 / -1}.field span{font-size:12px;letter-spacing:.04em;color:#475569;text-transform:uppercase}.field textarea{min-height:120px;resize:vertical}.form-actions{display:flex;justify-content:flex-end;gap:12px}.loading,.error,.success{margin:18px 0;padding:12px}.error{color:#b42318;background:#fef2f2}.success{color:#166534;background:#f0fdf4}@media(max-width:720px){.metrics{grid-template-columns:repeat(2,1fr)}.block-form,.form-grid{grid-template-columns:1fr}.row{align-items:flex-start;flex-wrap:wrap}header{align-items:flex-start;flex-direction:column}.edit-header,.form-actions{flex-direction:column;align-items:stretch}.form-actions button{width:100%}}
  `]
})
export class OwnerWorkspaceComponent implements OnInit {
  dashboard: OwnerDashboard = { totalProperties: 0, activeProperties: 0, pendingProperties: 0, pendingBookingRequests: 0, acceptedBookings: 0, completedStays: 0, estimatedEarnings: 0 };
  properties: Property[] = []; bookings: Booking[] = []; availability: AvailabilityBlock[] = []; availabilityWindows: AvailabilityWindow[] = []; tab = 'properties'; loading = true; error = ''; success = ''; editing: Property | null = null; selectedImageFiles: File[] = []; savingPropertyId: number | null = null; deactivatingPropertyId: number | null = null; activatingPropertyId: number | null = null; processingBookingId: number | null = null;
  block: { propertyId: number | null; startDate: string; endDate: string; reason: string } = { propertyId: null, startDate: '', endDate: '', reason: '' };
  windowDraft: { propertyId: number | null; startDate: string; endDate: string } = { propertyId: null, startDate: '', endDate: '' };
  constructor(private owner: OwnerService, private propertyService: PropertyService) {}
  ngOnInit() { this.reload(); }
  reload() { this.loading = true; this.error = ''; this.success = ''; forkJoin({ dashboard: this.owner.dashboard(), properties: this.owner.properties(), bookings: this.owner.bookings(), availability: this.owner.availability(), availabilityWindows: this.owner.availabilityWindows() }).subscribe({ next: data => { this.dashboard=data.dashboard; this.properties=data.properties; this.bookings=data.bookings; this.availability=data.availability; this.availabilityWindows=data.availabilityWindows; this.loading=false; }, error: e => this.fail(e) }); }
  fail(e: any) { this.error=e?.error?.message ?? 'Unable to load owner data.'; this.loading=false; }
  decide(booking: Booking, status: 'ACCEPTED'|'REJECTED') { this.processingBookingId=booking.id; this.owner.decideBooking(booking.id,status).subscribe({next:()=>{this.processingBookingId=null;this.reload()},error:e=>{this.processingBookingId=null;this.fail(e)}}); }
  checkInGuest(booking: Booking) { this.processingBookingId=booking.id; this.owner.checkIn(booking.id).subscribe({next:()=>{this.processingBookingId=null;this.reload()},error:e=>{this.processingBookingId=null;this.fail(e)}}); }
  completeCheckout(booking: Booking) { this.processingBookingId=booking.id; this.owner.completeCheckout(booking.id).subscribe({next:()=>{this.processingBookingId=null;this.reload()},error:e=>{this.processingBookingId=null;this.fail(e)}}); }
  deactivate(property: Property) {
    if (this.deactivatingPropertyId !== null || property.approvalStatus === 'INACTIVE') return;
    this.error = '';
    this.success = '';
    this.deactivatingPropertyId = property.id;
    this.owner.deactivateProperty(property.id).subscribe({
      next: () => {
        if (property.approvalStatus === 'APPROVED') this.dashboard.activeProperties = Math.max(0, this.dashboard.activeProperties - 1);
        property.approvalStatus = 'INACTIVE';
        this.success = `${property.title} has been deactivated and is no longer visible to renters.`;
        this.deactivatingPropertyId = null;
      },
      error: e => {
        this.deactivatingPropertyId = null;
        this.fail(e);
      },
    });
  }
  startEditing(property: Property) { this.error = ''; this.success = ''; this.selectedImageFiles = []; this.editing = { ...property }; }
  cancelEditing() { this.editing = null; this.selectedImageFiles = []; }
  onImagesSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedImageFiles = input.files ? Array.from(input.files) : [];
  }
  updateProperty() {
    if (!this.editing || this.savingPropertyId !== null) return;
    const draft = this.editing;
    const selectedImages = this.selectedImageFiles;
    const { id, title, description, location, pricePerNight, mapUrl, maxGuests, checkInTime, checkOutTime, houseRules, cancellationFreeHours, refundPercentBeforeDeadline, refundPercentWithinDeadline, refundPercentAfterCheckIn } = draft;
    this.error = '';
    this.success = '';
    this.savingPropertyId = id;
    this.owner.updateProperty(id, { title, description, location, pricePerNight, mapUrl: mapUrl ?? '', maxGuests, checkInTime, checkOutTime, houseRules, cancellationFreeHours, refundPercentBeforeDeadline, refundPercentWithinDeadline, refundPercentAfterCheckIn }).pipe(
      switchMap(updated => selectedImages.length
        ? this.propertyService.replacePropertyImages(id, selectedImages).pipe(map(imageUrls => ({ ...updated, imageUrls })))
        : of(updated))
    ).subscribe({
      next: updated => {
        this.properties = this.properties.map(property => property.id === updated.id ? updated : property);
        this.editing = null;
        this.selectedImageFiles = [];
        this.savingPropertyId = null;
        this.success = `${updated.title} was updated.`;
      },
      error: e => { this.savingPropertyId = null; this.fail(e); },
    });
  }
  activate(property: Property) {
    if (this.activatingPropertyId !== null || property.approvalStatus !== 'INACTIVE') return;
    this.error = '';
    this.success = '';
    this.activatingPropertyId = property.id;
    this.owner.activateProperty(property.id).subscribe({
      next: updated => {
        this.properties = this.properties.map(item => item.id === updated.id ? updated : item);
        this.activatingPropertyId = null;
        if (updated.approvalStatus === 'APPROVED') this.dashboard.activeProperties += 1;
        this.success = updated.approvalStatus === 'APPROVED'
          ? `${updated.title} is active again.`
          : `${updated.title} was reactivated and is awaiting approval.`;
      },
      error: e => { this.activatingPropertyId = null; this.fail(e); },
    });
  }
  addBlock() { if (!this.block.propertyId) return; this.owner.blockAvailability({ propertyId:this.block.propertyId,startDate:this.block.startDate,endDate:this.block.endDate,reason:this.block.reason }).subscribe({next:()=>{this.block={propertyId:null,startDate:'',endDate:'',reason:''};this.reload()},error:e=>this.fail(e)}); }
  removeBlock(id:number) { this.owner.removeAvailability(id).subscribe({next:()=>this.reload(),error:e=>this.fail(e)}); }
  addWindow() { if (!this.windowDraft.propertyId) return; this.owner.addAvailabilityWindow(this.windowDraft as { propertyId: number; startDate: string; endDate: string }).subscribe({next:()=>{this.windowDraft={propertyId:null,startDate:'',endDate:''};this.reload()},error:e=>this.fail(e)}); }
  removeWindow(id:number) { this.owner.removeAvailabilityWindow(id).subscribe({next:()=>this.reload(),error:e=>this.fail(e)}); }
  propertyName(id:number) { return this.properties.find(p=>p.id===id)?.title ?? `Property #${id}`; }
  statusLabel(status: string) { return status.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, letter => letter.toUpperCase()); }
}
