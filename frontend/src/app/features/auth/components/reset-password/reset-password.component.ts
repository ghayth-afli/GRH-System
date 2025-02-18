import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { ResetPasswordResponse } from '../../models/auth-responses.interface';

@Component({
  selector: 'app-reset-password',
  standalone: false,
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css',
})
export class ResetPasswordComponent {
  resetForm: FormGroup;
  sending = false;
  responseMessage: string = '';
  passwordMismatchError = false;
  token: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.token = this.route.snapshot.queryParamMap.get('token');
    if (!this.token) {
      this.router.navigate(['/', 'auth', 'forgot-password']);
    }

    this.resetForm = this.fb.group(
      {
        password: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', Validators.required],
      },
      { validators: this.passwordMatchValidator }
    );
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value
      ? null
      : { mismatch: true };
  }

  onSubmit() {
    this.sending = true;
    const password = this.resetForm.value.password;
    this.authService.resetPassword(this.token!, password).subscribe({
      next: (response: ResetPasswordResponse) => {
        this.sending = false;
        this.responseMessage = response.message;
        this.resetForm.disable();

        setTimeout(() => {
          this.router.navigate(['/', 'auth', 'login']);
        }, 3000);
      },
      error: (error) => {
        this.sending = false;
        console.error(error);
        this.responseMessage = 'An error occurred. Please try again.';
      },
    });
  }
}
