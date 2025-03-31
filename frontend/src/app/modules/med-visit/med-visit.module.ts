import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MedVisitRoutingModule } from './med-visit-routing.module';
import { MedicalVisitsPageComponent } from './pages/medical-visits-page/medical-visits-page.component';
import { AppointmentsPageComponent } from './pages/appointments-page/appointments-page.component';
import { MatSortModule } from '@angular/material/sort';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MedicalVisitFormModalComponent } from './components/medical-visit-form-modal/medical-visit-form-modal.component';
import { AppointmentModalComponent } from './components/appointment-modal/appointment-modal.component';

@NgModule({
  declarations: [
    MedicalVisitsPageComponent,
    AppointmentsPageComponent,
    MedicalVisitFormModalComponent,
    AppointmentModalComponent,
  ],
  imports: [
    CommonModule,
    MedVisitRoutingModule,
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
export class MedVisitModule {}
