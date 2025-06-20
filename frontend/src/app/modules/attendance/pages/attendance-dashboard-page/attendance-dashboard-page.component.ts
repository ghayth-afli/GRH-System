import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { AttendanceRecord } from '../../models/attendance-record';
import { Exception } from '../../models/exception';
import { combineLatest, of } from 'rxjs';
import { startWith, debounceTime, map } from 'rxjs/operators';
import { AttendanceService } from '../../service/attendance.service';

// NOTE: Dialog component imports are unused in this logic but kept for context.
import { AttendanceDetailDialogComponent } from '../../components/attendance-detail-dialog/attendance-detail-dialog.component';
import { ExceptionDetailDialogComponent } from '../../components/exception-detail-dialog/exception-detail-dialog.component';

@Component({
  selector: 'app-attendance-dashboard-page',
  standalone: false,
  templateUrl: './attendance-dashboard-page.component.html',
  styleUrls: ['./attendance-dashboard-page.component.css'],
})
export class AttendanceDashboardPageComponent implements OnInit {
  filterForm: FormGroup;

  // Master lists to hold the original data
  private masterAttendanceRecords: AttendanceRecord[] = [];
  private masterExceptions: Exception[] = [];

  // Arrays for displaying filtered data
  filteredAttendanceRecords: AttendanceRecord[] = [];
  filteredExceptions: Exception[] = [];

  kpis = {
    present: 0,
    late: 0,
    absent: 0,
    totalHours: '0h',
  };
  viewMode = 'daily';
  filterOpen = false;
  today = new Date().toISOString().split('T')[0];
  departments = ['HR', 'IT', 'Finance'];
  statuses = [
    'Present',
    'Awaiting',
    'Late',
    'Absent',
    'Half-Day',
    'On-Leave',
    'Weekend',
  ];
  isDataLoading = false; // Single, unified loading state

  // Pagination for attendance records
  attendancePageSize = 5;
  attendancePageSizes = [5, 10, 20];
  attendanceCurrentPage = 1;
  attendanceTotalPages = 1;
  paginatedAttendanceRecords: AttendanceRecord[] = [];

  // Pagination for exceptions
  exceptionsPageSize = 5;
  exceptionsPageSizes = [5, 10, 20];
  exceptionsCurrentPage = 1;
  exceptionsTotalPages = 1;
  paginatedExceptions: Exception[] = [];

  constructor(
    private fb: FormBuilder,
    private attendanceService: AttendanceService
  ) {
    this.filterForm = this.fb.group({
      date: [this.today],
      dateRangeStart: [''],
      dateRangeEnd: [''],
      month: [''],
      department: [''],
      status: [''],
      search: [''],
    });
  }

  ngOnInit() {
    this.isDataLoading = true;
    // Fetch initial data once. Use `of([])` for mock/non-existent services.
    combineLatest([
      this.attendanceService.getAttendanceRecords(),
      of([] as Exception[]), // FIX: Correctly handle mock exceptions as an observable
    ])
      .pipe(
        map(
          ([attendanceRecords, exceptions]: [
            AttendanceRecord[],
            Exception[]
          ]) => {
            // Extract issues from attendance records and convert to exceptions
            const issuesAsExceptions =
              this.convertIssuesToExceptions(attendanceRecords);

            // Combine original exceptions with issues converted to exceptions
            const combinedExceptions = [...exceptions, ...issuesAsExceptions];

            return [attendanceRecords, combinedExceptions] as [
              AttendanceRecord[],
              Exception[]
            ];
          }
        )
      )
      .subscribe({
        next: ([attendanceRecords, combinedExceptions]: [
          AttendanceRecord[],
          Exception[]
        ]) => {
          this.masterAttendanceRecords = attendanceRecords;
          this.masterExceptions = combinedExceptions;

          // Start listening to filter changes only after data is loaded
          this.filterForm.valueChanges
            .pipe(startWith(this.filterForm.value), debounceTime(300))
            .subscribe((filters) => {
              this.applyFilters(filters);
            });

          this.isDataLoading = false;
        },
        error: (err) => {
          console.error('Error fetching initial data', err);
          this.isDataLoading = false;
        },
      });
  }

  /**
   * Converts issues from attendance records to Exception objects
   */
  private convertIssuesToExceptions(
    attendanceRecords: AttendanceRecord[]
  ): Exception[] {
    const exceptions: Exception[] = [];

    attendanceRecords.forEach((record) => {
      if (record.issues) {
        // Handle both Set<string> and string[] types
        const issuesArray = Array.isArray(record.issues)
          ? record.issues
          : Array.from(record.issues);

        // Check if there are any issues
        if (issuesArray.length > 0) {
          issuesArray.forEach((issue) => {
            // Only add non-empty issues
            if (issue && issue.trim()) {
              exceptions.push({
                employee: record.employeeName,
                date: record.date,
                issue: issue.trim(),
              });
            }
          });
        }
      }
    });

    return exceptions;
  }

  applyFilters(filters: any): void {
    // Filter attendance records from the master list
    this.filteredAttendanceRecords = this.masterAttendanceRecords.filter(
      (record) => {
        const searchStr = filters.search?.toLowerCase() || '';
        const matchesSearch =
          !searchStr || record.employeeName.toLowerCase().includes(searchStr);
        const matchesDept =
          !filters.department || record.department === filters.department;
        const matchesStatus =
          !filters.status || record.status === filters.status.toUpperCase();

        let matchesDate = true;
        if (this.viewMode === 'daily' && filters.date) {
          matchesDate = record.date === filters.date;
        } else if (this.viewMode === 'monthly' && filters.month) {
          matchesDate = record.date.startsWith(filters.month);
        } else if (filters.dateRangeStart && filters.dateRangeEnd) {
          matchesDate =
            record.date >= filters.dateRangeStart &&
            record.date <= filters.dateRangeEnd;
        }

        return matchesSearch && matchesDept && matchesStatus && matchesDate;
      }
    );

    // Filter exceptions from the master list
    this.filteredExceptions = this.masterExceptions.filter((exception) => {
      const searchStr = filters.search?.toLowerCase() || '';

      // Add search functionality for exceptions
      const matchesSearch =
        !searchStr ||
        exception.employee.toLowerCase().includes(searchStr) ||
        exception.issue.toLowerCase().includes(searchStr);

      let matchesDate = true;
      if (this.viewMode === 'daily' && filters.date) {
        matchesDate = exception.date === filters.date;
      } else if (this.viewMode === 'monthly' && filters.month) {
        matchesDate = exception.date.startsWith(filters.month);
      } else if (filters.dateRangeStart && filters.dateRangeEnd) {
        matchesDate =
          exception.date >= filters.dateRangeStart &&
          exception.date <= filters.dateRangeEnd;
      }

      return matchesSearch && matchesDate;
    });

    this.updateKpis();
    this.updateAttendancePagination();
    this.updateExceptionsPagination();
  }

  updateKpis(): void {
    this.kpis.present = this.filteredAttendanceRecords.filter(
      (r) => r.status === 'PRESENT'
    ).length;
    this.kpis.late = this.filteredAttendanceRecords.filter(
      (r) => r.status === 'LATE'
    ).length;
    this.kpis.absent = this.filteredAttendanceRecords.filter(
      (r) => r.status === 'ABSENT'
    ).length;
    this.kpis.totalHours =
      this.filteredAttendanceRecords
        .reduce(
          (total, record) =>
            total + (record.totalHours ? parseFloat(record.totalHours) : 0),
          0
        )
        .toFixed(2) + 'h';
  }

  updateAttendancePagination(): void {
    this.attendanceTotalPages = Math.ceil(
      this.filteredAttendanceRecords.length / this.attendancePageSize
    );
    if (this.attendanceTotalPages === 0) this.attendanceTotalPages = 1;

    this.attendanceCurrentPage = Math.min(
      this.attendanceCurrentPage,
      this.attendanceTotalPages
    );

    const start = (this.attendanceCurrentPage - 1) * this.attendancePageSize;
    this.paginatedAttendanceRecords = this.filteredAttendanceRecords.slice(
      start,
      start + this.attendancePageSize
    );
  }

  updateExceptionsPagination(): void {
    this.exceptionsTotalPages = Math.ceil(
      this.filteredExceptions.length / this.exceptionsPageSize
    );
    if (this.exceptionsTotalPages === 0) this.exceptionsTotalPages = 1;

    this.exceptionsCurrentPage = Math.min(
      this.exceptionsCurrentPage,
      this.exceptionsTotalPages
    );

    const start = (this.exceptionsCurrentPage - 1) * this.exceptionsPageSize;
    this.paginatedExceptions = this.filteredExceptions.slice(
      start,
      start + this.exceptionsPageSize
    );
  }

  changeAttendancePage(page: number): void {
    if (page >= 1 && page <= this.attendanceTotalPages) {
      this.attendanceCurrentPage = page;
      this.updateAttendancePagination();
    }
  }

  changeExceptionsPage(page: number): void {
    if (page >= 1 && page <= this.exceptionsTotalPages) {
      this.exceptionsCurrentPage = page;
      this.updateExceptionsPagination();
    }
  }

  changeAttendancePageSize(size: string | number): void {
    this.attendancePageSize = Number(size);
    this.attendanceCurrentPage = 1;
    this.updateAttendancePagination();
  }

  changeExceptionsPageSize(size: string | number): void {
    this.exceptionsPageSize = Number(size);
    this.exceptionsCurrentPage = 1;
    this.updateExceptionsPagination();
  }

  toggleView(mode: string): void {
    this.viewMode = mode;
    const initialFilters = {
      date: this.today,
      month: '',
      dateRangeStart: '',
      dateRangeEnd: '',
    };
    this.filterForm.reset(initialFilters);
  }

  toggleFilter(): void {
    this.filterOpen = !this.filterOpen;
  }

  generateReport(): void {
    console.log('Generating report...');
  }

  onFileDrop(event: DragEvent): void {
    event.preventDefault();
    const file = event.dataTransfer?.files[0];
    if (file && file.type === 'text/csv') {
      console.log('CSV upload feature not implemented in mock service');
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  openAttendanceDetail(record: AttendanceRecord): void {
    console.log('Opening attendance detail for:', record);
  }

  openExceptionDetail(exception: Exception): void {
    console.log('Opening exception detail for:', exception);
  }
}
