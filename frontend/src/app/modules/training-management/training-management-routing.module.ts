import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TrainingsPageComponent } from './pages/trainings-page/trainings-page.component';
import { InvitationsPageComponent } from './pages/invitations-page/invitations-page.component';
import { RoleGuard } from '../../core/guards/role.guard';
import { TrainingFormPageComponent } from './pages/training-form-page/training-form-page.component';
import { TrainingDetailsPageComponent } from './pages/training-details-page/training-details-page.component';

const routes: Routes = [
  {
    path: '',
    component: TrainingsPageComponent,
    canActivate: [RoleGuard],
    data: { roles: ['Manager', 'HR', 'Employee'] },
  },
  {
    path: 'create',
    component: TrainingFormPageComponent,
    canActivate: [RoleGuard],
    data: { roles: ['Manager'] },
  },
  {
    path: ':id/edit',
    component: TrainingFormPageComponent,
    canActivate: [RoleGuard],
    data: { roles: ['Manager'] },
  },
  {
    path: ':id/details',
    component: TrainingDetailsPageComponent,
    canActivate: [RoleGuard],
    data: { roles: ['Manager', 'HR', 'Employee'] },
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
