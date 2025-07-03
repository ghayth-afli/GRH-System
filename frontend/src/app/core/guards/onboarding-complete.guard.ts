import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class OnboardingCompleteGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    const user = this.authService.authenticatedUser;

    // Check if the user has completed the onboarding process
    if (
      user &&
      user.jobTitle &&
      user.phoneNumber1 &&
      user.gender &&
      user.birthDate
    ) {
      return true;
    } else {
      // Redirect to the onboarding page if not completed
      this.router.navigate(['/onboarding']);
      return false;
    }
  }
}
