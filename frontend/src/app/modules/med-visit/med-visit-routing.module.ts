import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MedicalVisitsPageComponent } from './pages/medical-visits-page/medical-visits-page.component';
import { AppointmentsPageComponent } from './pages/appointments-page/appointments-page.component';

const routes: Routes = [
  {
    path: '',
    component: MedicalVisitsPageComponent,
  },
  {
    path: ':id/appointments',
    component: AppointmentsPageComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class MedVisitRoutingModule {}
