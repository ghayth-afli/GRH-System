import { Component, HostListener, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/auth-responses.interface';
import { map, Observable } from 'rxjs';
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
  isDropdownOpen = false;
  notifications = [
    {
      title: 'Notification 1',
      content: 'This is the content of notification 1',
      icon: 'assets/icons/icon1.png',
      seen: true,
    },
    {
      title: 'Notification 2',
      content: 'This is the content of notification 2',
      icon: 'assets/icons/icon2.png',
      seen: false,
    },
    {
      title: 'Notification 3',
      content: 'This is the content of notification 3',
      icon: 'assets/icons/icon3.png',
      seen: true,
    },
  ];

  get unreadNotificationsCount(): number {
    return this.notifications.filter((notification) => !notification.seen)
      .length;
  }

  user: User = {
    id: '',
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    department: '',
    role: '',
  };

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
    if (this.isDropdownOpen) {
      this.markAllAsSeen();
    }
  }

  markAllAsSeen() {
    this.notifications.forEach((notification) => (notification.seen = true));
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    const notificationContainer = document.querySelector(
      '.notification-container'
    );
    if (notificationContainer && !notificationContainer.contains(target)) {
      this.isDropdownOpen = false;
    }
  }

  ngOnDestroy() {
    document.removeEventListener('click', this.onDocumentClick);
  }

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
