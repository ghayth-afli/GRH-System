import { Component, Inject, inject } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { AppointmentService } from '../../services/appointment.service';
import { MedicalVisitService } from '../../services/medical-visit.service';
import { MedicalVisit } from '../../models/medical-visit';

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

  constructor(@Inject(MAT_DIALOG_DATA) public data: MedicalVisit) {}

  ngOnInit(): void {
    if (this.data) {
      this.medicalVisitForm = new FormGroup({
        doctorName: new FormControl(this.data.doctorName),
        visitDate: new FormControl(this.data.visitDate),
        startTime: new FormControl(this.data.startTime),
        endTime: new FormControl(this.data.endTime),
      });
      this.isUpdate = true;
    } else {
      this.medicalVisitForm = new FormGroup({
        doctorName: new FormControl(''),
        visitDate: new FormControl(''),
        startTime: new FormControl(''),
        endTime: new FormControl(''),
      });
    }
  }

  onSubmit() {
    if (this.isUpdate) {
      this.updateMedicalVisit();
    } else {
      this.createMedicalVisit();
    }
  }

  updateMedicalVisit() {
    this.isLoading = true;
    const medicalVisitData = this.medicalVisitForm.value;
    console.log('Medical Visit Data:', medicalVisitData);
    // Call the service to save the data here
    this.medicalVisitService
      .updateMedicalVisit(this.data.id, medicalVisitData)
      .subscribe({
        next: (response) => {
          console.log('Medical visit updated:', response);
          this.isLoading = false;
          this.dialogRef.close(medicalVisitData);
        },
        error: (error) => {
          console.error('Error updating medical visit:', error);
          this.isLoading = false;
        },
      });
  }

  createMedicalVisit() {
    if (this.medicalVisitForm.valid) {
      this.isLoading = true;
      const medicalVisitData = this.medicalVisitForm.value;
      console.log('Medical Visit Data:', medicalVisitData);
      this.medicalVisitService.createMedicalVisit(medicalVisitData).subscribe({
        next: (response) => {
          console.log('Medical visit created:', response);
          this.isLoading = false;
          this.dialogRef.close(medicalVisitData);
        },
        error: (error) => {
          console.error('Error creating medical visit:', error);
          this.isLoading = false;
        },
      });
    } else {
      console.error('Form is invalid');
    }
  }
  onCancel() {
    this.dialogRef.close();
  }
  onClose() {
    this.dialogRef.close();
  }
}
