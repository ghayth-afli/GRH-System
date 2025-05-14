import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { LeaveRoutingModule } from './leave-routing.module';
import { LeaveRequestsPageComponent } from './pages/leave-requests-page/leave-requests-page.component';
import { SharedModule } from '../../shared/shared.module';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { LeaveRequestsDataTableComponent } from './components/leave-requests-data-table/leave-requests-data-table.component';
import { MatDialogModule } from '@angular/material/dialog';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { LeaveRequestFormModalComponent } from './components/leave-request-form-modal/leave-request-form-modal.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@NgModule({
  declarations: [
    LeaveRequestsPageComponent,
    LeaveRequestsDataTableComponent,
    LeaveRequestFormModalComponent,
  ],
  imports: [
    CommonModule,
    LeaveRoutingModule,
    SharedModule,
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
  exports: [LeaveRequestFormModalComponent],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class LeaveModule {}
