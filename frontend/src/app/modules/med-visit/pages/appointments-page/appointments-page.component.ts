import { Component, inject } from '@angular/core';
import { Appointment } from '../../models/appointment';
import { AppointmentStatus } from '../../models/appointment-status';
import { AppointmentService } from '../../services/appointment.service';
import { ActivatedRoute, Router } from '@angular/router';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { AuthService } from '../../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';

@Component({
  selector: 'app-appointments-page',
  standalone: false,
  templateUrl: './appointments-page.component.html',
  styleUrl: './appointments-page.component.css',
})
export class AppointmentsPageComponent {
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
  statusFilter = '';

  // Data properties
  appointments: Appointment[] = [];
  displayedAppointments: Appointment[] = [];

  // Inject services
  authService = inject(AuthService);
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
      this.loadAppointments();
    });
  }

  onStatusFilterChange() {
    this.pageIndex = 0;
    this.applyFilters();
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

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.pageIndex) {
      this.pageIndex = page;
      this.updateVisiblePages();
      this.updateDisplayedAppointments(this.appointments);
    }
  }

  previousPage(): void {
    if (this.pageIndex > 0) {
      this.pageIndex--;
      this.updateVisiblePages();
      this.updateDisplayedAppointments(this.appointments);
    }
  }

  nextPage(): void {
    if (this.pageIndex < this.totalPages - 1) {
      this.pageIndex++;
      this.updateVisiblePages();
      this.updateDisplayedAppointments(this.appointments);
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

  export(type: 'pdf' | 'csv') {
    if (type === 'pdf') {
      const doc = new jsPDF();
      doc.setFontSize(16);
      doc.text('Medical Visits Report', 10, 10);
      doc.setFontSize(12);

      const tableData = this.displayedAppointments.map((appointment) => [
        appointment.id,
        appointment.doctorName,
        new Date(appointment.timeSlot).toLocaleDateString(),
        appointment.userFullName,
        AppointmentStatus[appointment.status],
      ]);

      autoTable(doc, {
        head: [['ID', 'Doctor Name', 'Visit Date', 'Patient Name', 'Status']],
        body: tableData,
      });

      doc.save('medical_visits_report.pdf');
    } else if (type === 'csv') {
      const csvContent =
        'data:text/csv;charset=utf-8,' +
        this.displayedAppointments
          .map(
            (appointment) =>
              `${appointment.id},${appointment.doctorName},${appointment.userFullName},${appointment.timeSlot},${appointment.status}`
          )
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

  private loadAppointments() {
    const id = this.route.snapshot.params['id'];
    this.appointmentService.getAppointmentsByMedicalVisitId(id).subscribe({
      next: (appointments: Appointment[]) => {
        this.appointments = appointments;
        this.totalItems = appointments.length;
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error loading appointments:', error);
        this.launchSnackbar('Failed to load appointments', 'error');
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
    let filteredAppointments = [...this.appointments];

    if (this.searchTerm) {
      filteredAppointments = filteredAppointments.filter((visit) =>
        visit.userFullName.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    }

    if (this.statusFilter) {
      filteredAppointments = filteredAppointments.filter(
        (visit) => visit.status === this.statusFilter
      );
    }

    filteredAppointments = filteredAppointments.sort(
      (a, b) => new Date(a.timeSlot).getTime() - new Date(b.timeSlot).getTime()
    );

    this.totalItems = filteredAppointments.length;
    this.calculatePagination();

    if (this.pageIndex >= this.totalPages && this.totalPages > 0) {
      this.pageIndex = this.totalPages - 1;
    } else if (this.totalPages === 0) {
      this.pageIndex = 0;
    }

    this.updateDisplayedAppointments(filteredAppointments);
  }

  private updateDisplayedAppointments(sourceList: Appointment[]): void {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedAppointments = sourceList.slice(startIndex, endIndex);
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
