import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Leave, LeaveResponse } from '../../models/leave-responses.interface';
import { LeaveService } from '../../services/leave.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-data-table',
  standalone: false,

  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.css',
})
export class DataTableComponent implements OnInit {
  private leaveService = inject(LeaveService);
  private authService = inject(AuthService);
  displayedColumns: string[] = [
    'Name',
    'department',
    'startDate',
    'endDate',
    'leaveType',
    'status',
  ];

  dataSource = new MatTableDataSource<Leave>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  ngOnInit(): void {
    let columns = [...this.displayedColumns];
    if (this.isHr() || this.isManager()) {
      columns.push('attachments');
    }
    if (this.isManager()) {
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
  isManager(): boolean {
    return this.authService.hasRole('Manager');
  }
  isHr(): boolean {
    return this.authService.hasRole('HR');
  }
  isEmployee(): boolean {
    return this.authService.hasRole('Employee');
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
      error: (error) => {
        console.error('There was an error!', error);
      },
    });
  }
  rejectLeave(id: number) {
    this.leaveService.rejectLeave(id).subscribe({
      next: (response: { message: string }) => {
        console.log(response.message);
        this.leaveService.getAllLeaveRequests().subscribe((data: Leave[]) => {
          this.dataSource.data = data;
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
        });
      },
      error: (error) => {
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
