import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { ResetPasswordResponse } from '../../models/auth-responses.interface';

@Component({
  selector: 'app-reset-password',
  standalone: false,
  templateUrl: './reset-password.component.html',
  styleUrls: [
    './reset-password.component.css',
    '../../shared/auth/auth-shared.css',
  ],
})
export class ResetPasswordComponent {
  resetForm: FormGroup;
  responseMessage: string = '';
  sending = false;
  token: string | null = null;

  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);

  constructor() {
    this.token = this.route.snapshot.queryParamMap.get('token');
    if (!this.token) {
      this.router.navigate(['/auth/login']);
    }

    this.resetForm = this.fb.group(
      {
        password: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', Validators.required],
      },
      {
        validator: this.passwordMatchValidator,
      }
    );
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value
      ? null
      : { mismatch: true };
  }

  onSubmit(): void {
    if (this.resetForm.invalid) return;

    this.sending = true;
    const password = this.resetForm.value.password;

    this.authService.resetPassword(this.token!, password).subscribe({
      next: (response) => {
        this.sending = false;
        this.responseMessage =
          response.message || 'Password updated successfully';
        this.router.navigate(['/auth/login']);
      },
      error: (error) => {
        this.sending = false;
        this.responseMessage =
          error.error?.message || 'Failed to update password';
      },
    });
  }
}
