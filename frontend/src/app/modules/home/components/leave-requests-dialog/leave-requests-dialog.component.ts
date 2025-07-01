import {
  Component,
  Inject,
  ViewChild,
  AfterViewInit,
  inject,
} from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { Leave } from '../../../leave/models/leave';
import { LeaveService } from '../../../leave/services/leave.service';
import { ConfirmationModalComponent } from '../../../../shared/components/confirmation-modal/confirmation-modal.component';
interface LeaveRequestDisplay {
  id: number;
  status: string;
  startDate: string;
  endDate: string;
  leaveType: string;
  duration: string;
  color: string;
}
@Component({
  selector: 'app-leave-requests-dialog',
  standalone: false,
  templateUrl: './leave-requests-dialog.component.html',
  styleUrl: './leave-requests-dialog.component.css',
})
export class LeaveRequestsDialogComponent {
  displayedColumns: string[] = [
    'status',
    'startDate',
    'endDate',
    'leaveType',
    'duration',
    'actions',
  ];
  dataSource: MatTableDataSource<LeaveRequestDisplay>;

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  private dialog = inject(MatDialog);

  leaveService = inject(LeaveService);

  constructor(
    public dialogRef: MatDialogRef<LeaveRequestsDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      leaves: Leave[];
      legendItems: { type: string; color: string }[];
    }
  ) {
    const leavesArray = Array.isArray(data.leaves) ? data.leaves : [];
    const leaveRequests: LeaveRequestDisplay[] = leavesArray.map((leave) => ({
      id: leave.id,
      status: leave.status,
      startDate: leave.startDate,
      endDate: leave.endDate,
      leaveType: leave.leaveType,
      duration: this.calculateDurationString(leave),
      color:
        data.legendItems.find((item) => item.type === leave.leaveType)?.color ||
        '#000000',
    }));
    this.dataSource = new MatTableDataSource(leaveRequests);
  }

  ngAfterViewInit() {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  calculateDurationString(request: Leave): string {
    try {
      if (
        request.leaveType === 'AUTORISATION' &&
        request.startHOURLY &&
        request.endHOURLY
      ) {
        const start = new Date(`${request.startDate}T${request.startHOURLY}`);
        const end = new Date(`${request.endDate}T${request.endHOURLY}`);
        if (isNaN(start.getTime()) || isNaN(end.getTime())) return 'N/A';

        let diffMs = end.getTime() - start.getTime();
        const hours = Math.floor(diffMs / 3600000);
        diffMs -= hours * 3600000;
        const minutes = Math.floor(diffMs / 60000);

        let durationStr = '';
        if (hours > 0) durationStr += `${hours}h `;
        if (minutes > 0) durationStr += `${minutes}m`;
        return durationStr.trim() || '0m';
      } else {
        const start = new Date(request.startDate);
        const end = new Date(request.endDate);
        if (isNaN(start.getTime()) || isNaN(end.getTime())) return '0';
        const diffTime = Math.abs(end.getTime() - start.getTime());
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
        return `${diffDays} day${diffDays > 1 ? 's' : ''}`;
      }
    } catch (e) {
      return 'N/A';
    }
  }

  onCancelLeave(leaveId: number): void {
    const dialogRef = this.dialog.open(ConfirmationModalComponent, {
      data: {
        title: 'Cancel Leave Request',
        message: 'Are you sure you want to cancel this leave request?',
        confirmButtonText: 'Yes, Cancel',
        cancelButtonText: 'No, Keep',
      },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result?.confirmed) {
        this.cancelLeaveRequest(leaveId);
      }
    });
  }

  cancelLeaveRequest(leaveId: number) {
    this.leaveService.cancelLeave(leaveId).subscribe({
      next: (response) => {
        console.log('Leave request cancelled:', response);
        const leaveRequest = this.dataSource.data.find(
          (leave) => leave.id === leaveId
        );
        if (leaveRequest) {
          leaveRequest.status = 'CANCELLED';
          this.dataSource.data = [...this.dataSource.data];
        }
      },
      error: (error) => {
        console.error('Error cancelling leave request:', error);
      },
    });
  }
}
