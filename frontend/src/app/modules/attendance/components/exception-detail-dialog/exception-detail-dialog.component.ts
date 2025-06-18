import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Exception } from '../../models/exception';
import { AttendanceRecord } from '../../models/attendance-record';
import { AttendanceService } from '../../service/attendance.service';

@Component({
  selector: 'app-exception-detail-dialog',
  standalone: false,
  templateUrl: './exception-detail-dialog.component.html',
  styleUrl: './exception-detail-dialog.component.css',
})
export class ExceptionDetailDialogComponent {
  punchForm: FormGroup;
  record: AttendanceRecord | null = null;

  constructor(
    @Inject('DIALOG_DATA') public data: Exception,
    @Inject('AttendanceService') private attendanceService: AttendanceService,
    @Inject('FormBuilder') private fb: FormBuilder
  ) {
    this.punchForm = this.fb.group({
      punchTime: [
        '',
        [Validators.required, Validators.pattern(/^[0-2]?[0-9]:[0-5][0-9]$/)],
      ],
    });
    this.attendanceService
      .getAttendanceRecords(this.data.date)
      .subscribe((records: AttendanceRecord[]) => {
        this.record =
          records.find(
            (r: AttendanceRecord) => r.employeeName === this.data.employee
          ) || null;
      });
  }

  addPunch() {
    // if (this.record && this.punchForm.valid) {
    //   const punchTime = this.punchForm.get('punchTime')?.value;
    //   this.attendanceService
    //     .addManualPunch(this.record.id, punchTime)
    //     .subscribe((record) => {
    //       this.record = { ...record };
    //       this.punchForm.reset();
    //     });
    // }
  }
}
