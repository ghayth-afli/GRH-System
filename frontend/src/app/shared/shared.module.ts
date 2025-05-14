import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CalendarComponent } from './components/calendar/calendar.component';
import { FullCalendarModule } from '@fullcalendar/angular';
import { EditPersonalInfoModalFormComponent } from './components/edit-personal-info-modal-form/edit-personal-info-modal-form.component';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TimeAgoPipe } from './pipes/time-ago.pipe';
import { CustomSnackbarComponent } from './components/custom-snackbar/custom-snackbar.component';

@NgModule({
  declarations: [
    CalendarComponent,
    EditPersonalInfoModalFormComponent,
    TimeAgoPipe,
    CustomSnackbarComponent,
  ],
  imports: [
    CommonModule,
    FullCalendarModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    FormsModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
  ],
  exports: [CalendarComponent, EditPersonalInfoModalFormComponent, TimeAgoPipe],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class SharedModule {}
