import { Component, Inject, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MedicalVisit } from '../../models/medical-visit';
import { AppointmentService } from '../../services/appointment.service';

@Component({
  selector: 'app-appointment-modal',
  standalone: false,
  templateUrl: './appointment-modal.component.html',
  styleUrl: './appointment-modal.component.css',
})
export class AppointmentModalComponent {
  dialogRef = inject(MatDialogRef);
  timeSlots: Date[] = [];
  selectedTimeSlot: Date = new Date();
  isLoading = false;
  appointmentService = inject(AppointmentService);

  constructor(@Inject(MAT_DIALOG_DATA) public data: MedicalVisit) {}

  ngOnInit(): void {
    this.initializeTimeSlots();
    this.fetchTakenSlots();
  }

  onCancel() {
    this.dialogRef.close();
  }

  onClose() {
    this.dialogRef.close();
  }

  onSubmit() {
    this.isLoading = true;
    const appointmentData = this.createAppointmentData();
    this.submitAppointment(appointmentData);
  }

  private initializeTimeSlots(): void {
    this.timeSlots = [];
    const visitDate = new Date(this.data.visitDate);

    if (isNaN(visitDate.getTime())) {
      console.error('Error: visitDate is invalid', this.data.visitDate);
      return;
    }

    const startTime = this.createDateFromTime(this.data.startTime);
    const endTime = this.createDateFromTime(this.data.endTime);

    if (!startTime || !endTime) {
      console.error('Error: startTime or endTime is invalid', this.data);
      return;
    }

    this.generateTimeSlots(visitDate, startTime, endTime);
  }

  private generateTimeSlots(
    visitDate: Date,
    startTime: Date,
    endTime: Date
  ): void {
    let currentTime = new Date(startTime);
    while (currentTime <= endTime) {
      const slotDate = new Date(visitDate);
      slotDate.setHours(currentTime.getHours(), currentTime.getMinutes(), 0, 0);
      this.timeSlots.push(slotDate);
      currentTime.setMinutes(currentTime.getMinutes() + 30);
    }
    console.log('Generated time slots:', this.timeSlots);
  }

  private fetchTakenSlots(): void {
    const takedSlots: Date[] = [];
    this.appointmentService
      .getAppointmentsByMedicalVisitId(this.data.id.toString())
      .subscribe({
        next: (response) => {
          response.forEach((appointment) => {
            const appointmentDate = new Date(appointment.timeSlot);
            takedSlots.push(appointmentDate);
          });
          this.filterAvailableTimeSlots(takedSlots);
        },
        error: (error) => {
          console.error('Error fetching appointments:', error);
        },
      });
  }

  private filterAvailableTimeSlots(takedSlots: Date[]): void {
    this.timeSlots = this.timeSlots.filter((timeSlot) => {
      return !takedSlots.some(
        (takedSlot) =>
          timeSlot.getHours() === takedSlot.getHours() &&
          timeSlot.getMinutes() === takedSlot.getMinutes()
      );
    });
  }

  private createDateFromTime(timeString: string): Date | null {
    if (!timeString) return null;

    const [hours, minutes, seconds] = timeString.split(':').map(Number);
    if (isNaN(hours) || isNaN(minutes)) return null;

    const date = new Date();
    date.setHours(hours, minutes, seconds || 0, 0);
    return date;
  }

  private createAppointmentData(): { medicalVisitId: number; timeSlot: Date } {
    return {
      medicalVisitId: this.data.id,
      timeSlot: this.selectedTimeSlot,
    };
  }

  private submitAppointment(appointmentData: {
    medicalVisitId: number;
    timeSlot: Date;
  }): void {
    console.log('Appointment Data:', appointmentData);
    this.appointmentService.createAppointment(appointmentData).subscribe({
      next: (response) => {
        console.log('Appointment created:', response);
        this.isLoading = false;
        this.dialogRef.close(appointmentData);
      },
      error: (error) => {
        console.error('Error creating appointment:', error);
        this.isLoading = false;
      },
    });
  }
}
