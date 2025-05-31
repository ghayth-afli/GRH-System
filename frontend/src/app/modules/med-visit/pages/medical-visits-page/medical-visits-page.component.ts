import { Component, inject } from '@angular/core';
import { MedicalVisit } from '../../models/medical-visit';
import { MedicalVisitService } from '../../services/medical-visit.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';

import { AuthService } from '../../../../core/services/auth.service';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CreateEditMedicalVisitComponent } from '../../components/create-edit-medical-visit/create-edit-medical-visit.component';
import { TakeRegistrationDialogComponent } from '../../components/take-registration-dialog/take-registration-dialog.component';
import { ConfirmationModalComponent } from '../../../../shared/components/confirmation-modal/confirmation-modal.component';
import { AppointmentService } from '../../services/appointment.service';

@Component({
  selector: 'app-medical-visits-page',
  standalone: false,
  templateUrl: './medical-visits-page.component.html',
  styleUrl: './medical-visits-page.component.css',
})
export class MedicalVisitsPageComponent {
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
  visitDateFilter = '';

  // Data properties
  visits: MedicalVisit[] = [];
  displayedVisits: MedicalVisit[] = [];

  // Inject services
  authService = inject(AuthService);
  private medicalVisitService = inject(MedicalVisitService);
  private appointmentService = inject(AppointmentService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  ngOnInit() {
    this.route.queryParamMap.subscribe((params) => {
      const pageSize = params.get('pageSize');
      if (pageSize) {
        this.pageSize = parseInt(pageSize, 10);
      }
      this.loadVisits();
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

  onVisitDateChange() {
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
      this.updateDisplayedTrainings(this.visits); // Re-evaluate if this should use filtered list or full list then filter
    }
  }

  previousPage(): void {
    if (this.pageIndex > 0) {
      this.pageIndex--;
      this.updateVisiblePages();
      this.updateDisplayedTrainings(this.visits);
    }
  }

  nextPage(): void {
    if (this.pageIndex < this.totalPages - 1) {
      this.pageIndex++;
      this.updateVisiblePages();
      this.updateDisplayedTrainings(this.visits);
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

  openCreateDialog() {
    const dialogRef = this.dialog.open(CreateEditMedicalVisitComponent, {
      width: '500px',
      data: { isEdit: false },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadVisits();

        this.snackBar.openFromComponent(CustomSnackbarComponent, {
          data: {
            message: 'Medical visit created successfully!',
            type: 'success',
          },
          duration: 5000,
          panelClass: ['custom-snackbar'],
          horizontalPosition: 'end',
          verticalPosition: 'top',
        });
      }
    });
  }

  openEditDialog(visit: MedicalVisit) {
    const dialogRef = this.dialog.open(CreateEditMedicalVisitComponent, {
      width: '500px',
      data: { isEdit: true, visit },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadVisits();

        this.snackBar.openFromComponent(CustomSnackbarComponent, {
          data: {
            message: 'Medical visit updated successfully!',
            type: 'success',
          },
          duration: 5000,
          panelClass: ['custom-snackbar'],
          horizontalPosition: 'end',
          verticalPosition: 'top',
        });
      }
    });
  }

  deleteVisit(id: number) {
    this.medicalVisitService.deleteMedicalVisit(id).subscribe({
      next: (response) => {
        this.visits = this.visits.filter((visit) => visit.id !== id);
        this.applyFilters();
        this.snackBar.openFromComponent(CustomSnackbarComponent, {
          data: {
            message: 'Medical visit deleted successfully!',
            type: 'success',
          },
          duration: 5000,
          panelClass: ['custom-snackbar'],
          horizontalPosition: 'end',
          verticalPosition: 'top',
        });
      },
      error: (error) => {
        console.error('Error deleting medical visit:', error);
        this.launchSnackbar('Failed to delete medical visit', 'error');
      },
    });
  }

  openTakeAppointmentDialog(visit: MedicalVisit) {
    const dialogRef = this.dialog.open(TakeRegistrationDialogComponent, {
      width: '400px',
      data: { visit },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadVisits();
        this.snackBar.openFromComponent(CustomSnackbarComponent, {
          data: {
            message: 'Appointment booked successfully!',
            type: 'success',
          },
          duration: 5000,
          panelClass: ['custom-snackbar'],
          horizontalPosition: 'end',
          verticalPosition: 'top',
        });
      }
    });
  }

  onCancelAppointment(id: number) {
    const dialogRef = this.dialog.open(ConfirmationModalComponent, {
      data: {
        title: 'Cancel Appointment',
        message: 'Are you sure you want to cancel this appointment?',
        confirmButtonText: 'Cancel Appointment',
        cancelButtonText: 'No, Keep Appointment',
      },
    });

    dialogRef.afterClosed().subscribe({
      next: (confirmed) => {
        if (confirmed) {
          this.appointmentService.cancelAppointment(id).subscribe({
            next: () => {
              this.loadVisits();
              this.launchSnackbar(
                'Appointment cancelled successfully',
                'success'
              );
            },
            error: (error) => {
              console.error('Error cancelling appointment:', error);
              this.launchSnackbar('Failed to cancel appointment', 'error');
            },
          });
        }
      },
      error: (error) => {
        console.error('Error in confirmation dialog:', error);
        this.launchSnackbar('Failed to confirm cancellation', 'error');
      },
    });
  }

  openDeleteConfirmation(id: number) {
    const dialogRef = this.dialog.open(ConfirmationModalComponent, {
      data: {
        title: 'Delete Medical Visit',
        message: 'Are you sure you want to delete this medical visit?',
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
      },
    });

    dialogRef.afterClosed().subscribe({
      next: (confirmed) => {
        if (confirmed) {
          this.deleteVisit(id);
        }
      },
      error: (error) => {
        console.error('Error in confirmation dialog:', error);
        this.launchSnackbar('Failed to confirm deletion', 'error');
      },
    });
  }

  export(type: 'pdf' | 'csv') {
    if (type === 'pdf') {
      const doc = new jsPDF();
      doc.setFontSize(16);
      doc.text('Medical Visits Report', 14, 16);

      const tableData = this.displayedVisits.map((visit) => [
        visit.id.toString(),
        visit.doctorName,
        visit.visitDate,
      ]);

      autoTable(doc, {
        head: [['ID', 'Doctor Name', 'Visit Date', 'Patient Name', 'Status']],
        body: tableData,
      });

      doc.save('medical_visits_report.pdf');
    } else if (type === 'csv') {
      const csvContent =
        'data:text/csv;charset=utf-8,' +
        this.displayedVisits
          .map((visit) => `${visit.id},${visit.doctorName},${visit.visitDate}`)
          .join('\n');

      const encodedUri = encodeURI(csvContent);
      const link = document.createElement('a');
      link.setAttribute('href', encodedUri);
      link.setAttribute('download', 'medical_visits_report.csv');
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } else {
      this.launchSnackbar('Invalid export type', 'error');
    }
  }

  private loadVisits() {
    this.medicalVisitService.getMedicalVisits().subscribe({
      next: (visits) => {
        this.visits = visits;
        this.totalItems = visits.length;
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error loading medical visits:', error);
        this.launchSnackbar('Failed to load medical visits', 'error');
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

  private applyFilters() {
    let filteredVisits = [...this.visits];

    if (this.searchTerm) {
      filteredVisits = filteredVisits.filter((visit) =>
        visit.doctorName.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    }

    filteredVisits = filteredVisits.filter((visit) => {
      const matchesDate = this.visitDateFilter
        ? new Date(visit.visitDate).toISOString().split('T')[0] ===
          this.visitDateFilter
        : true;
      return matchesDate;
    });

    filteredVisits = filteredVisits.sort(
      (a, b) =>
        new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime()
    );

    this.totalItems = filteredVisits.length;
    this.calculatePagination();

    if (this.pageIndex >= this.totalPages && this.totalPages > 0) {
      this.pageIndex = this.totalPages - 1;
    } else if (this.totalPages === 0) {
      this.pageIndex = 0;
    }

    this.updateDisplayedTrainings(filteredVisits);
  }

  private updateDisplayedTrainings(sourceList: MedicalVisit[]): void {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedVisits = sourceList.slice(startIndex, endIndex);
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
}
