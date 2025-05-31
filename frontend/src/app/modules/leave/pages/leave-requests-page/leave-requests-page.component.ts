import { Component, inject, ViewChild } from '@angular/core';
import { LeaveService } from '../../services/leave.service';
import { AuthService } from '../../../../core/services/auth.service';
import { MatTableDataSource } from '@angular/material/table';
import { Leave } from '../../models/leave';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { delay, map, Observable, of } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { LeaveType } from '../../models/leave-type';
import { HttpClient } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';
import { ConfirmationModalComponent } from '../../../../shared/components/confirmation-modal/confirmation-modal.component';

@Component({
  selector: 'app-leave-requests-page',
  standalone: false,

  templateUrl: './leave-requests-page.component.html',
  styleUrl: './leave-requests-page.component.css',
})
export class LeaveRequestsPageComponent {
  // Pagination
  pageSize = 5;
  pageIndex = 0;
  pageSizeOptions = [5, 10, 25, 100];
  totalItems = 0;
  totalPages = 0;
  pages: number[] = [];
  visiblePages: (number | string)[] = [];

  // Filter properties
  searchTerm: string = '';
  statusFilter = 'all';
  fromDateFilter?: Date;
  toDateFilter?: Date;

  // Data properties
  requests: Leave[] = [];
  displayedRequests: Leave[] = [];
  loading = false;

  // Inject services
  authService = inject(AuthService);
  private leaveService = inject(LeaveService);
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
      this.loadRequests();
    });
  }

  private loadRequests() {
    this.leaveService.getAllLeaveRequests().subscribe({
      next: (requests: Leave[]) => {
        this.requests = requests;
        this.totalItems = requests.length;
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error loading leave requests:', error);
        this.launchSnackbar('Failed to load leave requests', 'error');
      },
    });
  }

  calculateDuration(startDate: string, endDate: string): string {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = end.getTime() - start.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    return `${diffDays} day${diffDays !== 1 ? 's' : ''}`;
  }

  mapLeaveType(type: string): string {
    const map: { [key: string]: string } = {
      ANNUEL: 'Annual Leave',
      MALADIE: 'Sick Leave',
    };
    return map[type] || type;
  }

  mapStatus(status: string): string {
    const map: { [key: string]: string } = {
      EN_ATTENTE: 'Pending',
      APPROUVÉE: 'Approved',
      REFUSÉE: 'Rejected',
    };

    return map[status] || status;
  }

  mapStatusClass(status: string): string {
    return status.toLowerCase().replace('é', 'e');
  }

  viewAttachments(request: Leave) {
    this.leaveService.getReceivedAttachment(request.id).subscribe({
      next: (data: Blob) => {
        const url = window.URL.createObjectURL(data);
        const a = document.createElement('a');
        document.body.appendChild(a);
        a.href = url;
        a.download = `attachment-${request.id}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading attachment:', error);
        this.launchSnackbar('Failed to download attachment', 'error');
      },
    });
  }

  openApproveConfirmation(id: number) {
    const dialogRef = this.dialog.open(ConfirmationModalComponent, {
      data: {
        title: 'Approve Leave Request',
        message: 'Are you sure you want to approve this leave request?',
        confirmButtonText: 'Approve',
        cancelButtonText: 'Cancel',
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.confirmed) {
        this.approveRequest(id);
      }
    });
  }

  openRejectConfirmation(id: number) {
    const dialogRef = this.dialog.open(ConfirmationModalComponent, {
      data: {
        title: 'Reject Leave Request',
        message: 'Are you sure you want to reject this leave request?',
        confirmButtonText: 'Reject',
        cancelButtonText: 'Cancel',
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.confirmed) {
        this.rejectRequest(id);
      }
    });
  }

  private approveRequest(id: number) {
    this.leaveService.approveLeave(id).subscribe({
      next: () => {
        this.launchSnackbar('Leave request approved successfully', 'success');
        this.requests = this.requests.map((request) =>
          request.id === id ? { ...request, status: 'APPROUVÉE' } : request
        );
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error approving leave request:', error);
        this.launchSnackbar('Failed to approve leave request', 'error');
      },
    });
  }

  private rejectRequest(id: number) {
    this.leaveService.rejectLeave(id).subscribe({
      next: () => {
        this.launchSnackbar('Leave request rejected successfully', 'success');
        this.requests = this.requests.map((request) =>
          request.id === id ? { ...request, status: 'REFUSÉE' } : request
        );
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error rejecting leave request:', error);
        this.launchSnackbar('Failed to reject leave request', 'error');
      },
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

  onStartDateFilterChange() {
    this.pageIndex = 0;
    this.applyFilters();
  }

  onEndDateFilterChange() {
    this.pageIndex = 0;
    this.applyFilters();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.pageIndex) {
      this.pageIndex = page;
      this.updateVisiblePages();
      this.updateDisplayedRequests(this.requests);
    }
  }

  previousPage(): void {
    if (this.pageIndex > 0) {
      this.pageIndex--;
      this.updateVisiblePages();
      this.updateDisplayedRequests(this.requests);
    }
  }

  nextPage(): void {
    if (this.pageIndex < this.totalPages - 1) {
      this.pageIndex++;
      this.updateVisiblePages();
      this.updateDisplayedRequests(this.requests);
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
    let filteredRequests = [...this.requests];

    if (this.searchTerm) {
      filteredRequests = filteredRequests.filter((request) =>
        request.Name.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    }

    if (this.statusFilter && this.statusFilter !== 'all') {
      filteredRequests = filteredRequests.filter(
        (request) => request.status === this.statusFilter.toUpperCase()
      );
    }

    this.totalItems = filteredRequests.length;
    this.calculatePagination();

    if (this.pageIndex >= this.totalPages && this.totalPages > 0) {
      this.pageIndex = this.totalPages - 1;
    } else if (this.totalPages === 0) {
      this.pageIndex = 0;
    }

    this.updateDisplayedRequests(filteredRequests);
  }

  private updateDisplayedRequests(sourceList: Leave[]): void {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedRequests = sourceList.slice(startIndex, endIndex);
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
