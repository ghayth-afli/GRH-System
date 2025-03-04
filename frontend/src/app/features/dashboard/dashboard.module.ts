import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardRoutingModule } from './dashboard-routing.module';
import { HomePageComponent } from './pages/home-page/home-page.component';
import { DashboardComponent } from './dashboard.component';
import { LayoutModule } from '../../layout/layout.module';

@NgModule({
  declarations: [HomePageComponent, DashboardComponent],
  imports: [CommonModule, DashboardRoutingModule, LayoutModule],
})
export class DashboardModule {}
