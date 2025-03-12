import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { LeaveService } from '../../services/leave.service';

@Component({
  selector: 'app-leave-request',
  standalone: false,
  templateUrl: './leave-request.component.html',
  styleUrls: ['./leave-request.component.css'],
})
export class LeaveRequestComponent {
  public leaveRequestForm!: FormGroup;
  private leaveService = inject(LeaveService);
  dialogRef = inject(MatDialogRef);

  requestTypes = [
    'ANNUEL',
    'MALADIE',
    'MATERNITÉ',
    'PATERNITÉ',
    'SANS_SOLDE',
    'DÉCÈS',
    'TÉLÉTRAVAIL',
    'AUTORISATION',
  ];

  requestTypesRequireAttachment = [
    'MALADIE',
    'MATERNITÉ',
    'PATERNITÉ',
    'DÉCÈS',
  ];

  ngOnInit(): void {
    this.leaveRequestForm = new FormGroup({
      leaveType: new FormControl('', Validators.required),
      startDate: new FormControl('', Validators.required),
      endDate: new FormControl('', Validators.required),
    });

    this.leaveRequestForm.get('leaveType')?.valueChanges.subscribe({
      next: (leaveType) => this.onLeaveTypeChange(leaveType),
    });
  }

  onLeaveTypeChange(leaveType: string) {
    if (leaveType === 'AUTORISATION') {
      this.leaveRequestForm.addControl(
        'startTime',
        new FormControl('', Validators.required)
      );
      this.leaveRequestForm.addControl(
        'endTime',
        new FormControl('', Validators.required)
      );
    } else if (this.requestTypesRequireAttachment.includes(leaveType)) {
      this.leaveRequestForm.addControl(
        'attachment',
        new FormControl('', Validators.required)
      );
    } else {
      this.leaveRequestForm.removeControl('startTime');
      this.leaveRequestForm.removeControl('endTime');
      this.leaveRequestForm.removeControl('attachment');
    }
  }

  onSubmit() {
    if (this.leaveRequestForm.valid) {
      this.leaveService.applyLeave(this.leaveRequestForm.value).subscribe({
        next: (response: { message: string }) => {
          console.log(response.message);
          this.dialogRef.close();
        },
        error: (error) => {
          console.error('There was an error!', error.message);
        },
      });
    } else {
      console.log('Form is invalid');
    }
  }
}
