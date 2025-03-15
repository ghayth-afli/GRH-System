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

@NgModule({
  declarations: [LeaveRequestsPageComponent, LeaveRequestsDataTableComponent],
  imports: [
    CommonModule,
    LeaveRoutingModule,
    SharedModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatCardModule,
    MatSnackBarModule,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class LeaveModule {}
