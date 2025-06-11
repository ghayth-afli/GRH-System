import {
  Component,
  HostListener,
  inject,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/auth-responses.interface';
import { BehaviorSubject, map, Observable } from 'rxjs';
import { LeaveService } from '../../modules/leave/services/leave.service';
import { ActivatedRoute } from '@angular/router';
import { LeaveBalance } from '../../modules/leave/models/leave-balance';
import { UserService } from '../../core/services/user.service';
import { ProfilePicture } from '../../core/models/ProfilePicture';
import { MatDialog } from '@angular/material/dialog';
import { EditPersonalInfoModalFormComponent } from '../../shared/components/edit-personal-info-modal-form/edit-personal-info-modal-form.component';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-header',
  standalone: false,
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  isDropdownOpen = false;
  showBadge = true;
  imageSrc: string | null = null;

  // Notification-related properties from the original component
  notificationsSubject = new BehaviorSubject<any[]>([]);

  user: User = {
    id: '',
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    department: '',
    role: '',
    jobTitle: '',
    phoneNumber1: '',
    phoneNumber2: '',
  };

  // Injected services
  notificationService = inject(NotificationService);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);
  private dialog = inject(MatDialog);
  private userService = inject(UserService);

  // Static notifications for fallback (keeping your existing data)
  notifications = [
    {
      message: 'New application from MOHAMED GHAYTH AFLI',
      time: 'May 13, 2025, 2:30 PM',
      unread: true,
      url: '/applications/12345',
    },
    {
      message: 'Interview scheduled for Jane Smith',
      time: 'May 12, 2025, 10:15 AM',
      unread: true,
      url: '/interviews/12345',
    },
    {
      message: "David Johnson's application reviewed",
      time: 'May 11, 2025, 4:00 PM',
      unread: false,
      url: '/applications/67890',
    },
  ];

  ngOnInit(): void {
    this.initializeUser();
    this.loadProfilePicture();
    this.initializeNotifications();
  }

  ngOnDestroy(): void {
    document.removeEventListener('click', this.onDocumentClick);
  }

  private initializeUser(): void {
    this.user = this.authService.authenticatedUser || this.user;
  }

  private loadProfilePicture(): void {
    this.userService.getProfilePicture(this.user.username).subscribe({
      next: (response) => {
        this.imageSrc = this.getBase64Image(response);
      },
      error: (error) => {
        console.error(error);
      },
    });
  }

  private getBase64Image(profilePicture: ProfilePicture): string {
    if (profilePicture.base64Image) {
      return profilePicture.base64Image;
    } else {
      const binary = new Uint8Array(profilePicture.picture);
      const base64String = btoa(String.fromCharCode(...binary));
      return `data:image/${profilePicture.type};base64,${base64String}`;
    }
  }

  // Notification-related methods from the original component
  private initializeNotifications(): void {
    this.notificationService.loadNotifications();

    this.notificationService.notifications$.subscribe((notifications) => {
      console.log('Notifications:', notifications);
      this.notificationsSubject.next(notifications);
      // Update the local notifications array with service data
      if (notifications && notifications.length > 0) {
        this.notifications = notifications.map((n: any) => ({
          message: n.message,
          time: n.time || n.createdAt || '', // adjust property as needed
          unread: typeof n.unread === 'boolean' ? n.unread : !n.read, // adjust logic as needed
          url: n.actionUrl || '#', // adjust property as needed
        }));
      }
    });

    this.notificationService.unreadCount$.subscribe((count) => {
      console.log('Unread notifications count:', count);
      this.showBadge = count > 0;
    });
  }

  toggleNotifications(): void {
    this.isDropdownOpen = !this.isDropdownOpen;

    // Mark all notifications as read when dropdown is opened
    if (this.isDropdownOpen) {
      this.notificationService.markAllAsRead().subscribe(() => {
        console.log('All notifications marked as read');
      });
    }
  }

  clearNotifications(): void {
    this.notifications = [];
    this.showBadge = false;
    this.notificationsSubject.next([]);
  }

  // Handle clicks outside the dropdown to close it
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    const notificationContainer = document.querySelector(
      '.notification-dropdown'
    );
    const notificationButton = document.querySelector('.notification-btn');

    if (
      notificationContainer &&
      notificationButton &&
      !notificationContainer.contains(target) &&
      !notificationButton.contains(target)
    ) {
      this.isDropdownOpen = false;
    }
  }
  openEditDialog($event: MouseEvent): void {
    $event.stopPropagation(); // Prevent the click from propagating to the document
    const dialogRef = this.dialog.open(EditPersonalInfoModalFormComponent, {
      width: '400px',
      data: { user: this.user, imageSrc: this.imageSrc },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.user = {
          ...this.user,
          firstName: result.firstName,
          lastName: result.lastName,
          email: result.email,
          jobTitle: result.jobTitle,
          phoneNumber1: result.phoneNumber1,
          phoneNumber2: result.phoneNumber2,
        };
        localStorage.setItem('user', JSON.stringify(this.user));
        this.loadProfilePicture();
      }
    });
  }
}
