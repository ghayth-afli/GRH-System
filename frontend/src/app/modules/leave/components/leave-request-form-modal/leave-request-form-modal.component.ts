import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { LeaveService } from '../../services/leave.service';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-leave-request-form-modal',
  standalone: false,

  templateUrl: './leave-request-form-modal.component.html',
  styleUrl: './leave-request-form-modal.component.css',
})
export class LeaveRequestFormModalComponent {
  public leaveRequestForm!: FormGroup;
  private leaveService = inject(LeaveService);
  dialogRef = inject(MatDialogRef);
  private snackBar = inject(MatSnackBar);
  isLoading = false;
  minDate: string;

  constructor() {
    const today = new Date();
    this.minDate = today.toISOString().split('T')[0];
  }

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

  onAttachmentSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      this.leaveRequestForm.patchValue({ attachment: file });
      this.leaveRequestForm.get('attachment')?.updateValueAndValidity();
    }
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
      this.isLoading = true;
      this.leaveService.applyLeave(this.leaveRequestForm.value).subscribe({
        next: (response: { message: string }) => {
          console.log(response.message);
          this.isLoading = false;
          this.snackBar.open(response.message, 'Close', {
            duration: 5000,
            horizontalPosition: 'end',
            verticalPosition: 'top',
          });

          this.dialogRef.close();
        },
        error: (error: { message: string }) => {
          this.isLoading = false;
          this.snackBar.open(error.message, 'Close', {
            duration: 5000,
            horizontalPosition: 'end',
            verticalPosition: 'top',
          });
          console.error('There was an error!', error.message);
        },
      });
    } else {
      console.log('Form is invalid');
    }
  }
}
