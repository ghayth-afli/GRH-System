import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/auth-responses.interface';

@Component({
  selector: 'app-header',
  standalone: false,

  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
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

  ngOnInit(): void {
    this.user = this.authService.authenticatedUser || this.user;
  }
}
