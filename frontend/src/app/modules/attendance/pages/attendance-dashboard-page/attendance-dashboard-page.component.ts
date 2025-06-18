import { Component } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { AttendanceRecord } from '../../models/attendance-record';
import { Exception } from '../../models/exception';
import { BehaviorSubject, combineLatest } from 'rxjs';
import { debounceTime, map } from 'rxjs/operators';
import { AttendanceService } from '../../service/attendance.service';

@Component({
  selector: 'app-attendance-dashboard-page',
  standalone: false,
  templateUrl: './attendance-dashboard-page.component.html',
  styleUrl: './attendance-dashboard-page.component.css',
})
export class AttendanceDashboardPageComponent {
  filterForm: FormGroup;
  attendanceRecords: AttendanceRecord[] = [];
  exceptions: Exception[] = [];
  kpis = {
    present: 0,
    late: 0,
    absent: 0,
    totalHours: '0h',
  };
  viewMode = 'daily';
  filterOpen = false;
  today = '2025-06-17';
  departments = ['HR', 'IT', 'Finance'];
  statuses = ['On-Time', 'Late', 'Absent'];

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

  private filterSubject = new BehaviorSubject<any>({ date: this.today });

  constructor(
    private fb: FormBuilder,
    private attendanceService: AttendanceService
  ) {
    this.filterForm = this.fb.group({
      date: [this.today],
      month: [''],
      department: [''],
      status: [''],
      search: [''],
    });
  }

  ngOnInit() {
    this.loadAttendance(this.today);
    this.loadExceptions(this.today);
  }

  loadAttendance(date: string) {
    this.attendanceService.getAttendanceRecords(date).subscribe((records) => {
      this.attendanceRecords = records;
      this.updateAttendancePagination();
      this.kpis.present = records.filter(
        (r) => r.status === 'PRESENT' || r.status === 'LATE'
      ).length;
      this.kpis.late = records.filter((r) => r.status === 'LATE').length;
      this.kpis.absent = records.filter((r) => r.status === 'ABSENT').length;
      this.kpis.totalHours =
        records
          .reduce(
            (total, record) =>
              total + (record.totalHours ? parseFloat(record.totalHours) : 0),
            0
          )
          .toFixed(2) + 'h';
    });
  }

  applyFilters() {
    if (this.viewMode === 'daily') {
      const date = this.filterForm.value.date;
      this.loadAttendance(date);
    }
    if (this.viewMode === 'monthly') {
      const month = this.filterForm.value.month;
      this.attendanceService
        .getAttendanceRecordsByMonth(month)
        .subscribe((records) => {
          this.attendanceRecords = records;
          this.kpis.present = records.filter(
            (r) => r.status === 'PRESENT' || r.status === 'LATE'
          ).length;
          this.kpis.late = records.filter((r) => r.status === 'LATE').length;
          this.kpis.absent = records.filter(
            (r) => r.status === 'ABSENT'
          ).length;
          this.kpis.totalHours =
            records
              .reduce(
                (total, record) =>
                  total +
                  (record.totalHours ? parseFloat(record.totalHours) : 0),
                0
              )
              .toFixed(2) + 'h';
          this.updateAttendancePagination();
        });
    }
    //apply other filters like department and status
    const department = this.filterForm.value.department;
    const status = this.filterForm.value.status;
    const search = this.filterForm.value.search.toLowerCase();
    this.attendanceRecords = this.attendanceRecords.filter((record) => {
      return (
        (!department || record.employeeDepartment === department) &&
        (!status || record.status === status) &&
        (!search ||
          record.employeeName.toLowerCase().includes(search) ||
          record.employeeId.toLowerCase().includes(search))
      );
    });
    this.updateAttendancePagination();
    // this.exceptions = this.exceptions.filter((exception) => {
    //   return (
    //     (!department || exception.department === department) &&
    //     (!status || exception.status === status) &&
    //     (!search ||
    //       exception.employeeName.toLowerCase().includes(search) ||
    //       exception.employeeId.toLowerCase().includes(search))
    //   );
    // });
    this.updateExceptionsPagination();
  }

  loadExceptions(date: string) {}

  updateAttendancePagination() {
    this.attendanceTotalPages = Math.ceil(
      this.attendanceRecords.length / this.attendancePageSize
    );
    this.attendanceCurrentPage = Math.min(
      this.attendanceCurrentPage,
      this.attendanceTotalPages || 1
    );
    const start = (this.attendanceCurrentPage - 1) * this.attendancePageSize;
    this.paginatedAttendanceRecords = this.attendanceRecords.slice(
      start,
      start + this.attendancePageSize
    );
  }

  updateExceptionsPagination() {
    this.exceptionsTotalPages = Math.ceil(
      this.exceptions.length / this.exceptionsPageSize
    );
    this.exceptionsCurrentPage = Math.min(
      this.exceptionsCurrentPage,
      this.exceptionsTotalPages || 1
    );
    const start = (this.exceptionsCurrentPage - 1) * this.exceptionsPageSize;
    this.paginatedExceptions = this.exceptions.slice(
      start,
      start + this.exceptionsPageSize
    );
  }

  changeAttendancePage(page: number) {
    if (page >= 1 && page <= this.attendanceTotalPages) {
      this.attendanceCurrentPage = page;
      this.updateAttendancePagination();
    }
  }

  changeExceptionsPage(page: number) {
    if (page >= 1 && page <= this.exceptionsTotalPages) {
      this.exceptionsCurrentPage = page;
      this.updateExceptionsPagination();
    }
  }

  changeAttendancePageSize(size: number) {
    this.attendancePageSize = size;
    this.attendanceCurrentPage = 1;
    this.updateAttendancePagination();
  }

  changeExceptionsPageSize(size: number) {
    this.exceptionsPageSize = size;
    this.exceptionsCurrentPage = 1;
    this.updateExceptionsPagination();
  }

  toggleView(mode: string) {
    this.viewMode = mode;
    this.filterForm.reset({ date: this.today });
  }

  toggleFilter() {
    this.filterOpen = !this.filterOpen;
  }

  generateReport() {}

  onFileDrop(event: DragEvent) {
    event.preventDefault();
    const file = event.dataTransfer?.files[0];
    if (file && file.type === 'text/csv') {
      alert('CSV upload feature not implemented in mock service');
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
  }

  openAttendanceDetail(record: AttendanceRecord) {
    // Dialog logic (unchanged)
  }

  openExceptionDetail(exception: Exception) {
    // Dialog logic (unchanged)
  }
}
