import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AttendanceRoutingModule } from './attendance-routing.module';
import { AttendanceDashboardPageComponent } from './pages/attendance-dashboard-page/attendance-dashboard-page.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { AttendanceDetailDialogComponent } from './components/attendance-detail-dialog/attendance-detail-dialog.component';
import { ExceptionDetailDialogComponent } from './components/exception-detail-dialog/exception-detail-dialog.component';

@NgModule({
  declarations: [
    AttendanceDashboardPageComponent,
    AttendanceDetailDialogComponent,
    ExceptionDetailDialogComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    AttendanceRoutingModule,
  ],
})
export class AttendanceModule {}
