import { Component } from '@angular/core';
import { FormBuilder, FormGroup, NgForm, Validators } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { ForgotPasswordResponse } from '../../../../core/models/auth-responses.interface';

@Component({
  selector: 'app-forgot-password',
  standalone: false,
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css'],
})
export class ForgotPasswordComponent {
  email: string = '';
  sending = false;
  responseMessage: string = '';
  isDisabled = false;

  constructor(private authService: AuthService) {}

  onSubmit(form: NgForm) {
    if (form.invalid) return;

    this.sending = true;

    this.authService.forgotPassword(this.email).subscribe({
      next: (response: ForgotPasswordResponse) => {
        this.sending = false;
        this.responseMessage = response.message;
        this.isDisabled = true;
      },
      error: (error) => {
        this.sending = false;
        console.error(error);
        this.responseMessage = 'An error occurred. Please try again.';
      },
    });
  }
}
