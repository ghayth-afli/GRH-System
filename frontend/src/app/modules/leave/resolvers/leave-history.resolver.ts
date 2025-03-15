import {
  ActivatedRouteSnapshot,
  Resolve,
  ResolveFn,
  RouterStateSnapshot,
} from '@angular/router';
import { Leave } from '../models/leave';
import { LeaveService } from '../services/leave.service';
import { inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class LeaveHistoryResolver implements Resolve<Leave[]> {
  constructor(private leaveRequestService: LeaveService) {}
  authService = inject(AuthService);

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<Leave[]> {
    if (this.authService.hasRole('Employee')) {
      return this.leaveRequestService.getLeaveHistory();
    }
    return of([]);
  }
}
