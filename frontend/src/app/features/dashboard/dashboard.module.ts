import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardRoutingModule } from './dashboard-routing.module';
import { HomePageComponent } from './pages/home-page/home-page.component';
import { DashboardComponent } from './dashboard.component';
import { LayoutModule } from '../../layout/layout.module';
import { FullCalendarModule } from '@fullcalendar/angular';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [HomePageComponent, DashboardComponent],
  imports: [
    CommonModule,
    DashboardRoutingModule,
    LayoutModule,
    FullCalendarModule,
    MatDialogModule,
    MatButtonModule,
    MatChipsModule,
    FormsModule,
  ],
})
export class DashboardModule {}
