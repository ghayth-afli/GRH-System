import { Component, HostListener, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/auth-responses.interface';
import { map, Observable } from 'rxjs';
import { LeaveService } from '../../modules/leave/services/leave.service';
import { ActivatedRoute } from '@angular/router';
import { LeaveBalance } from '../../modules/leave/models/leave-balance';
import { UserService } from '../../core/services/user.service';
import { ProfilePicture } from '../../core/models/ProfilePicture';
import { MatDialog } from '@angular/material/dialog';
import { EditPersonalInfoModalFormComponent } from '../../shared/components/edit-personal-info-modal-form/edit-personal-info-modal-form.component';

@Component({
  selector: 'app-header',
  standalone: false,
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})
export class HeaderComponent {
  leaveBalance$: Observable<LeaveBalance>;
  imageSrc: string | null = null;
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
    jobTitle: '',
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

  constructor(
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private userService: UserService
  ) {
    this.leaveBalance$ = this.route.data.pipe(
      map((data) => data['leaveBalance'])
    );
  }

  ngOnInit(): void {
    this.user = this.authService.authenticatedUser || this.user;
    this.userService.getProfilePicture(this.user.username).subscribe({
      next: (response) => {
        console.log(response);
        this.imageSrc = this.getBase64Image(response);
      },
      error: (error) => {
        console.error(error);
      },
    });
  }

  getBase64Image(profilePicture: ProfilePicture): string {
    if (profilePicture.base64Image) {
      return profilePicture.base64Image;
    } else {
      const binary = new Uint8Array(profilePicture.picture);
      const base64String = btoa(String.fromCharCode(...binary));
      return `data:image/${profilePicture.type};base64,${base64String}`;
    }
  }

  isEmployee() {
    return this.authService.hasRole('Employee');
  }

  openEditModal() {
    console.log(this.user);
    const dialogRef = this.dialog.open(EditPersonalInfoModalFormComponent, {
      width: '500px',
      data: {
        firstName: this.user.firstName,
        lastName: this.user.lastName,
        email: this.user.email,
        jobTitle: this.user.jobTitle,
        imageSrc: this.imageSrc,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.user = {
          ...this.user,
          firstName: dialogRef.componentInstance.editForm.value.firstName,
          lastName: dialogRef.componentInstance.editForm.value.lastName,
          email: dialogRef.componentInstance.editForm.value.email,
          jobTitle: dialogRef.componentInstance.editForm.value.jobTitle,
        };
        this.authService._setUser(this.user);
        this.userService.getProfilePicture(this.user.username).subscribe({
          next: (response) => {
            console.log(response);
            this.imageSrc = this.getBase64Image(response);
          },
          error: (error) => {
            console.error(error);
          },
        });
      }
    });
  }
}
