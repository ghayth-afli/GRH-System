import { Component, Inject, inject, OnDestroy } from '@angular/core';
import { TrainingService } from '../../services/training.service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormControl, FormGroup } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Training } from '../../models/training';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-training-form-modal',
  standalone: false,

  templateUrl: './training-form-modal.component.html',
  styleUrl: './training-form-modal.component.css',
})
export class TrainingFormModalComponent {
  private trainingService = inject(TrainingService);
  dialogRef = inject(MatDialogRef);
  trainingForm!: FormGroup;
  isLoading = false;
  isUpdate = false;
  private snackBar = inject(MatSnackBar);
  private subscriptions: Subscription = new Subscription();

  constructor(@Inject(MAT_DIALOG_DATA) public data: Training) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private initializeForm(): void {
    if (this.data) {
      this.setupUpdateForm();
    } else {
      this.setupCreateForm();
    }
  }

  private setupUpdateForm(): void {
    this.trainingForm = new FormGroup({
      title: new FormControl(this.data.title),
      description: new FormControl(this.data.description),
      startDate: new FormControl(this.data.startDate),
      endDate: new FormControl(this.data.endDate),
    });
    this.isUpdate = true;
  }

  private setupCreateForm(): void {
    this.trainingForm = new FormGroup({
      title: new FormControl(''),
      description: new FormControl(''),
      startDate: new FormControl(''),
      endDate: new FormControl(''),
    });
  }

  onSubmit(): void {
    this.isUpdate ? this.updateTraining() : this.createTraining();
  }

  private updateTraining(): void {
    this.isLoading = true;
    const trainingData = this.trainingForm.value;
    console.log('training Data:', trainingData);
    const updateSub = this.trainingService
      .updateTraining(this.data.id, trainingData)
      .subscribe({
        next: (response) => this.handleUpdateSuccess(response, trainingData),
        error: (error) => this.handleError('updating', error),
      });
    this.subscriptions.add(updateSub);
  }

  private createTraining(): void {
    if (this.trainingForm.valid) {
      this.isLoading = true;
      const trainingData = this.trainingForm.value;
      console.log('training Data:', trainingData);
      const createSub = this.trainingService
        .createTraining(trainingData)
        .subscribe({
          next: (response) => this.handleCreateSuccess(response, trainingData),
          error: (error) => this.handleError('creating', error),
        });
      this.subscriptions.add(createSub);
    } else {
      console.error('Form is invalid');
    }
  }

  private handleUpdateSuccess(
    response: { message: string },
    trainingData: any
  ): void {
    console.log('training updated:', response);
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
    trainingData: any
  ): void {
    console.log('training created:', response);
    this.snackBar.open(response.message, 'X', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
    });
    this.isLoading = false;
    this.dialogRef.close('submitted');
  }

  private handleError(action: string, error: { message: string }): void {
    console.error(`Error ${action} training:`, error);
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
