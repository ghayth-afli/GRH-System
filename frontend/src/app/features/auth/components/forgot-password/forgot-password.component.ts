import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { ForgotPasswordResponse } from '../../models/auth-responses.interface';

@Component({
  selector: 'app-forgot-password',
  standalone: false,
  templateUrl: './forgot-password.component.html',
  styleUrls: [
    './forgot-password.component.css',
    '../../shared/auth-shared.css',
  ],
})
export class ForgotPasswordComponent {
  forgotForm: FormGroup;
  sending = false;
  responseMessage: string = '';

  constructor(private fb: FormBuilder, private authService: AuthService) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  onSubmit() {
    this.sending = true;
    const email = this.forgotForm.value.email;
    this.authService.forgotPassword(email).subscribe({
      next: (response: ForgotPasswordResponse) => {
        this.sending = false;
        this.responseMessage = response.message;
        this.forgotForm.disable();
      },
      error: (error) => {
        this.sending = false;
        console.error(error);
        this.responseMessage = 'An error occurred. Please try again.';
      },
    });
  }
}
