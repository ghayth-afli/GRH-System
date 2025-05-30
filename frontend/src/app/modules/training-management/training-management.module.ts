import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TrainingManagementRoutingModule } from './training-management-routing.module';
import { InvitationsPageComponent } from './pages/invitations-page/invitations-page.component';
import { TrainingsPageComponent } from './pages/trainings-page/trainings-page.component';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TrainingFormPageComponent } from './pages/training-form-page/training-form-page.component';
import { TrainingDetailsPageComponent } from './pages/training-details-page/training-details-page.component';

@NgModule({
  declarations: [
    InvitationsPageComponent,
    TrainingsPageComponent,
    TrainingFormPageComponent,
    TrainingDetailsPageComponent,
  ],
  imports: [
    CommonModule,
    TrainingManagementRoutingModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    FormsModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class TrainingManagementModule {}
