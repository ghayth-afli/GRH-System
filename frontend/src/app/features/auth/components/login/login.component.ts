import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: false,

  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css', '../../shared/auth/auth-shared.css'],
})
export class LoginComponent {
  loginForm: FormGroup;
  responseMessage: string = '';
  sending = false;

  private authService = inject(AuthService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  constructor() {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.sending = true;
    const { username, password } = this.loginForm.value;

    this.authService.login(username, password).subscribe({
      next: () => {
        this.sending = false;
        this.router.navigate(['/dashboard']);
      },
      error: (error: Error) => {
        this.sending = false;
        console.error(error);
        this.responseMessage = "Couldn't log in. Please try again.";
      },
    });
  }
}
