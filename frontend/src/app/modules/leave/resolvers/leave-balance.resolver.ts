import {
  ActivatedRouteSnapshot,
  Resolve,
  ResolveFn,
  RouterStateSnapshot,
} from '@angular/router';
import { LeaveBalance } from '../models/leave-balance';
import { LeaveService } from '../services/leave.service';
import { Observable, of } from 'rxjs';
import { inject, Injectable } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class LeaveBalanceResolver implements Resolve<LeaveBalance> {
  constructor(private leaveRequestService: LeaveService) {}
  authService = inject(AuthService);

  resolve(): Observable<LeaveBalance> {
    if (this.authService.hasRole('Employee')) {
      return this.leaveRequestService.getLeaveBalance();
    } else {
      return of({} as LeaveBalance);
    }
  }
}
