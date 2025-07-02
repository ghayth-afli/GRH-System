import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomSnackbarComponent } from '../custom-snackbar/custom-snackbar.component';

@Component({
  selector: 'app-edit-personal-info-modal-form',
  standalone: false,
  templateUrl: './edit-personal-info-modal-form.component.html',
  styleUrls: ['./edit-personal-info-modal-form.component.css'],
})
export class EditPersonalInfoModalFormComponent implements OnInit {
  editForm: FormGroup;
  selectedFile: File | null = null;
  user = {
    profilePicture: '/assets/images/nopicture.png',
  };
  userHasNullAttributes = false;
  countryCode = '+216';
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<EditPersonalInfoModalFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.editForm = this.fb.group({
      firstName: [
        '',
        [Validators.required, Validators.pattern(/^[a-zA-ZÀ-ÿ\s-']+$/)],
      ],
      lastName: [
        '',
        [Validators.required, Validators.pattern(/^[a-zA-ZÀ-ÿ\s-']+$/)],
      ],
      email: [
        { value: '', disabled: true },
        [Validators.required, Validators.email],
      ],
      jobTitle: ['', Validators.required],
      phoneNumber1: [
        '',
        [Validators.required, Validators.pattern(/^[0-9]{8}$/)],
      ],
      phoneNumber2: ['', Validators.pattern(/^[0-9]{8}$/)],
    });
  }

  ngOnInit(): void {
    this.handleIfUserHasNullAttributes();
    this.user.profilePicture = this.data.imageSrc || this.user.profilePicture;
    const phoneNumber1 =
      this.data.user.phoneNumber1?.replace(this.countryCode, '') || '';
    const phoneNumber2 =
      this.data.user.phoneNumber2?.replace(this.countryCode, '') || '';
    this.editForm.patchValue({
      firstName: this.data.user.firstName || '',
      lastName: this.data.user.lastName || '',
      email: this.data.user.email || '',
      jobTitle: this.data.user.jobTitle || '',
      phoneNumber1: phoneNumber1,
      phoneNumber2: phoneNumber2,
    });
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.user.profilePicture = e.target.result;
      };
      reader.readAsDataURL(file);
      this.selectedFile = file;
    }
  }

  triggerFileInput(): void {
    const fileInput = document.getElementById(
      'profilePicture'
    ) as HTMLInputElement;
    fileInput?.click();
  }

  onSubmit(): void {
    console.log('Form submitted:', this.editForm.value);
    if (this.editForm.valid) {
      this.isLoading = true;
      const {
        firstName,
        lastName,
        email,
        jobTitle,
        phoneNumber1,
        phoneNumber2,
      } = this.editForm.value;
      const formData = {
        firstName,
        lastName,
        email: this.data.user.email || '',
        jobTitle,
        phoneNumber1,
        phoneNumber2,
        profilePicture: this.user.profilePicture,
      };
      this.userService
        .updateEmployeeInfo(
          formData.firstName,
          formData.lastName,
          formData.email,
          formData.jobTitle,
          formData.phoneNumber1,
          formData.phoneNumber2,
          undefined,
          this.selectedFile ?? undefined
        )
        .subscribe({
          next: () => {
            this.isLoading = false;
            this.snackBar.openFromComponent(CustomSnackbarComponent, {
              data: {
                message: 'Profile updated successfully',
                type: 'success',
              },
              duration: 5000,
              panelClass: ['custom-snackbar'],
              horizontalPosition: 'end',
              verticalPosition: 'top',
            });
            this.dialogRef.close(formData);
            console.log(
              'Profile updated:',
              JSON.stringify({ action: 'update_profile', ...formData }, null, 2)
            );
          },
          error: (error) => {
            console.error('Update failed:', error);
            this.isLoading = false;
            this.snackBar.openFromComponent(CustomSnackbarComponent, {
              data: { message: 'Failed to update profile', type: 'error' },
              duration: 5000,
              panelClass: ['custom-snackbar'],
              horizontalPosition: 'end',
              verticalPosition: 'top',
            });
          },
        });
    } else {
      this.editForm.markAllAsTouched();
    }
  }

  private handleIfUserHasNullAttributes(): void {
    this.userHasNullAttributes = Object.entries(
      this.authService.authenticatedUser || {}
    ).some(
      ([key, value]) =>
        key !== 'phoneNumber2' && (value === null || value === '')
    );
  }

  closeModal(): void {
    this.dialogRef.close();
  }
}
