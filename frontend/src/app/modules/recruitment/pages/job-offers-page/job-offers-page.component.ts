import { Component, inject, OnInit } from '@angular/core';
import { ConfirmationModalComponent } from '../../../../shared/components/confirmation-modal/confirmation-modal.component';
import { MatDialog } from '@angular/material/dialog';
import { JobOfferService } from '../../services/job-offer.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';
import { AuthService } from '../../../../core/services/auth.service';
import { JobOfferResponse } from '../../models/job-offer-response';
import { EjobOfferStatus } from '../../models/EjobOfferStatus';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-job-offers-page',
  standalone: false,
  templateUrl: './job-offers-page.component.html',
  styleUrl: './job-offers-page.component.css',
})
export class JobOffersPageComponent implements OnInit {
  // Pagination settings
  pageSize = 10;
  pageIndex = 0;
  pageSizeOptions = [5, 10, 25, 100];
  totalItems = 0;
  totalPages = 0;
  pages: number[] = [];
  visiblePages: (number | string)[] = [];

  // Filter settings
  searchTerm: string = '';
  selectedDepartment: string = 'all';
  selectedStatus: string = 'all';
  selectedType: string = 'all';

  // Data
  jobOffers: JobOfferResponse[] = [];
  displayedJobOffers: JobOfferResponse[] = [];

  // Constants
  jobOfferStatus = EjobOfferStatus;

  // Services
  authService = inject(AuthService);
  private jobOfferService = inject(JobOfferService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  ngOnInit() {
    this.route.queryParamMap.subscribe((params) => {
      const pageSize = params.get('pageSize');

      if (pageSize) {
        this.pageSize = parseInt(pageSize);
      }

      this.loadJobOffers();
    });
  }

  // Actions
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

  onToggleJobOfferStatus(id: number, status: string) {
    if (
      status === 'CONVERTED_TO_EXTERNAL' ||
      status === 'CONVERTED_TO_INTERNAL'
    ) {
      const dialogRef = this.dialog.open(ConfirmationModalComponent, {
        data: {
          title: 'Change Job Offer Status',
          message: `Are you sure you want to change the job offer status?`,
          confirmButtonText: 'Toggle',
          cancelButtonText: 'Cancel',
        },
      });

      dialogRef.afterClosed().subscribe((result) => {
        if (result?.confirmed) {
          this.toggleJobOfferStatus(id, status);
        }
      });
      return;
    }
    if (status === 'CLOSED') {
      const dialogRef = this.dialog.open(ConfirmationModalComponent, {
        data: {
          title: 'Finish Job Offer',
          message: `Are you sure you want to finish the job offer?`,
          confirmButtonText: 'Finish',
          cancelButtonText: 'Cancel',
        },
      });

      dialogRef.afterClosed().subscribe((result) => {
        if (result?.confirmed) {
          this.toggleJobOfferStatus(id, status);
        }
      });
      return;
    }
    if (status === 'OPEN') {
      const dialogRef = this.dialog.open(ConfirmationModalComponent, {
        data: {
          title: 'Finish Job Offer',
          message: `Are you sure you want to finish the job offer?`,
          confirmButtonText: 'Open',
          cancelButtonText: 'Cancel',
        },
      });

      dialogRef.afterClosed().subscribe((result) => {
        if (result?.confirmed) {
          this.toggleJobOfferStatus(id, status);
        }
      });
      return;
    }
  }

  // Filter handlers
  onPageSizeChange() {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        pageSize: this.pageSize,
      },
      queryParamsHandling: 'merge',
    });
  }

  onSearchTermChange() {
    this.pageIndex = 0;
    this.applyFilters();
  }

  onDepartmentChange() {
    this.pageIndex = 0;
    this.applyFilters();
  }

  onStatusChange() {
    this.pageIndex = 0;
    this.applyFilters();
  }

  onTypeChange() {
    this.pageIndex = 0;
    this.applyFilters();
  }

  // Pagination methods
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.pageIndex) {
      this.pageIndex = page;
      this.updateVisiblePages();
      this.updateDisplayedJobOffers(this.jobOffers);
    }
  }

  previousPage(): void {
    if (this.pageIndex > 0) {
      this.pageIndex--;
      this.updateVisiblePages();
      this.updateDisplayedJobOffers(this.jobOffers);
    }
  }

  nextPage(): void {
    if (this.pageIndex < this.totalPages - 1) {
      this.pageIndex++;
      this.updateVisiblePages();
      this.updateDisplayedJobOffers(this.jobOffers);
    }
  }

  changePageSize(size: number): void {
    this.pageSize = size;
    this.pageIndex = 0; // Reset to first page
    this.calculatePagination();
    this.updateDisplayedJobOffers(this.jobOffers);
    this.onPageSizeChange();
  }

  isPrevDisabled(): boolean {
    return this.pageIndex === 0;
  }

  isNextDisabled(): boolean {
    return this.pageIndex === this.totalPages - 1 || this.totalPages === 0;
  }

  // Private methods
  private toggleJobOfferStatus(id: number, status: string) {
    this.jobOfferService.toggleJobOfferStatus(id, status).subscribe({
      next: () => {
        this.launchSnackbar('Job offer status changed ', 'success');
        this.jobOffers = this.jobOffers.map((jobOffer) => {
          if (jobOffer.id === id) {
            if (status !== 'CONVERTED_TO_EXTERNAL') {
              return { ...jobOffer, status: status };
            } else {
              return { ...jobOffer, isInternal: false };
            }
          }
          return jobOffer;
        });
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error updating job offer status:', error);
        this.launchSnackbar('Error updating job offer status', 'error');
      },
    });
  }

  private deleteJobOffer(id: number) {
    this.jobOfferService.deleteJobOffer(id).subscribe({
      next: () => {
        this.jobOffers = this.jobOffers.filter(
          (jobOffer) => jobOffer.id !== id
        );
        this.launchSnackbar('Job offer deleted successfully', 'success');
        this.applyFilters();
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

  private loadJobOffers() {
    this.jobOfferService.getAllJobOffers().subscribe({
      next: (jobOffers) => {
        this.jobOffers = jobOffers;
        this.totalItems = jobOffers.length;
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error loading job offers:', error);
        this.launchSnackbar('Error loading job offers', 'error');
      },
    });
  }

  private applyFilters() {
    let filteredOffers = [...this.jobOffers];

    // Apply search filter
    if (this.searchTerm) {
      filteredOffers = filteredOffers.filter((jobOffer) =>
        jobOffer.title.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    }

    // Apply department filter
    if (this.selectedDepartment !== 'all') {
      filteredOffers = filteredOffers.filter(
        (jobOffer) =>
          jobOffer.department.toLowerCase() ===
          this.selectedDepartment.toLowerCase()
      );
    }

    // Apply status filter
    if (this.selectedStatus !== 'all') {
      filteredOffers = filteredOffers.filter(
        (jobOffer) =>
          jobOffer.status.toLowerCase() === this.selectedStatus.toLowerCase()
      );
    }

    // Apply type filter
    if (this.selectedType !== 'all') {
      filteredOffers = filteredOffers.filter(
        (jobOffer) => jobOffer.isInternal === (this.selectedType === 'internal')
      );
    }

    // Sort by creation date (newest first)
    filteredOffers = filteredOffers.sort(
      (a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );

    // Store the filtered results (before pagination)
    this.totalItems = filteredOffers.length;

    // Check if current page is valid after filtering
    this.calculatePagination();
    if (this.pageIndex >= this.totalPages && this.totalPages > 0) {
      this.pageIndex = this.totalPages - 1;
    }

    // Apply pagination
    this.updateDisplayedJobOffers(filteredOffers);
  }

  private updateDisplayedJobOffers(filteredList: JobOfferResponse[]): void {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedJobOffers = filteredList.slice(startIndex, endIndex);
  }

  private calculatePagination(): void {
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
    this.pages = Array.from({ length: this.totalPages }, (_, i) => i);
    this.updateVisiblePages();
  }

  private updateVisiblePages(): void {
    this.visiblePages = [];

    if (this.totalPages <= 7) {
      // Show all pages if total is 7 or fewer
      this.visiblePages = this.pages;
    } else {
      // Always add page 1
      this.visiblePages.push(0);
      const startPage = Math.max(1, this.pageIndex - 1);
      const endPage = Math.min(this.totalPages - 2, this.pageIndex + 1);

      // Add ellipsis if needed between page 1 and startPage
      if (startPage > 1) {
        this.visiblePages.push('...');
      }

      // Add pages around current page
      for (let i = startPage; i <= endPage; i++) {
        this.visiblePages.push(i);
      }

      // Add ellipsis if needed between endPage and last page
      if (endPage < this.totalPages - 2) {
        this.visiblePages.push('...');
      }

      // Always add last page if there is more than one page
      if (this.totalPages > 1) {
        this.visiblePages.push(this.totalPages - 1);
      }
    }
  }
}
