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
  leaveBalance$: Observable<LeaveBalance>;
  imageSrc: string | null = null;
  isDropdownOpen = false;
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

  notificationService = inject(NotificationService);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);
  private dialog = inject(MatDialog);
  private userService = inject(UserService);

  constructor() {
    this.leaveBalance$ = this.route.data.pipe(
      map((data) => data['leaveBalance'])
    );
  }

  ngOnInit(): void {
    this.initializeUser();
    this.loadProfilePicture();
    this.initializeNotifications();
  }

  ngOnDestroy(): void {
    document.removeEventListener('click', this.onDocumentClick);
  }

  // User-related methods
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

  isEmployee(): boolean {
    return this.authService.hasRole('Employee');
  }

  openEditModal(): void {
    const dialogRef = this.dialog.open(EditPersonalInfoModalFormComponent, {
      width: '500px',
      data: {
        firstName: this.user.firstName,
        lastName: this.user.lastName,
        email: this.user.email,
        jobTitle: this.user.jobTitle,
        phoneNumber1: this.user.phoneNumber1,
        phoneNumber2: this.user.phoneNumber2,
        imageSrc: this.imageSrc,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.updateUserInfo(dialogRef);
      }
    });
  }

  private updateUserInfo(dialogRef: any): void {
    this.user = {
      ...this.user,
      firstName: dialogRef.componentInstance.editForm.value.firstName,
      lastName: dialogRef.componentInstance.editForm.value.lastName,
      email: dialogRef.componentInstance.editForm.value.email,
      jobTitle: dialogRef.componentInstance.editForm.value.jobTitle,
      phoneNumber1: dialogRef.componentInstance.editForm.value.phoneNumber1,
      phoneNumber2: dialogRef.componentInstance.editForm.value.phoneNumber2,
    };
    this.authService._setUser(this.user);
    this.loadProfilePicture();
  }

  // Notification-related methods
  private initializeNotifications(): void {
    this.notificationService.loadNotifications();
    this.notificationService.notifications$.subscribe((notifications) => {
      this.notificationsSubject.next(notifications);
    });

    this.notificationService.unreadCount$.subscribe((count) => {});
  }

  // Dropdown-related methods
  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
    this.notificationService.markAllAsRead().subscribe(() => {});
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    const notificationContainer = document.querySelector(
      '.notification-container'
    );
    if (notificationContainer && !notificationContainer.contains(target)) {
      this.isDropdownOpen = false;
    }
  }
}
