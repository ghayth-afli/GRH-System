import { Component, inject, ViewChild } from '@angular/core';
import { LeaveService } from '../../services/leave.service';
import { AuthService } from '../../../../core/services/auth.service';
import { MatTableDataSource } from '@angular/material/table';
import { Leave } from '../../models/leave';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';

@Component({
  selector: 'app-leave-requests-data-table',
  standalone: false,

  templateUrl: './leave-requests-data-table.component.html',
  styleUrl: './leave-requests-data-table.component.css',
})
export class LeaveRequestsDataTableComponent {
  dataSource = new MatTableDataSource<Leave>();
  private leaveService = inject(LeaveService);
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

  ngOnInit(): void {
    let columns = [...this.displayedColumns];
    if (this.authService.hasRole('HR') || this.authService.hasRole('Manager')) {
      columns.push('attachments');
    }
    if (this.authService.hasRole('Manager')) {
      columns.push('actions');
    }
    this.displayedColumns = columns;

    this.leaveService.getAllLeaveRequests().subscribe({
      next: (data: Leave[]) => {
        this.dataSource.data = data;
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
      },
      error: (error) => {
        console.error('There was an error!', error);
      },
    });
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  approveLeave(id: number) {
    this.leaveService.approveLeave(id).subscribe({
      next: (response: { message: string }) => {
        console.log(response.message);

        this.leaveService.getAllLeaveRequests().subscribe((data: Leave[]) => {
          this.dataSource.data = data;
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
        });
      },
      error: (error: { message: string }) => {
        console.error('There was an error!', error);
      },
    });
  }
  rejectLeave(id: number) {
    this.leaveService.rejectLeave(id).subscribe({
      next: (response: { message: string }) => {
        this.leaveService.getAllLeaveRequests().subscribe((data: Leave[]) => {
          this.dataSource.data = data;
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
        });
      },
      error: (error: { message: string }) => {
        console.error('There was an error!', error);
      },
    });
  }

  downloadAttachment(leaveId: number) {
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
      error: (error) => {
        console.error('There was an error!', error);
      },
    });
  }
}
