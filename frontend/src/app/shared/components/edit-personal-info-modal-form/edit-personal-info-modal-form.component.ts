import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';

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
  userHasNullAttributes = true;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    public dialogRef: MatDialogRef<EditPersonalInfoModalFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.editForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      jobTitle: ['', Validators.required],
      phoneNumber1: [
        '',
        [Validators.required, Validators.pattern(/^\+216[0-9]{8}$/)],
      ],
      phoneNumber2: ['', Validators.pattern(/^\+216[0-9]{8}$/)],
    });
  }

  ngOnInit(): void {
    this.handleIfUserHasNullAttributes();
    this.user.profilePicture = this.data.imageSrc || this.user.profilePicture;
    this.editForm.patchValue({
      firstName: this.data.firstName || '',
      lastName: this.data.lastName || '',
      email: this.data.email || '',
      jobTitle: this.data.jobTitle || '',
      phoneNumber1: this.data.phoneNumber1 || '',
      phoneNumber2: this.data.phoneNumber2 || '',
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
    if (this.editForm.valid) {
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
        email,
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
          this.selectedFile ?? undefined
        )
        .subscribe({
          next: () => {
            this.dialogRef.close(formData);
            console.log(
              'Profile updated:',
              JSON.stringify({ action: 'update_profile', ...formData }, null, 2)
            );
          },
          error: (error) => console.error('Update failed:', error),
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
