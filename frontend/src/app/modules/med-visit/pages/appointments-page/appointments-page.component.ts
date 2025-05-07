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
  ];
  appointments: Appointment[] = [];

  ngOnInit(): void {
    this.fetchRouteParams();
    this.loadAppointments();
    this.initializeDataSource();
    this.subscribeToEventNotifications();
  }

  private sseService = inject(SseService<NotificationData<Appointment>>);
  private newAppointmentSubscription: Subscription | null = null;
  private updateAppointmentSubscription: Subscription | null = null;
  ngOnDestroy(): void {
    // Unsubscribe from all subscriptions
    if (this.newAppointmentSubscription) {
      this.newAppointmentSubscription.unsubscribe();
    }
    if (this.updateAppointmentSubscription) {
      this.updateAppointmentSubscription.unsubscribe();
    }
    // Close the SSE connection
    this.sseService.disconnect();
  }
  subscribeToEventNotifications(): void {
    this.subscribeToAppointmentEvent();
    this.subscribeToUpdatedAppointmentEvent();
  }

  //updatedLeaveRequestEvent
  private subscribeToAppointmentEvent(): void {
    this.newAppointmentSubscription = this.sseService
      .connect('UPDATED_MEDICAL_VISIT')
      .subscribe({
        next: (Event: NotificationData<Appointment>) => {
          this.loadAppointments();
        },
      });
  }

  private subscribeToUpdatedAppointmentEvent(): void {
    this.updateAppointmentSubscription = this.sseService
      .connect('UPDATED_MEDICAL_VISIT')
      .subscribe({
        next: (Event: NotificationData<Appointment>) => {
          this.loadAppointments();
        },
      });
  }

  exportCsv(): void {
    const csvData = this.mapAppointmentsToCsvData();
    const csvContent = this.generateCsvContent(csvData);
    this.downloadCsv(csvContent);
  }

  exportPdf(): void {
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
      body: this.mapAppointmentsToTableData(),
    });
    doc.save('appointments-report.pdf');
  }

  filter($event: KeyboardEvent): void {
    const filterValue = ($event.target as HTMLInputElement).value
      .trim()
      .toLowerCase();
    this.dataSource.filter = filterValue;
  }

  private fetchRouteParams(): void {
    this.route.paramMap.subscribe({
      next: (params) => {
        this.medicalVisitId = params.get('id');
        console.log('Medical Visit ID:', this.medicalVisitId);
      },
      error: (error) => {
        console.error('Error fetching route parameters:', error);
      },
    });
  }

  private loadAppointments(): void {
    if (!this.medicalVisitId) return;

    this.appointmentService
      .getAppointmentsByMedicalVisitId(this.medicalVisitId)
      .subscribe({
        next: (appointments) => {
          this.appointments = appointments;
          this.updateDataSource();
        },
        error: (error) => {
          console.error('Error fetching appointments:', error);
        },
      });
  }

  private initializeDataSource(): void {
    this.dataSource = new MatTableDataSource(this.appointments);
    this.dataSource.paginator = this.paginator;
  }

  private updateDataSource(): void {
    this.dataSource.data = this.appointments;
  }

  private mapAppointmentsToTableData(): any[] {
    return this.appointments.map((appointment) => [
      appointment.id,
      appointment.doctorName,
      appointment.timeSlot.toLocaleString(),
      appointment.status,
      appointment.employeeFullName,
      appointment.employeeEmail,
    ]);
  }

  private mapAppointmentsToCsvData(): any[] {
    return this.appointments.map((appointment) => ({
      ID: appointment.id,
      DoctorName: appointment.doctorName,
      TimeSlot: appointment.timeSlot.toLocaleString(),
      Status: appointment.status,
      EmployeeFullName: appointment.employeeFullName,
      EmployeeEmail: appointment.employeeEmail,
    }));
  }

  private generateCsvContent(csvData: any[]): string {
    return (
      'data:text/csv;charset=utf-8,' +
      Object.keys(csvData[0]).join(',') +
      '\n' +
      csvData
        .map((row) => Object.values(row).join(','))
        .join('\n')
        .replace(/,/g, ';')
    );
  }

  private downloadCsv(csvContent: string): void {
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', 'appointments-report.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}
