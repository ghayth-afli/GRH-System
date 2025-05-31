import {
  Component,
  inject,
  Inject,
  OnInit,
  ViewChild,
  AfterViewInit,
} from '@angular/core';
import { NgForm } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MedicalVisit } from '../../models/medical-visit';
import { AppointmentService } from '../../services/appointment.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-take-registration-dialog',
  standalone: false,
  templateUrl: './take-registration-dialog.component.html',
  styleUrl: './take-registration-dialog.component.css',
})
export class TakeRegistrationDialogComponent implements OnInit {
  @ViewChild('takeSlotForm') takeSlotForm!: NgForm;

  timeSlots: string[] = [];
  isLoadingTimeSlots = true;
  selectedTimeSlot: string = '';

  private appointmentService = inject(AppointmentService);

  constructor(
    public dialogRef: MatDialogRef<TakeRegistrationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { visit: MedicalVisit }
  ) {}

  ngOnInit(): void {
    this.loadAvailableTimeSlots();
  }

  async loadAvailableTimeSlots(): Promise<void> {
    this.isLoadingTimeSlots = true;
    try {
      const visitDateStr = this.data.visit.visitDate; // "YYYY-MM-DD"
      const startTimeStr = this.data.visit.startTime; // "HH:mm:ss"
      const endTimeStr = this.data.visit.endTime; // "HH:mm:ss"

      const allPossibleSlots = this.generateAllPossibleSlots(
        visitDateStr,
        startTimeStr,
        endTimeStr
      );

      const takenTimeSlots = await this.fetchTakenTimeSlotsAsync(
        this.data.visit.id.toString(),
        visitDateStr
      );

      this.timeSlots = allPossibleSlots.filter(
        (slot) => !takenTimeSlots.includes(slot)
      );
    } catch (error) {
      console.error('Error loading time slots:', error);
      this.timeSlots = [];
    } finally {
      this.isLoadingTimeSlots = false;
    }
  }

  private generateAllPossibleSlots(
    visitDateStr: string,
    startTimeStr: string,
    endTimeStr: string
  ): string[] {
    const slots: string[] = [];
    const interval = 30 * 60 * 1000;

    const startDateTime = this.parseVisitDateTime(visitDateStr, startTimeStr);
    const endDateTime = this.parseVisitDateTime(visitDateStr, endTimeStr);

    let currentTime = startDateTime.getTime();
    const endTime = endDateTime.getTime();

    while (currentTime < endTime) {
      const currentDate = new Date(currentTime);
      const hours = currentDate.getHours().toString().padStart(2, '0');
      const minutes = currentDate.getMinutes().toString().padStart(2, '0');
      slots.push(`${hours}:${minutes}:00`);
      currentTime += interval;
    }
    return slots;
  }

  private parseVisitDateTime(dateStr: string, timeStr: string): Date {
    const [year, month, day] = dateStr.split('-').map(Number);
    const [hours, minutes, seconds] = timeStr.split(':').map(Number);
    return new Date(year, month - 1, day, hours, minutes, seconds || 0);
  }

  private async fetchTakenTimeSlotsAsync(
    medicalVisitId: string,
    visitDateStr: string
  ): Promise<string[]> {
    try {
      const appointments = await firstValueFrom(
        this.appointmentService.getAppointmentsByMedicalVisitId(medicalVisitId)
      );

      return appointments.map((appointment) => {
        const appointmentDate = new Date(appointment.timeSlot);
        const hours = appointmentDate.getHours().toString().padStart(2, '0');
        const minutes = appointmentDate
          .getMinutes()
          .toString()
          .padStart(2, '0');
        return `${hours}:${minutes}:00`;
      });
    } catch (error) {
      console.error('Error fetching taken appointments:', error);
      throw error;
    }
  }

  onSubmit() {
    if (!this.takeSlotForm || !this.takeSlotForm.valid) {
      if (this.takeSlotForm) {
        Object.keys(this.takeSlotForm.controls).forEach((field) => {
          const control = this.takeSlotForm.controls[field];
          control.markAsTouched({ onlySelf: true });
        });
      }
      return;
    }

    const visitDateStr = this.data.visit.visitDate;
    const selectedTimeSlotStr = this.takeSlotForm.value.timeSlot;

    if (!selectedTimeSlotStr) {
      console.error('Time slot is not selected.');
      return;
    }

    const localDateTimeStr = `${visitDateStr}T${selectedTimeSlotStr}`;

    const appointmentDate = new Date(localDateTimeStr);
    appointmentDate.setHours(appointmentDate.getHours() + 1);

    this.appointmentService
      .createAppointment({
        medicalVisitId: this.data.visit.id,
        timeSlot: appointmentDate,
      })
      .subscribe({
        next: () => {
          this.dialogRef.close(true);
        },
        error: (error) => {
          console.error('Error creating appointment:', error);
          this.dialogRef.close({
            error: 'Error creating appointment. Please try again.',
          });
        },
      });
  }

  onCancel() {
    this.dialogRef.close();
  }
}
