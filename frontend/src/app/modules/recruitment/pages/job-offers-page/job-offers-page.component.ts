import { Component, inject } from '@angular/core';
import { ConfirmationModalComponent } from '../../../../shared/components/confirmation-modal/confirmation-modal.component';
import { MatDialog } from '@angular/material/dialog';
import { JobOffer } from '../../models/job-offer';
import { JobOfferService } from '../../services/job-offer.service';

@Component({
  selector: 'app-job-offers-page',
  standalone: false,
  templateUrl: './job-offers-page.component.html',
  styleUrl: './job-offers-page.component.css',
})
export class JobOffersPageComponent {
  pageSize = 10;
  pageSizeOptions = [5, 10, 25, 100];
  jobOffers: JobOffer[] = [];
  jobOfferService = inject(JobOfferService);
  constructor(private dialog: MatDialog) {}

  ngOnInit() {
    this.loadJobOffers();
  }

  loadJobOffers(page: number = 0, size: number = this.pageSize) {
    this.jobOfferService.getAllJobOffers().subscribe({
      next: (response) => {
        this.jobOffers = response.content;
        console.log('Job offers loaded:', this.jobOffers);
      },
      error: (error) => {
        console.error('Error loading job offers:', error);
      },
    });
  }

  deleteJobOffer(id: number) {
    // Placeholder: Now handled by openDeleteConfirmation
  }

  toggleInternalExternal(id: number) {
    console.log(`Toggling internal/external for job offer ID: ${id}`);
  }

  finishStopJobOffer(id: number) {
    console.log(`Finishing/Stopping job offer ID: ${id}`);
  }

  onPageSizeChange() {
    console.log(`Page size changed to: ${this.pageSize}`);
  }
}
