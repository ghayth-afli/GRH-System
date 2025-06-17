import { Component, inject, Inject, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { JobOfferService } from '../../services/job-offer.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';

@Component({
  selector: 'app-job-offer-form',
  standalone: false,
  templateUrl: './job-offer-form.component.html',
  styleUrl: './job-offer-form.component.css',
})
export class JobOfferFormComponent {
  isEditMode = false;
  submitted = false;
  @ViewChild('jobOfferForm') jobOfferForm!: NgForm;
  jobOfferService = inject(JobOfferService);
  snackBar = inject(MatSnackBar);
  jobOffer = {
    title: '',
    description: '',
    department: '',
    responsibilities: '',
    qualifications: '',
    role: '',
    isInternal: false,
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    jobService: JobOfferService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    this.isEditMode = !!id;
    if (this.isEditMode) {
      // Simulate fetching job offer data for edit mode
      this.loadJobOffer(Number(id));
    }
  }

  private loadJobOffer(id: number) {
    this.jobOfferService.getJobOfferById(id).subscribe({
      next: (response) => {
        this.jobOffer = response;
        console.log('Job offer loaded:', this.jobOffer);
      },
      error: (error) => {
        console.error('Error loading job offer:', error);
      },
    });
  }

  onSubmit() {
    this.submitted = true;
    if (this.jobOfferForm.valid) {
      if (this.isEditMode) {
        const id = this.route.snapshot.paramMap.get('id');
        const jsonRequest = JSON.stringify(this.jobOffer, null, 2);
        console.log('Updating job offer:', jsonRequest);
        // Simulate backend request
        // In a real app, send to backend via HttpClient
        this.jobOfferService
          .updateJobOffer(Number(id), this.jobOfferForm.value)
          .subscribe({
            next: (response) => {
              console.log('Job offer updated successfully:', response);
              this.snackBar.openFromComponent(CustomSnackbarComponent, {
                data: {
                  message: 'Job offer updated successfully.',
                  type: 'success',
                },
                duration: 5000,
                panelClass: ['custom-snackbar'],
                horizontalPosition: 'end',
                verticalPosition: 'top',
              });
              this.router.navigate(['/recruitment/job-offers']);
            },
            error: (error) => {
              this.snackBar.openFromComponent(CustomSnackbarComponent, {
                data: {
                  message:
                    error.message === ''
                      ? 'Job offer update failed. Please try again.'
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
      } else {
        const jsonRequest = JSON.stringify(this.jobOffer, null, 2);
        console.log('Submitting job offer:', jsonRequest);
        // Simulate backend request
        // In a real app, send to backend via HttpClient
        // this.router.navigate(['/recruitment/job-offers']);
        this.jobOfferService.createJobOffer(this.jobOfferForm.value).subscribe({
          next: (response) => {
            console.log('Job offer created successfully:', response);
            this.snackBar.openFromComponent(CustomSnackbarComponent, {
              data: {
                message: 'Job offer created successfully.',
                type: 'success',
              },
              duration: 5000,
              panelClass: ['custom-snackbar'],
              horizontalPosition: 'end',
              verticalPosition: 'top',
            });
            this.router.navigate(['/recruitment/job-offers']);
          },
          error: (error) => {
            this.snackBar.openFromComponent(CustomSnackbarComponent, {
              data: {
                message:
                  error.message === ''
                    ? 'Job offer creation failed. Please try again.'
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
  }
}
