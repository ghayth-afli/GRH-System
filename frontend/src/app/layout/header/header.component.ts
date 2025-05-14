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
export class HeaderComponent {
  isDropdownOpen = false;
  showBadge = true;
  imageSrc: string | null = null;
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
  notifications = [
    {
      message: 'New application from MOHAMED GHAYTH AFLI',
      time: 'May 13, 2025, 2:30 PM',
      unread: true,
    },
    {
      message: 'Interview scheduled for Jane Smith',
      time: 'May 12, 2025, 10:15 AM',
      unread: true,
    },
    {
      message: 'David Johnson’s application reviewed',
      time: 'May 11, 2025, 4:00 PM',
      unread: false,
    },
  ];
  ngOnInit(): void {
    this.initializeUser();
    this.loadProfilePicture();
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

  toggleNotifications() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  clearNotifications() {
    this.notifications = [];
    this.showBadge = false;
  }
}
