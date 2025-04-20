import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TrainingsPageComponent } from './pages/trainings-page/trainings-page.component';
import { InvitationsPageComponent } from './pages/invitations-page/invitations-page.component';
import { RoleGuard } from '../../core/guards/role.guard';

const routes: Routes = [
  {
    path: '',
    component: TrainingsPageComponent,
  },
  {
    path: ':id/invitations',
    component: InvitationsPageComponent,
    canActivate: [RoleGuard],
    data: { roles: ['Manager', 'HR'] },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TrainingManagementRoutingModule {}
