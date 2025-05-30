import {
  Component,
  Inject,
  ViewChild,
  AfterViewInit,
  inject,
} from '@angular/core';
import { NgForm } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogRef,
} from '@angular/material/dialog';
import { MedicalVisit } from '../../models/medical-visit';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  ConfirmationModalComponent,
  ConfirmationModalData,
} from '../../../../shared/components/confirmation-modal/confirmation-modal.component';
import { MedicalVisitService } from '../../services/medical-visit.service';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';

@Component({
  selector: 'app-create-edit-medical-visit',
  standalone: false,
  templateUrl: './create-edit-medical-visit.component.html',
  styleUrl: './create-edit-medical-visit.component.css',
})
export class CreateEditMedicalVisitComponent {
  @ViewChild('medicalVisitForm') medicalVisitForm!: NgForm;
  today = new Date().toISOString().split('T')[0];
  isLoading = false;
  isEditMode = false;
  visitData = {
    doctorName: '',
    visitDate: '',
    startTime: '',
    endTime: '',
  };

  private dialogRef = inject(MatDialogRef<CreateEditMedicalVisitComponent>);
  private snackBar = inject(MatSnackBar);
  private medicalVisitService = inject(MedicalVisitService);

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: { isEdit: boolean; visit?: MedicalVisit }
  ) {
    if (data.isEdit && data.visit) {
      this.visitData = {
        ...data.visit,
        visitDate: new Date(data.visit.visitDate).toISOString().split('T')[0],
      };
      this.isEditMode = true;
    }
  }

  onSubmit() {
    this.isLoading = true;
    if (this.data.isEdit && this.data.visit) {
      this.medicalVisitService
        .updateMedicalVisit(this.data.visit.id, {
          ...this.visitData,
          id: this.data.visit.id,
          numberOfAppointments: this.data.visit.numberOfAppointments,
          visitDate: new Date(this.visitData.visitDate).toISOString(),
        })
        .subscribe({
          next: (response) => {
            this.snackBar.open(response.message, 'Close', {
              duration: 3000,
            });
            this.dialogRef.close(true);
          },
          error: (error) => {
            this.snackBar.open(error.error.message, 'Close', {
              duration: 3000,
            });
          },
        });
    } else {
      this.medicalVisitService
        .createMedicalVisit({
          doctorName: this.visitData.doctorName,
          visitDate: new Date(this.visitData.visitDate),
          startTime: this.visitData.startTime,
          endTime: this.visitData.endTime,
        })
        .subscribe({
          next: (response) => {
            this.launchSnackbar(response.message, 'success');
            this.isLoading = false;
            this.dialogRef.close(true);
          },
          error: (error) => {
            this.launchSnackbar(
              error.error.message || 'Error creating medical visit',
              'error'
            );
            this.isLoading = false;
            this.dialogRef.close;
          },
        });
    }
  }

  onCancel() {
    this.dialogRef.close();
  }

  private launchSnackbar(message: string, type: 'success' | 'error') {
    this.snackBar.openFromComponent(CustomSnackbarComponent, {
      data: {
        message: message,
        type: type,
      },
      duration: 5000,
      panelClass: ['custom-snackbar'],
      horizontalPosition: 'end',
      verticalPosition: 'top',
    });
  }

  private removeError(errors: any, key: string): any {
    if (!errors) return null;
    const { [key]: _, ...rest } = errors;
    return Object.keys(rest).length > 0 ? rest : null;
  }
}
