import { Component, inject, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { LeaveService } from '../../../leave/services/leave.service';

@Component({
  selector: 'app-submit-leave-request-dialog',
  standalone: false,
  templateUrl: './submit-leave-request-dialog.component.html',
  styleUrl: './submit-leave-request-dialog.component.css',
})
export class SubmitLeaveRequestDialogComponent implements OnInit {
  public leaveRequestForm!: FormGroup;
  private leaveService = inject(LeaveService);
  dialogRef = inject(MatDialogRef<SubmitLeaveRequestDialogComponent>);
  isLoading = false;
  minDate: string;
  selectedFile: File | null = null;

  constructor() {
    const today = new Date();
    this.minDate = today.toISOString().split('T')[0];
  }

  leaveTypes = [
    { value: 'ANNUEL', label: 'Vacation' },
    { value: 'MALADIE', label: 'Sick' },
    { value: 'MATERNITÉ', label: 'Maternity' },
    { value: 'PATERNITÉ', label: 'Paternity' },
    { value: 'DÉCÈS', label: 'Bereavement' },
    { value: 'SANS_SOLDE', label: 'Unpaid Leave' },
    { value: 'TÉLÉTRAVAIL', label: 'Remote Work' },
    { value: 'AUTORISATION', label: 'Authorization' },
  ];

  requestTypesRequireAttachment = [
    'MALADIE',
    'MATERNITÉ',
    'PATERNITÉ',
    'DÉCÈS',
  ];

  ngOnInit(): void {
    this.leaveRequestForm = new FormGroup(
      {
        leaveType: new FormControl('', Validators.required),
        startDate: new FormControl('', Validators.required),
        endDate: new FormControl('', Validators.required),
      },
      [this.dateValidator]
    );

    this.leaveRequestForm.get('leaveType')?.valueChanges.subscribe({
      next: (leaveType) => this.onLeaveTypeChange(leaveType),
    });
  }

  // Custom validator for date range
  dateValidator = (control: AbstractControl) => {
    const form = control as FormGroup;
    const startDate = form.get('startDate')?.value;
    const endDate = form.get('endDate')?.value;

    if (startDate && endDate && new Date(endDate) < new Date(startDate)) {
      form.get('endDate')?.setErrors({ dateRange: true });
      return { dateRange: true };
    }

    // Clear the error if dates are valid
    const endDateControl = form.get('endDate');
    if (endDateControl?.errors?.['dateRange']) {
      delete endDateControl.errors['dateRange'];
      if (Object.keys(endDateControl.errors).length === 0) {
        endDateControl.setErrors(null);
      }
    }

    return null;
  };

  // Custom validator for time range
  timeValidator = (control: AbstractControl) => {
    const form = control as FormGroup;
    const startTime = form.get('startTime')?.value;
    const endTime = form.get('endTime')?.value;

    if (startTime && endTime && startTime >= endTime) {
      form.get('endTime')?.setErrors({ timeRange: true });
      return { timeRange: true };
    }

    // Clear the error if times are valid
    const endTimeControl = form.get('endTime');
    if (endTimeControl?.errors?.['timeRange']) {
      delete endTimeControl.errors['timeRange'];
      if (Object.keys(endTimeControl.errors).length === 0) {
        endTimeControl.setErrors(null);
      }
    }

    return null;
  };

  onLeaveTypeChange(leaveType: string) {
    // Remove all dynamic controls first
    this.leaveRequestForm.removeControl('startTime');
    this.leaveRequestForm.removeControl('endTime');
    this.leaveRequestForm.removeControl('attachment');

    // Clear selected file
    this.selectedFile = null;

    if (leaveType === 'AUTORISATION') {
      // Add time controls for authorization
      this.leaveRequestForm.addControl(
        'startTime',
        new FormControl('', Validators.required)
      );
      this.leaveRequestForm.addControl(
        'endTime',
        new FormControl('', Validators.required)
      );

      // Add time validator
      this.leaveRequestForm.setValidators([
        this.dateValidator,
        this.timeValidator,
      ]);
    } else if (this.requestTypesRequireAttachment.includes(leaveType)) {
      // Add attachment control for types that require it
      this.leaveRequestForm.addControl(
        'attachment',
        new FormControl(null, Validators.required)
      );

      // Reset to date validator only
      this.leaveRequestForm.setValidators([this.dateValidator]);
    } else {
      // Reset to date validator only
      this.leaveRequestForm.setValidators([this.dateValidator]);
    }

    // Update form validity
    this.leaveRequestForm.updateValueAndValidity();
  }

  onAttachmentSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
      this.leaveRequestForm.patchValue({ attachment: this.selectedFile });
      this.leaveRequestForm.get('attachment')?.updateValueAndValidity();
    }
  }

  onSubmit() {
    if (this.leaveRequestForm.invalid) {
      this.leaveRequestForm.markAllAsTouched();
      console.log('Form is invalid');
      return;
    }

    this.isLoading = true;

    // Prepare form data as an object for the service
    const formValue = this.leaveRequestForm.value;
    const submitData = {
      leaveType: formValue.leaveType,
      startDate: formValue.startDate,
      endDate: formValue.endDate,
      startTime: formValue.startTime,
      endTime: formValue.endTime,
      attachment: this.selectedFile ?? undefined,
    };
    console.log('Submitting leave request:', submitData);

    this.leaveService.applyLeave(submitData).subscribe({
      next: (response: { message: string }) => {
        console.log(response.message);
        this.isLoading = false;
        this.dialogRef.close(response);
      },
      error: (error: { message: string }) => {
        this.isLoading = false;
        console.error('There was an error!', error.message);
      },
    });
  }

  onClose() {
    this.dialogRef.close();
  }

  // Helper methods for template
  isAuthorization(): boolean {
    return this.leaveRequestForm.get('leaveType')?.value === 'AUTORISATION';
  }

  isAttachmentRequired(): boolean {
    const leaveType = this.leaveRequestForm.get('leaveType')?.value;
    return this.requestTypesRequireAttachment.includes(leaveType);
  }

  hasTimeControls(): boolean {
    return (
      this.leaveRequestForm.contains('startTime') &&
      this.leaveRequestForm.contains('endTime')
    );
  }

  hasAttachmentControl(): boolean {
    return this.leaveRequestForm.contains('attachment');
  }
}
