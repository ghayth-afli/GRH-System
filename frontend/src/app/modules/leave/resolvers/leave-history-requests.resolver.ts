import { inject, Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  Resolve,
  ResolveFn,
  RouterStateSnapshot,
} from '@angular/router';
import { Leave } from '../models/leave';
import { LeaveService } from '../services/leave.service';
import { AuthService } from '../../../core/services/auth.service';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LeaveRequestsResolver implements Resolve<Leave[]> {
  constructor(private leaveRequestService: LeaveService) {}
  authService = inject(AuthService);

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<Leave[]> {
    if (this.authService.hasRole('Employee')) {
      return this.leaveRequestService.getAllLeaveRequests();
    }
    return of([]);
  }
}
