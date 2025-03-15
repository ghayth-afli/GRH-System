import { Component, inject, ViewChild } from '@angular/core';
import { LeaveService } from '../../services/leave.service';
import { AuthService } from '../../../../core/services/auth.service';
import { MatTableDataSource } from '@angular/material/table';
import { Leave } from '../../models/leave';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { map, Observable } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-leave-requests-page',
  standalone: false,

  templateUrl: './leave-requests-page.component.html',
  styleUrl: './leave-requests-page.component.css',
})
export class LeaveRequestsPageComponent {
  private authService = inject(AuthService);

  leaveRequests$: Observable<Leave[]>;

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

  constructor(private route: ActivatedRoute) {
    this.leaveRequests$ = this.route.data.pipe(
      map((data) => data['leaveRequests'])
    );
  }

  dataSource = new MatTableDataSource<Leave>();

  ngOnInit(): void {
    let columns = [...this.displayedColumns];
    if (this.isHr() || this.isManager()) {
      columns.push('attachments');
    }
    if (this.isManager()) {
      columns.push('actions');
    }
    this.displayedColumns = columns;

    this.leaveRequests$.subscribe({
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

  isManager(): boolean {
    return this.authService.hasRole('Manager');
  }
  isHr(): boolean {
    return this.authService.hasRole('HR');
  }
  isEmployee(): boolean {
    return this.authService.hasRole('Employee');
  }
}
