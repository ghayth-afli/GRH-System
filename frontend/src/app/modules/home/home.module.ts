import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { HomeRoutingModule } from './home-routing.module';
import { HomePageComponent } from './pages/home-page/home-page.component';

import { LeaveModule } from '../leave/leave.module';
import { SharedModule } from '../../shared/shared.module';
import { SubmitLeaveRequestDialogComponent } from './components/submit-leave-request-dialog/submit-leave-request-dialog.component';
import { DateDetailsDialogComponent } from './components/date-details-dialog/date-details-dialog.component';
import { BaseChartDirective } from 'ng2-charts';
import { FullCalendarModule } from '@fullcalendar/angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { LeaveRequestsDialogComponent } from './components/leave-requests-dialog/leave-requests-dialog.component';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import { MatPaginatorModule } from '@angular/material/paginator';
import { CustomSnackbarComponent } from '../../shared/components/custom-snackbar/custom-snackbar.component';

@NgModule({
  declarations: [
    HomePageComponent,
    SubmitLeaveRequestDialogComponent,
    DateDetailsDialogComponent,
    LeaveRequestsDialogComponent,
  ],
  imports: [
    CommonModule,
    HomeRoutingModule,
    LeaveModule,
    SharedModule,
    BaseChartDirective,
    FormsModule,
    FullCalendarModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatSnackBarModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
  ],
  providers: [],
})
export class HomeModule {}
