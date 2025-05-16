import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  ConfirmationModalComponent,
  ConfirmationModalData,
} from '../../../../shared/components/confirmation-modal/confirmation-modal.component';
import { MatDialog } from '@angular/material/dialog';
import { JobOfferResponse } from '../../models/job-offer-response';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { JobOfferService } from '../../services/job-offer.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-job-offer-details',
  standalone: false,
  templateUrl: './job-offer-details.component.html',
  styleUrl: './job-offer-details.component.css',
})
export class JobOfferDetailsComponent {
  jobOffer: JobOfferResponse | null = null;
  hasApplied = false;

  private jobOffers: JobOfferResponse[] = [];
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private jobOfferService = inject(JobOfferService);
  authService = inject(AuthService);
  constructor(private dialog: MatDialog, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.loadJobOffer();
  }

  loadJobOffer() {
    this.route.params.subscribe((params) => {
      const jobId = +params['id'];
      this.jobOfferService.getJobOfferById(jobId).subscribe({
        next: (jobOffer) => {
          this.jobOffer = jobOffer;
          this.hasApplied = jobOffer.applied;
        },
        error: () => {
          this.router.navigate(['/not-found']);
        },
      });
    });
  }

  openDeleteConfirmation(id: number) {
    const dialogRef = this.dialog.open(ConfirmationModalComponent, {
      data: {
        title: 'Delete Job Offer',
        message: 'Are you sure you want to delete this job offer?',
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.confirmed) {
        this.deleteJobOffer(id);
      }
    });
  }

  private deleteJobOffer(id: number) {
    this.jobOfferService.deleteJobOffer(id).subscribe({
      next: () => {
        this.jobOffers = this.jobOffers.filter(
          (jobOffer) => jobOffer.id !== id
        );
        this.router.navigateByUrl('/recruitment/job-offers');
        this.launchSnackbar('Job offer deleted successfully', 'success');
      },
      error: (error) => {
        console.error('Error deleting job offer:', error);
        this.launchSnackbar('Error deleting job offer', 'error');
      },
    });
  }
  private launchSnackbar(message: string, type: 'success' | 'error') {
    this.snackBar.openFromComponent(CustomSnackbarComponent, {
      data: {
        message: message,
        type: type,
      },
      duration: 5000,
      panelClass: ['custom-snackbar'],
      horizontalPosition: 'end',
      verticalPosition: 'top',
    });
  }
}
