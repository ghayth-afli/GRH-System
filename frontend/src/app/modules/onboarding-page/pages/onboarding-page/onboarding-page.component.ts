import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../../../../core/services/user.service';
import { AuthService } from '../../../../core/services/auth.service';
import { User } from '../../../../core/models/auth-responses.interface';

@Component({
  selector: 'app-onboarding-page',
  standalone: false,
  templateUrl: './onboarding-page.component.html',
  styleUrl: './onboarding-page.component.css',
})
export class OnboardingPageComponent {
  onboardingForm: FormGroup;
  isLoading = false;
  isSuccess = false;
  selectedFile: File | null = null;
  photoPreview: string | null = 'assets/images/nopicture.png';
  readonly countryCode = '+216';
  user: User = {
    id: '',
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    department: '',
    role: '',
    jobTitle: '',
    phoneNumber1: '',
    phoneNumber2: '',
    gender: '',
    birthDate: '',
  };

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private userService: UserService,
    private authService: AuthService
  ) {
    this.onboardingForm = this.fb.group({
      gender: ['', Validators.required],
      birthDate: ['', Validators.required],
      jobTitle: ['', Validators.required],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      profilePhoto: [null],
    });
  }

  ngOnInit(): void {
    this.initializeUser();
  }

  private initializeUser(): void {
    this.user = this.authService.authenticatedUser || this.user;
  }

  handlePhotoUpload(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = () => {
          this.photoPreview = reader.result as string;
          this.onboardingForm.patchValue({ profilePhoto: file });
        };
        reader.readAsDataURL(file);
      } else {
        alert('Please upload a valid image file.');
      }
    }
  }

  submitForm() {
    if (
      this.onboardingForm.valid ||
      this.onboardingForm.get('phoneNumber')?.valid
    ) {
      const formData = {
        firstName: this.user.firstName || '',
        lastName: this.user.lastName || '',
        email: this.user.email || '',
        jobTitle: this.onboardingForm.get('jobTitle')?.value || '',
        phoneNumber1: this.onboardingForm.get('phoneNumber')?.value || '',
        phoneNumber2: this.user.phoneNumber2 || '',
        profilePicture: this.selectedFile,
        gender: this.onboardingForm.get('gender'),
        birthDate: this.onboardingForm.get('birthDate')?.value || '',
      };
      this.userService
        .updateEmployeeInfo(
          formData.firstName,
          formData.lastName,
          formData.email,
          formData.jobTitle,
          formData.phoneNumber1,
          formData.phoneNumber2,
          formData.gender?.value || '',
          formData.birthDate,
          this.selectedFile ?? undefined
        )
        .subscribe({
          next: () => {
            this.isLoading = true;
            setTimeout(() => {
              this.isLoading = false;
              this.isSuccess = true;
              setTimeout(() => {
                this.router.navigate(['/home']);
              }, 2000);
            }, 2000);
            this.user = {
              ...this.user,
              firstName: formData.firstName,
              lastName: formData.lastName,
              email: formData.email,
              jobTitle: formData.jobTitle,
              phoneNumber1: formData.phoneNumber1,
              phoneNumber2: formData.phoneNumber2,
              gender: formData.gender?.value,
              birthDate: formData.birthDate,
            };
            localStorage.setItem('user', JSON.stringify(this.user));
          },
          error: (error) => {},
        });
    } else {
      this.onboardingForm.markAllAsTouched();
    }
  }
}
