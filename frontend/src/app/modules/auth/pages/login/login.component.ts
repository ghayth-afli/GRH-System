import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: false,

  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent {
  username: string = '';
  password: string = '';
  responseMessage: string = '';
  sending = false;

  private authService = inject(AuthService);
  private router = inject(Router);

  onSubmit(): void {
    if (!this.username || !this.password) return;

    this.sending = true;

    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.sending = false;
        if (this.authService.hasRole('Employee')) {
          this.router.navigate(['/home']);
        } else if (
          this.authService.hasRole('Manager') ||
          this.authService.hasRole('HR')
        ) {
          this.router.navigate(['/leave']);
        }
      },
      error: (error: Error) => {
        this.sending = false;
        console.error(error);
        this.responseMessage = "Couldn't log in. Please try again.";
      },
    });
  }
}
