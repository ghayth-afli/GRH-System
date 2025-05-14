import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: false,

  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent {
  authService = inject(AuthService);
  private router = inject(Router);
  isExpanded = true;

  toggleSidebar() {
    this.isExpanded = !this.isExpanded;
  }
  logout() {
    this.authService.logout();

    this.router.navigate(['/auth/login']);
  }
}
