import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { Training } from '../../models/training';
import { ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';
import { TrainingService } from '../../services/training.service';
import { AuthService } from '../../../../core/services/auth.service';
import { InvitationService } from '../../services/invitation.service';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationModalComponent } from '../../../../shared/components/confirmation-modal/confirmation-modal.component';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-training-details-page',
  standalone: false,
  templateUrl: './training-details-page.component.html',
  styleUrl: './training-details-page.component.css',
})
export class TrainingDetailsPageComponent {
  training: Training | null = null;

  // Inject services
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private trainingService = inject(TrainingService);
  private invitationService = inject(InvitationService);
  authService = inject(AuthService);

  // To hold subscriptions
  private trainingSubscription: Subscription | undefined;
  private dialogCloseSubscription: Subscription | undefined;
  private confirmInvitationSubscription: Subscription | undefined;

  ngOnInit() {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.loadTraining(+id);
    } else {
      this.snackBar.openFromComponent(CustomSnackbarComponent, {
        data: { message: 'Training ID is missing', type: 'error' },
        duration: 3000,
      });
    }
  }

  ngOnDestroy() {
    if (this.trainingSubscription) {
      this.trainingSubscription.unsubscribe();
    }
    if (this.dialogCloseSubscription) {
      this.dialogCloseSubscription.unsubscribe();
    }
    if (this.confirmInvitationSubscription) {
      this.confirmInvitationSubscription.unsubscribe();
    }
  }

  onConfirmTraining(id: number) {
    if (this.dialogCloseSubscription) {
      this.dialogCloseSubscription.unsubscribe();
    }
    if (this.confirmInvitationSubscription) {
      this.confirmInvitationSubscription.unsubscribe();
    }

    this.dialogCloseSubscription = this.dialog
      .open(ConfirmationModalComponent, {
        data: {
          title: 'Confirm Training',
          message: 'Are you sure you want to confirm this training?',
          confirmButtonText: 'Confirm',
          cancelButtonText: 'Cancel',
        },
      })
      .afterClosed()
      .subscribe((confirmed) => {
        if (confirmed) {
          this.confirmInvitationSubscription = this.invitationService
            .confirmInvitation(id)
            .subscribe({
              next: () => {
                this.launchSnackbar(
                  'Training confirmed successfully',
                  'success'
                );
                if (this.training) {
                  this.training.isConfirmed = true;
                }
              },
              error: (error) => {
                this.launchSnackbar(
                  'Failed to confirm training: ' +
                    (error.error?.message || error.message),
                  'error'
                );
                console.error('Failed to confirm training:', error);
              },
            });
        }
      });
  }

  private loadTraining(id: number) {
    if (this.trainingSubscription) {
      this.trainingSubscription.unsubscribe();
    }
    this.trainingSubscription = this.trainingService
      .getTrainingById(id)
      .subscribe({
        next: (training) => {
          this.training = training;
        },
        error: (error) => {
          this.launchSnackbar(
            'Failed to load training details: ' +
              (error.error?.message || error.message),
            'error'
          );
          console.error('Failed to load training details:', error);
        },
      });
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
}
