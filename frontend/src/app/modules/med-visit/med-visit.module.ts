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
import { ToastrModule } from 'ngx-toastr';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { CreateEditMedicalVisitComponent } from './components/create-edit-medical-visit/create-edit-medical-visit.component';
import { TakeRegistrationDialogComponent } from './components/take-registration-dialog/take-registration-dialog.component';

@NgModule({
  declarations: [
    MedicalVisitsPageComponent,
    AppointmentsPageComponent,
    CreateEditMedicalVisitComponent,
    TakeRegistrationDialogComponent,
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
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class MedVisitModule {}
