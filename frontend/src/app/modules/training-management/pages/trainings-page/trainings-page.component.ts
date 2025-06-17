import { Component, inject } from '@angular/core';
import { TrainingService } from '../../services/training.service';
import { AuthService } from '../../../../core/services/auth.service';
import { MatDialog } from '@angular/material/dialog';
import { Training } from '../../models/training';
import { InvitationService } from '../../services/invitation.service';
import { Subscription } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { ConfirmationModalComponent } from '../../../../shared/components/confirmation-modal/confirmation-modal.component';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';

@Component({
  selector: 'app-trainings-page',
  standalone: false,
  templateUrl: './trainings-page.component.html',
  styleUrl: './trainings-page.component.css',
})
export class TrainingsPageComponent {
  // Pagination
  pageSize = 10;
  pageIndex = 0;
  pageSizeOptions = [5, 10, 25, 100];
  totalItems = 0;
  totalPages = 0;
  pages: number[] = [];
  visiblePages: (number | string)[] = [];

  // Filter properties
  searchTerm: string = '';
  selectedDepartment: string = 'all';
  selectedStatus: string = 'all';
  selectedType: string = 'all';

  // Data properties
  trainings: Training[] = [];
  displayedTrainings: Training[] = [];

  // Inject services
  authService = inject(AuthService);
  private trainingService = inject(TrainingService);
  private invitationService = inject(InvitationService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  // To hold subscriptions
  private queryParamsSubscription: Subscription | undefined;
  private loadTrainingsSubscription: Subscription | undefined;
  private deleteDialogSubscription: Subscription | undefined;
  private deleteTrainingCallSubscription: Subscription | undefined;
  private confirmDialogSubscription: Subscription | undefined;
  private confirmInvitationCallSubscription: Subscription | undefined;

  ngOnInit() {
    this.queryParamsSubscription = this.route.queryParamMap.subscribe(
      (params) => {
        const pageSize = params.get('pageSize');
        if (pageSize) {
          this.pageSize = parseInt(pageSize, 10);
        }
        this.loadTrainings();
      }
    );
  }

  ngOnDestroy() {
    if (this.queryParamsSubscription) {
      this.queryParamsSubscription.unsubscribe();
    }
    if (this.loadTrainingsSubscription) {
      this.loadTrainingsSubscription.unsubscribe();
    }
    if (this.deleteDialogSubscription) {
      this.deleteDialogSubscription.unsubscribe();
    }
    if (this.deleteTrainingCallSubscription) {
      this.deleteTrainingCallSubscription.unsubscribe();
    }
    if (this.confirmDialogSubscription) {
      this.confirmDialogSubscription.unsubscribe();
    }
    if (this.confirmInvitationCallSubscription) {
      this.confirmInvitationCallSubscription.unsubscribe();
    }
  }

  loadTrainings() {
    if (this.loadTrainingsSubscription) {
      this.loadTrainingsSubscription.unsubscribe();
    }
    this.loadTrainingsSubscription = this.trainingService
      .getAllTrainings()
      .subscribe({
        next: (trainings) => {
          this.trainings = trainings;
          this.totalItems = trainings.length;
          this.applyFilters();
        },
        error: (error) => {
          console.error('Error loading trainings:', error);
          this.launchSnackbar(
            'Error loading trainings: ' +
              (error.error?.message || error.message),
            'error'
          );
        },
      });
  }

  openDeleteConfirmation(id: number) {
    if (this.deleteDialogSubscription) {
      this.deleteDialogSubscription.unsubscribe();
    }
    if (this.deleteTrainingCallSubscription) {
      this.deleteTrainingCallSubscription.unsubscribe();
    }
    const dialogRef = this.dialog.open(ConfirmationModalComponent, {
      data: {
        title: 'Delete Training',
        message: 'Are you sure you want to delete this training?',
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
      },
    });

    this.deleteDialogSubscription = dialogRef
      .afterClosed()
      .subscribe((result) => {
        if (result?.confirmed) {
          this.deleteTraining(id);
        }
      });
  }

  onConfirmTraining(trainingId: number) {
    if (this.confirmDialogSubscription) {
      this.confirmDialogSubscription.unsubscribe();
    }
    if (this.confirmInvitationCallSubscription) {
      this.confirmInvitationCallSubscription.unsubscribe();
    }
    this.confirmDialogSubscription = this.dialog
      .open(ConfirmationModalComponent, {
        data: {
          title: 'Confirm Training',
          message: 'Are you sure you want to confirm this training?',
          confirmButtonText: 'Confirm',
          cancelButtonText: 'Cancel',
        },
      })
      .afterClosed()
      .subscribe((result) => {
        if (result?.confirmed) {
          this.confirmInvitationCallSubscription = this.invitationService
            .confirmInvitation(trainingId)
            .subscribe({
              next: () => {
                this.trainings = this.trainings.map((training) =>
                  training.id === trainingId
                    ? { ...training, isConfirmed: true }
                    : training
                );
                this.applyFilters();
                this.launchSnackbar(
                  'Training confirmed successfully',
                  'success'
                );
              },
              error: (error) => {
                console.error('Error confirming training:', error);
                this.launchSnackbar(
                  'Error confirming training: ' +
                    (error.error?.message || error.message),
                  'error'
                );
              },
            });
        }
      });
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

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.pageIndex) {
      this.pageIndex = page;
      this.updateVisiblePages();
      this.updateDisplayedTrainings(this.trainings); // Re-evaluate if this should use filtered list or full list then filter
    }
  }

  previousPage(): void {
    if (this.pageIndex > 0) {
      this.pageIndex--;
      this.updateVisiblePages();
      this.updateDisplayedTrainings(this.trainings); // Re-evaluate
    }
  }

  nextPage(): void {
    if (this.pageIndex < this.totalPages - 1) {
      this.pageIndex++;
      this.updateVisiblePages();
      this.updateDisplayedTrainings(this.trainings); // Re-evaluate
    }
  }

  changePageSize(size: number): void {
    this.pageSize = size;
    this.pageIndex = 0;
    this.onPageSizeChange();
  }

  isPrevDisabled(): boolean {
    return this.pageIndex === 0;
  }

  isNextDisabled(): boolean {
    return this.pageIndex === this.totalPages - 1 || this.totalPages === 0;
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

  private applyFilters() {
    let filteredTrainings = [...this.trainings];

    if (this.searchTerm) {
      filteredTrainings = filteredTrainings.filter((training) =>
        training.title.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    }

    if (this.selectedDepartment !== 'all') {
      filteredTrainings = filteredTrainings.filter(
        (training) =>
          training.department.toLowerCase() ===
          this.selectedDepartment.toLowerCase()
      );
    }

    filteredTrainings = filteredTrainings.sort(
      (a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );

    this.totalItems = filteredTrainings.length;
    this.calculatePagination();

    if (this.pageIndex >= this.totalPages && this.totalPages > 0) {
      this.pageIndex = this.totalPages - 1;
    } else if (this.totalPages === 0) {
      this.pageIndex = 0;
    }

    this.updateDisplayedTrainings(filteredTrainings);
  }

  private updateDisplayedTrainings(sourceList: Training[]): void {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedTrainings = sourceList.slice(startIndex, endIndex);
  }

  private calculatePagination(): void {
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
    if (this.totalPages === 0) {
      this.pages = [];
    } else {
      this.pages = Array.from({ length: this.totalPages }, (_, i) => i);
    }
    this.updateVisiblePages();
  }

  private updateVisiblePages(): void {
    this.visiblePages = [];
    if (this.totalPages <= 0) {
      return;
    }

    if (this.totalPages <= 7) {
      this.visiblePages = this.pages;
    } else {
      this.visiblePages.push(0);
      const currentPageIndex = this.pageIndex;

      let startPage = Math.max(1, currentPageIndex - 1);
      let endPage = Math.min(this.totalPages - 2, currentPageIndex + 1);

      if (currentPageIndex < 3) {
        endPage = Math.min(this.totalPages - 2, 3);
      }
      if (currentPageIndex > this.totalPages - 4) {
        startPage = Math.max(1, this.totalPages - 4);
      }

      if (startPage > 1) {
        this.visiblePages.push('...');
      }
      for (let i = startPage; i <= endPage; i++) {
        this.visiblePages.push(i);
      }
      if (endPage < this.totalPages - 2) {
        this.visiblePages.push('...');
      }
      this.visiblePages.push(this.totalPages - 1);
    }
  }

  private deleteTraining(id: number) {
    if (this.deleteTrainingCallSubscription) {
      this.deleteTrainingCallSubscription.unsubscribe();
    }
    this.deleteTrainingCallSubscription = this.trainingService
      .deleteTraining(id)
      .subscribe({
        next: () => {
          this.trainings = this.trainings.filter(
            (training) => training.id !== id
          );
          this.applyFilters();
          this.launchSnackbar('Training deleted successfully', 'success');
        },
        error: (error) => {
          console.error('Error deleting training:', error);
          this.launchSnackbar(
            'Error deleting training: ' +
              (error.error?.message || error.message),
            'error'
          );
        },
      });
  }
}
