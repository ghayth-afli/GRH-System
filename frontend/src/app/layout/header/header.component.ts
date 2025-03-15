import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/auth-responses.interface';
import { map, Observable, of } from 'rxjs';
import { LeaveService } from '../../modules/leave/services/leave.service';
import { ActivatedRoute } from '@angular/router';
import { LeaveBalance } from '../../modules/leave/models/leave-balance';

@Component({
  selector: 'app-header',
  standalone: false,

  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  leaveBalance$: Observable<LeaveBalance>;
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

  constructor(private route: ActivatedRoute) {
    this.leaveBalance$ = this.route.data.pipe(
      map((data) => data['leaveBalance'])
    );
  }

  ngOnInit(): void {
    this.user = this.authService.authenticatedUser || this.user;
  }
  isEmployee() {
    return this.authService.hasRole('Employee');
  }
}
