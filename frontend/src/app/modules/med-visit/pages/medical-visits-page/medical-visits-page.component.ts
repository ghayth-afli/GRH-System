import { ChangeDetectorRef, Component, inject, ViewChild } from '@angular/core';
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
import { AppointmentService } from '../../services/appointment.service';
import { AppointmentModalComponent } from '../../components/appointment-modal/appointment-modal.component';
import { Appointment } from '../../models/appointment';
import { catchError, map, Observable, of, Subscription } from 'rxjs';
import { NotificationData } from '../../../../core/models/NotificationData';
import { SseService } from '../../../../core/services/sse.service';

@Component({
  selector: 'app-medical-visits-page',
  standalone: false,
  templateUrl: './medical-visits-page.component.html',
  styleUrl: './medical-visits-page.component.css',
})
export class MedicalVisitsPageComponent {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  medicalVisitService = inject(MedicalVisitService);
  appointmentService = inject(AppointmentService);
  private cdr = inject(ChangeDetectorRef);
  route = inject(Router);
  authService = inject(AuthService);
  public dialog = inject(MatDialog);
  private sseService = inject(SseService);
  dataSource!: MatTableDataSource<MedicalVisit>;
  pageSize = 10;
  pageSizeOptions = [10, 50, 100];
  displayedColumns: string[] = [];
  medicalVisits: MedicalVisit[] = [];
  private newVisitSubscription: Subscription | null = null;
  private updateVisitSubscription: Subscription | null = null;
  private deletedVisitSubscription: Subscription | null = null;

  ngOnInit(): void {
    this.setDisplayedColumns();
    this.loadMedicalVisits();
    this.loadAppointmentsForEmployeeOrManager();
    this.subscribeToEventNotifications();
  }

  subscribeToEventNotifications(): void {
    this.subscribeToNewMedicalVisitEvent();
    this.subscribeToUpdatedMedicalVisitEvent();
    this.subscribeToDeletedMedicalVisitEvent();
  }

  private subscribeToDeletedMedicalVisitEvent(): void {
    this.deletedVisitSubscription = this.sseService
      .connect('DELETED_MEDICAL_VISIT')
      .subscribe({
        next: (event: NotificationData<MedicalVisit>) => {
          console.log(
            'Raw deleted medical visit event:',
            event.payload['medicalVisit']
          );
          this.removeVisitFromDataSource(event.payload['medicalVisit']);
        },
        error: (error) => {
          console.error('Error in DELETED_MEDICAL_VISIT subscription:', error);
        },
      });
  }

  private subscribeToNewMedicalVisitEvent(): void {
    this.newVisitSubscription = this.sseService
      .connect('CREATED_MEDICAL_VISIT')
      .subscribe({
        next: (event: NotificationData<MedicalVisit>) => {
          const transformedVisit = this.transformMedicalVisit(
            event.payload['medicalVisit']
          );
          this.medicalVisits = [transformedVisit, ...this.medicalVisits];
          this.initializeDataSource();
          console.log('New medical visit event received:', transformedVisit);
          console.log('Data source data:', this.dataSource.data);
        },
        error: (error) => {
          console.error('Error in CREATED_MEDICAL_VISIT subscription:', error);
        },
      });
  }

  private subscribeToUpdatedMedicalVisitEvent(): void {
    console.log('Subscribing to updated medical visit event...');
    this.updateVisitSubscription = this.sseService
      .connect('UPDATED_MEDICAL_VISIT')
      .subscribe({
        next: (event: NotificationData<MedicalVisit>) => {
          console.log(
            'Raw updated medical visit event:',
            event.payload['medicalVisit']
          );
          const transformedVisit = this.transformMedicalVisit(
            event.payload['medicalVisit']
          );
          console.log('Transformed updated medical visit:', transformedVisit);
          this.updateVisitInDataSource(transformedVisit);
        },
        error: (error) => {
          console.error('Error in UPDATED_MEDICAL_VISIT subscription:', error);
        },
      });
  }

  ngOnDestroy(): void {
    // Unsubscribe from all subscriptions
    if (this.newVisitSubscription) {
      this.newVisitSubscription.unsubscribe();
    }
    if (this.updateVisitSubscription) {
      this.updateVisitSubscription.unsubscribe();
    }
    if (this.deletedVisitSubscription) {
      this.deletedVisitSubscription.unsubscribe();
    }
    // Close the SSE connection
    this.sseService.disconnect();
  }

  private transformMedicalVisit(visit: any): MedicalVisit {
    return {
      id: visit.id,
      doctorName: visit.doctorName,
      visitDate: new Date(this.formatDate(visit.visitDate)),
      startTime: this.formatTime(visit.startTime),
      endTime: this.formatTime(visit.endTime),
      numberOfAppointments: visit.numberOfAppointments,
    };
  }

  private formatDate(dateArray: number[]): string {
    if (!Array.isArray(dateArray) || dateArray.length !== 3) {
      console.error('Invalid date array:', dateArray);
      return new Date().toISOString().split('T')[0]; // Fallback to current date
    }
    const [year, month, day] = dateArray;
    return `${year}-${month.toString().padStart(2, '0')}-${day
      .toString()
      .padStart(2, '0')}`;
  }

  private formatTime(timeArray: number[]): string {
    if (!Array.isArray(timeArray) || timeArray.length !== 2) {
      console.error('Invalid time array:', timeArray);
      return '00:00:00'; // Fallback
    }
    const [hour, minute] = timeArray;
    return `${hour.toString().padStart(2, '0')}:${minute
      .toString()
      .padStart(2, '0')}:00`;
  }

  makeAppointment(element: MedicalVisit): void {
    this.dialog.open(AppointmentModalComponent, {
      width: '35%',
      height: 'auto',
      data: element,
    });
  }

  cancelAppointment(element: MedicalVisit): void {
    const user = localStorage.getItem('user');
    const parsedUser = user ? JSON.parse(user) : null;
    const appointment = this.findAppointmentByEmployeeId(
      parsedUser.id,
      element.id
    );
    appointment.subscribe((appointment) => {
      if (appointment) {
        this.appointmentService.deleteAppointment(appointment.id).subscribe({
          next: () => {
            this.loadMedicalVisits();
          },
          error: (error) => {
            console.error('Error deleting appointment:', error);
          },
        });
      } else {
        console.error('No appointment found for the given medical visit ID');
      }
    });
  }

  filter($event: KeyboardEvent): void {
    this.dataSource.filter = ($event.target as HTMLInputElement).value
      .trim()
      .toLowerCase();
  }

  deleteVisit(element: MedicalVisit): void {
    this.medicalVisitService.deleteMedicalVisit(element.id).subscribe({
      next: () => {
        //this.removeVisitFromDataSource(element);
      },
      error: (error) => {
        console.error('Error deleting medical visit:', error);
      },
    });
  }

  addVisit(): void {
    this.dialog.open(MedicalVisitFormModalComponent, {
      width: '35%',
      height: 'auto',
    });
  }

  editVisit(element: MedicalVisit): void {
    const dialogRef = this.dialog.open(MedicalVisitFormModalComponent, {
      width: '35%',
      height: 'auto',
      data: element,
    });
  }

  exportcsv(): void {
    if (!this.medicalVisits || this.medicalVisits.length === 0) {
      alert('No data to export');
      return;
    }

    const csvContent = this.generateCsvContent();
    this.downloadFile(
      csvContent,
      'medical_visits.csv',
      'text/csv;charset=utf-8;'
    );
  }

  exportPdf(): void {
    if (!this.medicalVisits || this.medicalVisits.length === 0) {
      alert('No data to export');
      return;
    }

    const doc = new jsPDF();
    this.generatePdfContent(doc);
    doc.save('medical_visits.pdf');
  }

  findAppointmentByEmployeeId(
    employeeId: string,
    medicalVisitId: number
  ): Observable<Appointment | null> {
    return this.appointmentService.getAppointmentsByEmployeeId(employeeId).pipe(
      map(
        (appointments) =>
          appointments.find(
            (appointment) => appointment.medicalVisitId === medicalVisitId
          ) || null
      ),
      catchError((error) => {
        console.error('Error fetching appointments:', error);
        return of(null);
      })
    );
  }

  private setDisplayedColumns(): void {
    if (this.authService.hasRole('HR')) {
      this.displayedColumns = [
        'doctorName',
        'visitDate',
        'startTime',
        'endTime',
        'numberOfAppointments',
        'actions',
        'choosenSlot',
        'TakeAppointment',
      ];
    } else if (
      this.authService.hasRole('Employee') ||
      this.authService.hasRole('Manager')
    ) {
      this.displayedColumns = [
        'doctorName',
        'visitDate',
        'choosenSlot',
        'TakeAppointment',
      ];
    }
  }

  private loadMedicalVisits(): void {
    this.medicalVisitService.getMedicalVisits().subscribe({
      next: (medicalVisits) => {
        this.medicalVisits = medicalVisits;
        this.initializeDataSource();
      },
      error: (error) => {
        console.error('Error fetching medical visits:', error);
      },
    });
  }

  private loadAppointmentsForEmployeeOrManager(): void {
    const user = localStorage.getItem('user');
    if (user) {
      const userId = JSON.parse(user)?.id;
      this.appointmentService.getAppointmentsByEmployeeId(userId).subscribe({
        next: (appointments) => {
          this.updateMedicalVisitsWithAppointments(appointments);
        },
        error: (error) => {
          console.error('Error fetching appointments:', error);
        },
      });
    }
  }

  private updateMedicalVisitsWithAppointments(appointments: any[]): void {
    this.medicalVisits = this.medicalVisits.map((visit) => {
      const appointment = appointments.find(
        (appointment) => appointment.medicalVisitId === visit.id
      );
      if (appointment) {
        return { ...visit, choosenSlot: appointment.timeSlot };
      }
      return visit;
    });
    this.initializeDataSource();
  }

  private initializeDataSource(): void {
    this.dataSource = new MatTableDataSource(this.medicalVisits);
    this.dataSource.paginator = this.paginator;
  }

  private removeVisitFromDataSource(element: MedicalVisit): void {
    this.medicalVisits = this.medicalVisits.filter(
      (visit) => visit.id !== element.id
    );
    this.dataSource.data = this.medicalVisits;
  }

  private updateVisitInDataSource(updatedVisit: MedicalVisit): void {
    const index = this.medicalVisits.findIndex(
      (visit) => visit.id === updatedVisit.id
    );
    if (index !== -1) {
      this.medicalVisits[index] = updatedVisit;
      this.dataSource.data = this.medicalVisits;
      this.cdr.detectChanges();
      console.log('Updated medical visit in data source:', updatedVisit);
    } else {
      console.warn('Medical visit not found for update:', updatedVisit.id);
    }
  }

  private generateCsvContent(): string {
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

    return [csvHeaders.join(','), ...rows].join('\n');
  }

  private downloadFile(
    content: string,
    fileName: string,
    mimeType: string
  ): void {
    const blob = new Blob([content], { type: mimeType });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', fileName);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  private generatePdfContent(doc: jsPDF): void {
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
  }
}
