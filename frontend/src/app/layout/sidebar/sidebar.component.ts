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
  isExpanded = true;
  isDocumentsExpanded = false; // Track Documents sub-menu state

  constructor(public authService: AuthService) {}

  toggleSidebar() {
    this.isExpanded = !this.isExpanded;
  }

  toggleDocumentsMenu() {
    this.isDocumentsExpanded = !this.isDocumentsExpanded;
  }

  logout() {
    this.authService.logout();
  }
}
