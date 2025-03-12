import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/auth-responses.interface';
import { LeaveService } from '../../features/dashboard/services/leave.service';
import { map, Observable, of } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: false,

  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  remainingLeave: Observable<number> = of(0);
  totalLeave: Observable<number> = of(0);

  user: User = {
    id: '',
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    department: '',
    role: '',
  };
  authService = inject(AuthService);
  leaveService = inject(LeaveService);

  ngOnInit(): void {
    this.user = this.authService.authenticatedUser || this.user;
    this.remainingLeave = this.leaveService
      .getLeaveBalance()
      .pipe(map((balance) => balance.remainingLeave));
    this.totalLeave = this.leaveService
      .getLeaveBalance()
      .pipe(map((balance) => balance.totalLeave));
  }
}
