import { Component, inject, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MedicalVisit } from '../../models/medical-visit';
import { MatTableDataSource } from '@angular/material/table';
import { MedicalVisitService } from '../../services/medical-visit.service';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MedicalVisitFormModalComponent } from '../../components/medical-visit-form-modal/medical-visit-form-modal.component';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-medical-visits-page',
  standalone: false,

  templateUrl: './medical-visits-page.component.html',
  styleUrl: './medical-visits-page.component.css',
})
export class MedicalVisitsPageComponent {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  medicalVisitService = inject(MedicalVisitService);
  route = inject(Router);
  authService = inject(AuthService);
  public dialog = inject(MatDialog);
  dataSource!: MatTableDataSource<MedicalVisit>;
  pageSize = 10;
  pageSizeOptions = [10, 50, 100];
  displayedColumns = [
    'doctorName',
    'visitDate',
    'startTime',
    'endTime',
    'numberOfAppointments',
    'actions',
  ];
  medicalVisits!: MedicalVisit[];

  ngOnInit(): void {
    if (this.authService.hasRole('HR')) {
      this.displayedColumns = [
        'doctorName',
        'visitDate',
        'startTime',
        'endTime',
        'numberOfAppointments',
        'actions',
      ];
    }
    if (
      this.authService.hasRole('Employee') ||
      this.authService.hasRole('Manager')
    ) {
      this.displayedColumns = ['doctorName', 'visitDate', 'TakeAppointment'];
    }

    this.medicalVisitService.getMedicalVisits().subscribe({
      next: (medicalVisits) => {
        this.medicalVisits = medicalVisits;
        this.dataSource = new MatTableDataSource(this.medicalVisits);
        this.dataSource.paginator = this.paginator;
      },
      error: (error) => {
        console.error('Error fetching medical visits:', error);
      },
    });
    this.dataSource.paginator = this.paginator;
  }

  filter($event: KeyboardEvent) {
    this.dataSource.filter = ($event.target as HTMLInputElement).value
      .trim()
      .toLowerCase();
  }

  deleteVisit(element: MedicalVisit) {
    this.medicalVisitService.deleteMedicalVisit(element.id).subscribe({
      next: (response) => {
        console.log('Medical visit deleted:', response);
        this.medicalVisits = this.medicalVisits.filter(
          (visit) => visit.id !== element.id
        );
        this.dataSource.data = this.medicalVisits;
      },
      error: (error) => {
        console.error('Error deleting medical visit:', error);
      },
    });
  }
  addVisit() {
    this.dialog.open(MedicalVisitFormModalComponent, {
      width: '35%',
      height: 'auto',
    });
  }

  editVisit(element: MedicalVisit) {
    const dialogRef = this.dialog.open(MedicalVisitFormModalComponent, {
      width: '35%',
      height: 'auto',
      data: element,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Update the medical visit in the data source
        const index = this.medicalVisits.findIndex(
          (visit) => visit.id === result.id
        );
        if (index !== -1) {
          this.medicalVisits[index] = result;
          this.dataSource.data = this.medicalVisits;
        }
      }
    });
  }
  exportcsv() {
    if (!this.medicalVisits || this.medicalVisits.length === 0) {
      alert('No data to export');
      return;
    }

    const csvHeaders = [
      'Doctor Name',
      'Visit Date',
      'Start Time',
      'End Time',
      'Number of Appointments',
    ];
    const rows = this.medicalVisits.map((visit) => {
      const formattedDate =
        visit.visitDate instanceof Date
          ? visit.visitDate.toLocaleDateString()
          : new Date(visit.visitDate).toLocaleDateString();
      const escapedDoctorName = `"${visit.doctorName.replace(/"/g, '""')}"`;
      return [
        escapedDoctorName,
        formattedDate,
        visit.startTime,
        visit.endTime,
        visit.numberOfAppointments,
      ].join(',');
    });

    const csvContent = [csvHeaders.join(','), ...rows].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'medical_visits.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }
  exportPdf() {
    if (!this.medicalVisits || this.medicalVisits.length === 0) {
      alert('No data to export');
      return;
    }

    const doc = new jsPDF();
    const headers = [
      [
        'Doctor Name',
        'Visit Date',
        'Start Time',
        'End Time',
        'Number of Appointments',
      ],
    ];
    const data = this.medicalVisits.map((visit) => {
      const formattedDate =
        visit.visitDate instanceof Date
          ? visit.visitDate.toLocaleDateString()
          : new Date(visit.visitDate).toLocaleDateString();
      return [
        visit.doctorName,
        formattedDate,
        visit.startTime,
        visit.endTime,
        visit.numberOfAppointments.toString(),
      ];
    });

    autoTable(doc, {
      head: headers,
      body: data,
      startY: 20,
      theme: 'grid',
      styles: { fontSize: 10 },
      headStyles: { fillColor: [41, 128, 185], textColor: 255 },
      columnStyles: {
        0: { cellWidth: 40 },
        1: { cellWidth: 30 },
        2: { cellWidth: 30 },
        3: { cellWidth: 30 },
        4: { cellWidth: 20 },
      },
    });

    doc.text('Medical Visits Report', 14, 15);
    doc.save('medical_visits.pdf');
  }

  makeAppointment(element: MedicalVisit) {}
}
