import {
  Component,
  inject,
  ViewChild,
  OnDestroy,
  ChangeDetectorRef,
} from '@angular/core';
import { LeaveService } from '../../services/leave.service';
import { AuthService } from '../../../../core/services/auth.service';
import { MatTableDataSource } from '@angular/material/table';
import { Leave } from '../../models/leave';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { NotificationData } from '../../../../core/models/NotificationData';

interface ApiResponse {
  message: string;
}

@Component({
  selector: 'app-leave-requests-data-table',
  standalone: false,
  templateUrl: './leave-requests-data-table.component.html',
  styleUrls: ['./leave-requests-data-table.component.css'],
})
export class LeaveRequestsDataTableComponent implements OnDestroy {
  dataSource = new MatTableDataSource<Leave>();
  private leaveService = inject(LeaveService);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);
  authService = inject(AuthService);
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  displayedColumns: string[] = [
    'Name',
    'department',
    'startDate',
    'endDate',
    'leaveType',
    'status',
  ];
  showActionsColumn: boolean = false;
  private newLeaveSubscription: Subscription | null = null;

  ngOnInit(): void {
    this.initializeColumns();
    this.refreshLeaveRequests();
  }

  ngOnDestroy(): void {
    // Unsubscribe from all subscriptions
    if (this.newLeaveSubscription) {
      this.newLeaveSubscription.unsubscribe();
    }
  }
  initializeColumns(): void {
    if (this.authService.hasRole('HR') || this.authService.hasRole('Manager')) {
      this.displayedColumns.push('attachments');
    }
  }

  refreshLeaveRequests(): void {
    this.leaveService.getAllLeaveRequests().subscribe({
      next: (data: Leave[]) => {
        this.dataSource.data = data;
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.checkForActionsColumn(data);
        this.cdr.detectChanges();
      },
      error: (error) => this.showError(error.message),
    });
  }

  checkForActionsColumn(data: Leave[]): void {
    this.showActionsColumn = data.some(
      (leave) =>
        this.authService.hasRole('Manager') && leave.status === 'EN_ATTENTE'
    );
    if (this.showActionsColumn && !this.displayedColumns.includes('actions')) {
      this.displayedColumns = [...this.displayedColumns, 'actions'];
    }
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  approveLeave(id: number): void {
    this.leaveService.approveLeave(id).subscribe({
      next: (response: ApiResponse) => {
        this.refreshLeaveRequests();
        this.showMessage(response.message);
      },
      error: (error: ApiResponse) => this.showError(error.message),
    });
  }

  rejectLeave(id: number): void {
    this.leaveService.rejectLeave(id).subscribe({
      next: (response: ApiResponse) => {
        this.refreshLeaveRequests();
        this.showMessage(response.message);
      },
      error: (error: ApiResponse) => this.showError(error.message),
    });
  }

  downloadAttachment(leaveId: number): void {
    this.leaveService.getReceivedAttachment(leaveId).subscribe({
      next: (data: Blob) => {
        const url = window.URL.createObjectURL(data);
        const a = document.createElement('a');
        document.body.appendChild(a);
        a.href = url;
        a.download = `attachment-${leaveId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => this.showError(error.message),
    });
  }

  showMessage(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
    });
  }

  showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
    });
  }
}
