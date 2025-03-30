import { Component, inject, ViewChild } from '@angular/core';
import { Appointment } from '../../models/appointment';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { AppointmentStatus } from '../../models/appointment-status';
import { AppointmentService } from '../../services/appointment.service';
import { ActivatedRoute } from '@angular/router';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-appointments-page',
  standalone: false,

  templateUrl: './appointments-page.component.html',
  styleUrl: './appointments-page.component.css',
})
export class AppointmentsPageComponent {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  appointmentService = inject(AppointmentService);
  route = inject(ActivatedRoute);
  medicalVisitId: string | null = null;
  dataSource!: MatTableDataSource<Appointment>;
  pageSize = 10;
  pageSizeOptions = [10, 50, 100];
  displayedColumns = [
    'id',
    'employeeFullName',
    'employeeEmail',
    'timeSlot',
    'status',
    'actions',
  ];
  appointments: Appointment[] = [
    {
      id: 1,
      medicalVisitId: 1,
      doctorName: 'Dr. Smith',
      timeSlot: new Date('2023-10-01T09:00:00'),
      status: AppointmentStatus.PLANIFIE,
      employeeFullName: 'John Doe',
      employeeEmail: 'john@gmail.com',
    },
  ];
  ngOnInit(): void {
    this.route.paramMap.subscribe({
      next: (params) => {
        this.medicalVisitId = params.get('id');
        console.log('Medical Visit ID:', this.medicalVisitId);
      },
      error: (error) => {
        console.error('Error fetching route parameters:', error);
      },
    });
    this.appointmentService
      .getAppointmentsByMedicalVisitId(this.medicalVisitId!)
      .subscribe({
        next: (appointments) => {
          this.appointments = appointments;
          this.dataSource = new MatTableDataSource(this.appointments);
          this.dataSource.paginator = this.paginator;
        },
        error: (error) => {
          console.error('Error fetching appointments:', error);
        },
      });
    this.dataSource = new MatTableDataSource(this.appointments);
    this.dataSource.paginator = this.paginator;
  }

  filter($event: KeyboardEvent) {
    this.dataSource.filter = ($event.target as HTMLInputElement).value
      .trim()
      .toLowerCase();
  }
  exportPdf() {
    const doc = new jsPDF();
    doc.text('Appointments Report', 14, 16);
    autoTable(doc, {
      head: [
        [
          'ID',
          'Doctor Name',
          'Time Slot',
          'Status',
          'Employee Full Name',
          'Employee Email',
        ],
      ],
      body: this.appointments.map((appointment) => [
        appointment.id,
        appointment.doctorName,
        appointment.timeSlot.toLocaleString(),
        appointment.status,
        appointment.employeeFullName,
        appointment.employeeEmail,
      ]),
    });
    doc.save('appointments-report.pdf');
  }
  exportcsv() {
    const csvData = this.appointments.map((appointment) => {
      return {
        ID: appointment.id,
        DoctorName: appointment.doctorName,
        TimeSlot: appointment.timeSlot.toLocaleString(),
        Status: appointment.status,
        EmployeeFullName: appointment.employeeFullName,
        EmployeeEmail: appointment.employeeEmail,
      };
    });

    const csvContent =
      'data:text/csv;charset=utf-8,' +
      Object.keys(csvData[0]).join(',') +
      '\n' +
      csvData
        .map((row) => Object.values(row).join(','))
        .join('\n')
        .replace(/,/g, ';');

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', 'appointments-report.csv');
    document.body.appendChild(link); // Required for FF
    link.click();
    document.body.removeChild(link); // Clean up the DOM
  }
}
