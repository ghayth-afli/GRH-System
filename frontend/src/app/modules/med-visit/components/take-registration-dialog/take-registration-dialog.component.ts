import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MedicalVisit } from '../../models/medical-visit';

@Component({
  selector: 'app-take-registration-dialog',
  standalone: false,
  templateUrl: './take-registration-dialog.component.html',
  styleUrl: './take-registration-dialog.component.css',
})
export class TakeRegistrationDialogComponent {
  appointmentForm: FormGroup;
  timeSlots: string[] = [];

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<TakeRegistrationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { visit: MedicalVisit }
  ) {
    this.appointmentForm = this.fb.group({
      timeSlot: ['', Validators.required],
    });

    this.generateTimeSlots();
  }

  generateTimeSlots() {
    const start = this.parseTime(this.data.visit.startTime);
    const end = this.parseTime(this.data.visit.endTime);
    const interval = 30 * 60 * 1000; // 30 minutes in milliseconds

    for (let time = start; time < end; time += interval) {
      const date = new Date(time);
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      this.timeSlots.push(`${hours}:${minutes}:00`);
    }
  }

  parseTime(time: string): number {
    const [hours, minutes, seconds] = time.split(':').map(Number);
    const date = new Date();
    date.setHours(hours, minutes, seconds, 0);
    return date.getTime();
  }

  onSubmit() {
    if (this.appointmentForm.valid) {
      this.dialogRef.close(this.appointmentForm.value.timeSlot);
    }
  }

  onCancel() {
    this.dialogRef.close();
  }
}
