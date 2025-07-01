import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AttendanceDashboardPageComponent } from './pages/attendance-dashboard-page/attendance-dashboard-page.component';

const routes: Routes = [
  { path: '', component: AttendanceDashboardPageComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AttendanceRoutingModule {}
