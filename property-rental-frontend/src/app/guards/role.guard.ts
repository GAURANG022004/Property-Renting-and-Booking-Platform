import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { TokenService } from '../services/token.service';

@Injectable({
  providedIn: 'root',
})
export class RoleGuard implements CanActivate {
  constructor(private tokenService: TokenService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    const requiredRoles: string[] | undefined = route?.data?.['roles'];
    if (!requiredRoles || requiredRoles.length === 0) return true;

    const userRoles = this.tokenService.getRoles();
    const allowed = requiredRoles.some((r) => userRoles.includes(r));

    if (!allowed) {
      this.router.navigate(['/login']);
      return false;
    }
    return true;
  }
}

