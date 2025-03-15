import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { HomeRoutingModule } from './home-routing.module';
import { CalendarSideBarComponent } from './components/calendar-side-bar/calendar-side-bar.component';
import { HomePageComponent } from './pages/home-page/home-page.component';

import { LeaveModule } from '../leave/leave.module';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [CalendarSideBarComponent, HomePageComponent],
  imports: [CommonModule, HomeRoutingModule, LeaveModule, SharedModule],
})
export class HomeModule {}
