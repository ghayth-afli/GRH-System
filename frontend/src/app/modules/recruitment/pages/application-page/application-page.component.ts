import { Component, inject } from '@angular/core';
import { ApplicationResponseDTO } from '../../models/application-response';
import { AuthService } from '../../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { JobApplicationService } from '../../services/job-application.service';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';
import {
  ConfirmationModalComponent,
  ConfirmationModalData,
} from '../../../../shared/components/confirmation-modal/confirmation-modal.component';

@Component({
  selector: 'app-application-page',
  standalone: false,
  templateUrl: './application-page.component.html',
  styleUrl: './application-page.component.css',
})
export class ApplicationPageComponent {
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
  selectedScore: string = 'all';
  selectedStatus: string = 'all';

  // Data
  applications: ApplicationResponseDTO[] = [];
  displayedApplications: ApplicationResponseDTO[] = [];

  // Services
  authService = inject(AuthService);
  private jobApplicationService = inject(JobApplicationService);
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

      this.loadApplications();
    });
  }

  onStatusChange() {
    this.pageIndex = 0; // Reset to first page
    this.applyFilters();
  }

  onScoreChange() {
    this.pageIndex = 0; // Reset to first page
    this.applyFilters();
  }

  shortlistApplication(id: number) {
    this.jobApplicationService
      .updateApplicationStatus(id, 'SHORTLISTED')
      .subscribe({
        next: () => {
          this.launchSnackbar(
            'Application shortlisted successfully.',
            'success'
          );
          //update the application status in the list
          const index = this.applications.findIndex(
            (application) => application.id === id
          );
          if (index !== -1) {
            this.applications[index].status = 'SHORTLISTED';
          }
        },
        error: () => {
          this.launchSnackbar('Failed to shortlist the application.', 'error');
        },
      });
  }

  selectApplication(id: number) {
    const dialogRef = this.dialog.open(ConfirmationModalComponent, {
      data: {
        title: 'Confirmation',
        message: `Are you sure you want to select this application?`,
        confirmButtonText: 'Select',
        cancelButtonText: 'Cancel',
      },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result?.confirmed) {
        this.jobApplicationService
          .updateApplicationStatus(id, 'SELECTED')
          .subscribe({
            next: () => {
              this.launchSnackbar(
                'Application selected successfully.',
                'success'
              );
              //update the application status in the list
              const index = this.applications.findIndex(
                (application) => application.id === id
              );
              if (index !== -1) {
                this.applications[index].status = 'SELECTED';
              }
            },
            error: () => {
              this.launchSnackbar('Failed to select the application.', 'error');
            },
          });
      }
    });
  }

  rejectApplication(id: number) {
    const dialogRef = this.dialog.open(ConfirmationModalComponent, {
      data: {
        title: 'Confirmation',
        message: `Are you sure you want to reject this application?`,
        confirmButtonText: 'Reject',
        cancelButtonText: 'Cancel',
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.confirmed) {
        this.jobApplicationService
          .updateApplicationStatus(id, 'REJECTED')
          .subscribe({
            next: () => {
              this.launchSnackbar(
                'Application rejected successfully.',
                'success'
              );
              //update the application status in the list
              const index = this.applications.findIndex(
                (application) => application.id === id
              );
              if (index !== -1) {
                this.applications[index].status = 'REJECTED';
              }
            },
            error: () => {
              this.launchSnackbar('Failed to reject the application.', 'error');
            },
          });
      }
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

  // Pagination methods
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.pageIndex) {
      this.pageIndex = page;
      this.updateVisiblePages();
      this.updateDisplayedJobOffers(this.applications);
    }
  }

  previousPage(): void {
    if (this.pageIndex > 0) {
      this.pageIndex--;
      this.updateVisiblePages();
      this.updateDisplayedJobOffers(this.applications);
    }
  }

  nextPage(): void {
    if (this.pageIndex < this.totalPages - 1) {
      this.pageIndex++;
      this.updateVisiblePages();
      this.updateDisplayedJobOffers(this.applications);
    }
  }

  changePageSize(size: number): void {
    this.pageSize = size;
    this.pageIndex = 0; // Reset to first page
    this.calculatePagination();
    this.updateDisplayedJobOffers(this.applications);
    this.onPageSizeChange();
  }

  onPageSizeChange() {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        pageSize: this.pageSize,
      },
      queryParamsHandling: 'merge',
    });
  }

  isPrevDisabled(): boolean {
    return this.pageIndex === 0;
  }

  isNextDisabled(): boolean {
    return this.pageIndex === this.totalPages - 1 || this.totalPages === 0;
  }

  onSearchTermChange() {
    this.pageIndex = 0;
    this.applyFilters();
  }

  getScoreClass(score: number): string {
    if (score * 100 >= 80) return 'score-high';
    if (score * 100 >= 60) return 'score-medium';
    return 'score-low';
  }

  private loadApplications() {
    const jobIdParam = this.route.snapshot.paramMap.get('id');
    const jobId = jobIdParam ? Number(jobIdParam) : null;
    if (jobId === null || isNaN(jobId)) {
      this.snackBar.open('Invalid job ID.', 'Close', { duration: 3000 });
      return;
    }
    this.jobApplicationService.getAllApplications(jobId).subscribe({
      next: (applications) => {
        this.applications = applications.sort((a, b) => b.score - a.score);
        this.totalItems = this.applications.length;
        this.applyFilters();
      },
      error: (error) => {
        this.snackBar.open('Failed to load applications.', 'Close', {
          duration: 3000,
        });
      },
    });
  }

  private applyFilters() {
    let filteredApplications = [...this.applications];

    // Apply search filter
    if (this.searchTerm) {
      filteredApplications = filteredApplications.filter((applications) =>
        applications.FullName.toLowerCase().includes(
          this.searchTerm.toLowerCase()
        )
      );
    }

    // Apply status filter
    if (this.selectedStatus !== 'all') {
      filteredApplications = filteredApplications.filter(
        (applications) =>
          applications.status.toLowerCase() ===
          this.selectedStatus.toLowerCase()
      );
    }

    // Apply score filter
    if (this.selectedScore !== 'all') {
      filteredApplications = filteredApplications.filter((applications) => {
        const score = applications.score * 100; // Convert to percentage
        if (this.selectedScore === 'high') {
          return score >= 80;
        } else if (this.selectedScore === 'medium') {
          return score >= 60 && score < 80;
        } else if (this.selectedScore === 'low') {
          return score < 60;
        }
        return true; // Default case
      });
    }

    // Store the filtered results (before pagination)
    this.totalItems = filteredApplications.length;

    // Check if current page is valid after filtering
    this.calculatePagination();
    if (this.pageIndex >= this.totalPages && this.totalPages > 0) {
      this.pageIndex = this.totalPages - 1;
    }

    // Apply pagination
    this.updateDisplayedJobOffers(filteredApplications);
  }

  private calculatePagination(): void {
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
    this.pages = Array.from({ length: this.totalPages }, (_, i) => i);
    this.updateVisiblePages();
  }

  private updateDisplayedJobOffers(
    filteredList: ApplicationResponseDTO[]
  ): void {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedApplications = filteredList.slice(startIndex, endIndex);
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
