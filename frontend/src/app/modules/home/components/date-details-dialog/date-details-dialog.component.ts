import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

interface LeaveDetail {
  name: string;
  type: string;
  duration: string;
  status: string;
  color: string;
}

interface HolidayDetail {
  name: string;
  date: string;
  country: string;
  flag: string;
}

interface DateDetailsData {
  date: string;
  leaves: LeaveDetail[];
  holiday: HolidayDetail | null;
  userLeave: LeaveDetail | null;
}

@Component({
  selector: 'app-date-details-dialog',
  standalone: false,
  templateUrl: './date-details-dialog.component.html',
  styleUrls: ['./date-details-dialog.component.css'],
})
export class DateDetailsDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<DateDetailsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DateDetailsData
  ) {}
}
