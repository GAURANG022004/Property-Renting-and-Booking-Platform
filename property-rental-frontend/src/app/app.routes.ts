import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { PropertyDetailsComponent } from './pages/property-details/property-details.component';
import { BookingComponent } from './pages/booking/booking.component';
import { LoginComponent } from './pages/auth/login.component';
import { RegisterComponent } from './pages/auth/register.component';
import { OwnerAddPropertyComponent } from './pages/owner/owner-add-property.component';
import { MyBookingsComponent } from './pages/my-bookings/my-bookings.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { OwnerWorkspaceComponent } from './pages/owner/owner-workspace.component';
import { AdminWorkspaceComponent } from './pages/admin/admin-workspace.component';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'properties', component: HomeComponent },
  { path: 'properties/:id', component: PropertyDetailsComponent },
  {
    path: 'properties/:id/book',
    component: BookingComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['TENANT'] },
  },

  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['TENANT'] },
  },

  {
    path: 'owner/add-property',
    component: OwnerAddPropertyComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['OWNER'] },
  },
  {
    path: 'my-bookings',
    component: MyBookingsComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['TENANT'] },
  },
  { path: 'owner', component: OwnerWorkspaceComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['OWNER'] } },
  { path: 'admin', component: AdminWorkspaceComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },

  { path: '**', redirectTo: '' }
];
