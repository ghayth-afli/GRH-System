import { Component, inject, Inject, OnInit } from '@angular/core';
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
export class EditPersonalInfoModalFormComponent {
  editForm!: FormGroup;
  selectedFile: File | undefined = undefined;
  user = {
    profilePicture: '/assets/images/nopicture.png',
  };
  private authService = inject(AuthService);
  userhasNullAttributes = true;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,

    public dialogRef: MatDialogRef<EditPersonalInfoModalFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  ngOnInit(): void {
    this.handleIfUserHasNullAttributes();
    this.user = {
      profilePicture: this.data.imageSrc,
    };
    this.editForm = this.fb.group({
      firstName: [this.data.firstName, Validators.required],
      lastName: [this.data.lastName, Validators.required],
      email: [this.data.email, [Validators.required, Validators.email]],
      jobTitle: [this.data.jobTitle, Validators.required],
      phoneNumber1: [
        this.data.phoneNumber1,
        [Validators.required, Validators.pattern(/^\+216[0-9]{8}$/)],
      ],
      phoneNumber2: [
        this.data.phoneNumber2,
        Validators.pattern(/^\+216[0-9]{8}$/), // Optional field
      ],
    });
  }

  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (file) {
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
    fileInput.click();
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

      this.userService
        .updateEmployeeInfo(
          firstName,
          lastName,
          email,
          jobTitle,
          phoneNumber1,
          phoneNumber2,
          this.selectedFile
        )
        .subscribe({
          next: () => this.dialogRef.close(true),
        });
    } else {
      this.editForm.markAllAsTouched();
    }
  }

  private handleIfUserHasNullAttributes(): void {
    this.userhasNullAttributes = Object.entries(
      this.authService.authenticatedUser || {}
    ).some(
      ([key, value]) =>
        key !== 'phoneNumber2' && (value === null || value === '')
    );
  }

  closeModal(): void {
    this.dialogRef.close(true);
  }
}
