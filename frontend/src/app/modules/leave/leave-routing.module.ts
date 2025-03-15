import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LeaveRequestsPageComponent } from './pages/leave-requests-page/leave-requests-page.component';
import { LeaveRequestsResolver } from './resolvers/leave-history-requests.resolver';

const routes: Routes = [
  {
    path: '',
    component: LeaveRequestsPageComponent,
    resolve: {
      leaveRequests: LeaveRequestsResolver,
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class LeaveRoutingModule {}
