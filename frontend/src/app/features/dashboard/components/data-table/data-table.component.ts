import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Leave, LeaveResponse } from '../../models/leave-responses.interface';
import { LeaveService } from '../../services/leave.service';

@Component({
  selector: 'app-data-table',
  standalone: false,

  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.css',
})
export class DataTableComponent implements OnInit {
  leaveService = inject(LeaveService);
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
    this.leaveService.getAllLeaveRequests().subscribe((data: Leave[]) => {
      this.dataSource.data = data;
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    });
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }
}
