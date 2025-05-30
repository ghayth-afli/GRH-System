import { Component, inject, ViewChild } from '@angular/core';
import { Appointment } from '../../models/appointment';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { AppointmentStatus } from '../../models/appointment-status';
import { AppointmentService } from '../../services/appointment.service';
import { ActivatedRoute } from '@angular/router';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { Subscription } from 'rxjs';
import { SseService } from '../../../../core/services/sse.service';
import { NotificationData } from '../../../../core/models/NotificationData';

@Component({
  selector: 'app-appointments-page',
  standalone: false,
  templateUrl: './appointments-page.component.html',
  styleUrl: './appointments-page.component.css',
})
export class AppointmentsPageComponent {
  appointments: Appointment[] = [
    {
      id: 101,
      medicalVisitId: 789,
      doctorName: 'Dr. Emily Carter',
      timeSlot: new Date('2025-09-15T14:00:00'),
      status: AppointmentStatus.PLANIFIE,
      employeeFullName: 'John Doe',
      employeeEmail: 'john.doe@example.com',
    },
  ];

  filteredAppointments: Appointment[] = [];
  pagedAppointments: Appointment[] = [];
  pageSize = 5;
  pageSizeOptions = [5, 10, 25, 100];
  currentPage = 1;
  totalPages = 1;

  employeeNameFilter = '';
  statusFilter = 'all';

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    const visitId = Number(this.route.snapshot.paramMap.get('id'));
    this.filteredAppointments = this.appointments.filter(
      (a) => a.medicalVisitId === visitId
    );
    this.applyFilters();
  }

  applyFilters() {
    let filtered = this.appointments.filter((a) => {
      const matchesName = this.employeeNameFilter
        ? a.employeeFullName
            .toLowerCase()
            .includes(this.employeeNameFilter.toLowerCase())
        : true;
      const matchesStatus =
        this.statusFilter !== 'all' ? a.status === this.statusFilter : true;
      return matchesName && matchesStatus;
    });

    // Ensure we still filter by visitId
    const visitId = Number(this.route.snapshot.paramMap.get('id'));
    filtered = filtered.filter((a) => a.medicalVisitId === visitId);

    this.filteredAppointments = filtered;
    this.updatePagination();
  }

  updatePagination() {
    this.totalPages = Math.ceil(
      this.filteredAppointments.length / this.pageSize
    );
    this.currentPage = Math.min(this.currentPage, this.totalPages || 1);
    const start = (this.currentPage - 1) * this.pageSize;
    this.pagedAppointments = this.filteredAppointments.slice(
      start,
      start + this.pageSize
    );
  }

  changePage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePagination();
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxPages = 5;
    let startPage = Math.max(1, this.currentPage - Math.floor(maxPages / 2));
    let endPage = Math.min(this.totalPages, startPage + maxPages - 1);

    if (endPage - startPage + 1 < maxPages) {
      startPage = Math.max(1, endPage - maxPages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  }
}
