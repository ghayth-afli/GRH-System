import { Component, Inject, inject } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { AppointmentService } from '../../services/appointment.service';
import { MedicalVisitService } from '../../services/medical-visit.service';
import { MedicalVisit } from '../../models/medical-visit';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-medical-visit-form-modal',
  standalone: false,
  templateUrl: './medical-visit-form-modal.component.html',
  styleUrl: './medical-visit-form-modal.component.css',
})
export class MedicalVisitFormModalComponent {
  dialogRef = inject(MatDialogRef);
  medicalVisitForm!: FormGroup;
  isLoading = false;
  medicalVisitService = inject(MedicalVisitService);
  isUpdate = false;
  private snackBar = inject(MatSnackBar);

  constructor(@Inject(MAT_DIALOG_DATA) public data: MedicalVisit) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  private initializeForm(): void {
    if (this.data) {
      this.setupUpdateForm();
    } else {
      this.setupCreateForm();
    }
  }

  private setupUpdateForm(): void {
    this.medicalVisitForm = new FormGroup({
      doctorName: new FormControl(this.data.doctorName),
      visitDate: new FormControl(this.data.visitDate),
      startTime: new FormControl(this.data.startTime),
      endTime: new FormControl(this.data.endTime),
    });
    this.isUpdate = true;
  }

  private setupCreateForm(): void {
    this.medicalVisitForm = new FormGroup({
      doctorName: new FormControl(''),
      visitDate: new FormControl(''),
      startTime: new FormControl(''),
      endTime: new FormControl(''),
    });
  }

  onSubmit(): void {
    this.isUpdate ? this.updateMedicalVisit() : this.createMedicalVisit();
  }

  private updateMedicalVisit(): void {
    this.isLoading = true;
    const medicalVisitData = this.medicalVisitForm.value;
    console.log('Medical Visit Data:', medicalVisitData);
    this.medicalVisitService
      .updateMedicalVisit(this.data.id, medicalVisitData)
      .subscribe({
        next: (response: { message: string }) => {
          this.handleUpdateSuccess(response, medicalVisitData);
        },

        error: (error) => {
          this.handleError('updating', error);
        },
      });
  }

  private createMedicalVisit(): void {
    if (this.medicalVisitForm.valid) {
      this.isLoading = true;
      const medicalVisitData = this.medicalVisitForm.value;
      console.log('Medical Visit Data:', medicalVisitData);
      this.medicalVisitService.createMedicalVisit(medicalVisitData).subscribe({
        next: (response) =>
          this.handleCreateSuccess(response, medicalVisitData),
        error: (error) => this.handleError('creating', error),
      });
    } else {
      console.error('Form is invalid');
    }
  }

  private handleUpdateSuccess(
    response: { message: string },
    medicalVisitData: any
  ): void {
    console.log('Medical visit updated:', response);
    this.snackBar.open(response.message, 'X', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
    });
    this.isLoading = false;
    this.dialogRef.close('submitted');
  }

  private handleCreateSuccess(
    response: { message: string },
    medicalVisitData: any
  ): void {
    console.log('Medical visit created:', response);
    this.snackBar.open(response.message, 'X', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
    });
    this.isLoading = false;
    this.dialogRef.close('submitted');
  }

  private handleError(action: string, error: { message: string }): void {
    console.error(`Error ${action} medical visit:`, error);
    this.snackBar.open(error.message, 'X', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
    });
    this.isLoading = false;
  }

  onCancel(): void {
    this.closeDialog();
  }

  onClose(): void {
    this.closeDialog();
  }

  private closeDialog(): void {
    this.dialogRef.close();
  }
}
