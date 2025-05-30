import { Component, inject, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { TrainingService } from '../../services/training.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-training-form-page',
  standalone: false,
  templateUrl: './training-form-page.component.html',
  styleUrl: './training-form-page.component.css',
})
export class TrainingFormPageComponent {
  @ViewChild('trainingForm') trainingForm!: NgForm;
  isEditMode = false;
  submitted = false;
  isLoading = false;
  today: string = new Date().toISOString().split('T')[0];
  training = {
    title: '',
    description: '',
    startDate: '',
    endDate: '',
  };

  trainingService = inject(TrainingService);
  snackBar = inject(MatSnackBar);
  route = inject(ActivatedRoute);
  router = inject(Router);

  private loadTrainingSubscription: Subscription | undefined;
  private createTrainingSubscription: Subscription | undefined;
  private updateTrainingSubscription: Subscription | undefined;

  ngOnInit() {
    if (this.route.snapshot.paramMap.get('id')) {
      this.isEditMode = true;
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.loadTraining(Number(id));
      }
    }
  }

  ngOnDestroy() {
    if (this.loadTrainingSubscription) {
      this.loadTrainingSubscription.unsubscribe();
    }
    if (this.createTrainingSubscription) {
      this.createTrainingSubscription.unsubscribe();
    }
    if (this.updateTrainingSubscription) {
      this.updateTrainingSubscription.unsubscribe();
    }
  }

  private loadTraining(id: number) {
    if (this.loadTrainingSubscription) {
      this.loadTrainingSubscription.unsubscribe();
    }
    this.loadTrainingSubscription = this.trainingService
      .getTrainingById(id)
      .subscribe({
        next: (response) => {
          this.training = response;
        },
        error: (error) => {
          this.snackBar.openFromComponent(CustomSnackbarComponent, {
            data: {
              message:
                'Error loading training data: ' +
                (error.error?.message || error.message),
              type: 'error',
            },
            duration: 3000,
            panelClass: ['custom-snackbar'],
            horizontalPosition: 'end',
            verticalPosition: 'top',
          });
          this.router.navigate(['/trainings']);
        },
      });
  }

  onSubmit() {
    if (this.isLoading || !this.trainingForm.valid) return;
    this.isLoading = true;

    if (this.isEditMode) {
      this.updateTraining();
    } else {
      this.createTraining();
    }
  }

  private createTraining() {
    if (this.createTrainingSubscription) {
      this.createTrainingSubscription.unsubscribe();
    }
    this.createTrainingSubscription = this.trainingService
      .createTraining(this.training)
      .subscribe({
        next: (response) => {
          this.snackBar.openFromComponent(CustomSnackbarComponent, {
            data: {
              message: 'Training created successfully.',
              type: 'success',
            },
            duration: 5000,
            panelClass: ['custom-snackbar'],
            horizontalPosition: 'end',
            verticalPosition: 'top',
          });
          this.isLoading = false;
          this.router.navigate(['/trainings']);
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.openFromComponent(CustomSnackbarComponent, {
            data: {
              message:
                'Error creating training: ' +
                (error.error?.message || error.message),
              type: 'error',
            },
            duration: 3000,
            panelClass: ['custom-snackbar'],
            horizontalPosition: 'end',
            verticalPosition: 'top',
          });
        },
      });
  }

  private updateTraining() {
    if (this.updateTrainingSubscription) {
      this.updateTrainingSubscription.unsubscribe();
    }
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.updateTrainingSubscription = this.trainingService
        .updateTraining(Number(id), this.training)
        .subscribe({
          next: (response) => {
            this.isLoading = false;
            this.snackBar.openFromComponent(CustomSnackbarComponent, {
              data: {
                message: 'Training updated successfully.',
                type: 'success',
              },
              duration: 5000,
              panelClass: ['custom-snackbar'],
              horizontalPosition: 'end',
              verticalPosition: 'top',
            });
            this.router.navigate(['/trainings']);
          },
          error: (error) => {
            this.isLoading = false;
            this.snackBar.openFromComponent(CustomSnackbarComponent, {
              data: {
                message:
                  'Error updating training: ' +
                  (error.error?.message || error.message),
                type: 'error',
              },
              duration: 3000,
              panelClass: ['custom-snackbar'],
              horizontalPosition: 'end',
              verticalPosition: 'top',
            });
          },
        });
    } else {
      this.isLoading = false;
    }
  }

  onCancel() {
    this.trainingForm.resetForm();
    this.submitted = false;
    this.router.navigate(['/trainings']);
  }
}
