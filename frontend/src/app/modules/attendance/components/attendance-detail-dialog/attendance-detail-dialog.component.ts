import { Component, inject, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AttendanceRecord } from '../../models/attendance-record';
import { AttendanceService } from '../../service/attendance.service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
@Component({
  selector: 'app-attendance-detail-dialog',
  standalone: false,
  templateUrl: './attendance-detail-dialog.component.html',
  styleUrl: './attendance-detail-dialog.component.css',
})
export class AttendanceDetailDialogComponent {
  punchForm: FormGroup;
  punches: { time: string; type: string }[] = [];
  dialogRef = inject(MatDialogRef);

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: AttendanceRecord,
    private fb: FormBuilder,
    private attendanceService: AttendanceService
  ) {
    this.punchForm = this.fb.group({
      punchTime: [
        '',
        [
          Validators.required,
          Validators.pattern(/^([0-1][0-9]|2[0-3]):[0-5][0-9]$/),
        ],
      ],
    });
    // Exclude the first and last punches from allPunches
    const allPunches = data.allPunches || [];
    const middlePunches = allPunches.slice(1, allPunches.length - 1);

    this.punches = [
      ...(data.firstPunch ? [{ time: data.firstPunch, type: 'In' }] : []),
      ...middlePunches.map((p) => ({
        time: p,
        type: 'IN',
      })),
      ...(data.lastPunch ? [{ time: data.lastPunch, type: 'Out' }] : []),
    ];
  }

  addPunch() {}
  close() {
    // Logic to close the dialog
    // This could be a method to close the dialog in your dialog service or component
    console.log('Dialog closed');
    this.dialogRef.close();
  }
}
