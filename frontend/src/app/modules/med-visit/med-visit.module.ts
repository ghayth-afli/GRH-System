import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MedVisitRoutingModule } from './med-visit-routing.module';
import { MedicalVisitsPageComponent } from './pages/medical-visits-page/medical-visits-page.component';
import { AppointmentsPageComponent } from './pages/appointments-page/appointments-page.component';


@NgModule({
  declarations: [
    MedicalVisitsPageComponent,
    AppointmentsPageComponent
  ],
  imports: [
    CommonModule,
    MedVisitRoutingModule
  ]
})
export class MedVisitModule { }
