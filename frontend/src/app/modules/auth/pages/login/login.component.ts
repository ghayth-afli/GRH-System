import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';

@Component({
  selector: 'app-login',
  standalone: false,

  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent {
  username: string = '';
  password: string = '';
  showPassword: boolean = false;
  isLoading: boolean = false;
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  constructor(private router: Router) {}

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    if (this.isLoading) return;

    this.isLoading = true;
    const credentials = {
      username: this.username,
      password: this.password,
    };
    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.isLoading = false;
        if (this.authService.hasRole('Employee')) {
          this.router.navigate(['/home']);
        } else if (
          this.authService.hasRole('Manager') ||
          this.authService.hasRole('HR')
        ) {
          this.router.navigate(['/home']);
        } else if (this.authService.hasRole('HRD')) {
          this.router.navigate(['/recruitment/job-offers']);
        }
      },
      error: (error: Error) => {
        this.isLoading = false;
        this.snackBar.openFromComponent(CustomSnackbarComponent, {
          data: {
            message:
              error.message === ''
                ? 'Login failed. Please try again.'
                : error.message,
            type: 'error',
          },
          duration: 5000,
          panelClass: ['custom-snackbar'],
          horizontalPosition: 'end',
          verticalPosition: 'top',
        });
      },
    });
  }
}
