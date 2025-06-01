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

@NgModule({
  declarations: [
    HomePageComponent,
    SubmitLeaveRequestDialogComponent,
    DateDetailsDialogComponent,
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
  ],
  providers: [],
})
export class HomeModule {}
