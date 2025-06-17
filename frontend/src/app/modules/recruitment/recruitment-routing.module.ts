import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ApplicationPageComponent } from './pages/application-page/application-page.component';
import { ApplicationDetailsPageComponent } from './pages/application-details-page/application-details-page.component';
import { JobOffersPageComponent } from './pages/job-offers-page/job-offers-page.component';
import { JobOfferFormComponent } from './pages/job-offer-form/job-offer-form.component';
import { JobOfferDetailsComponent } from './pages/job-offer-details/job-offer-details.component';
import { JobApplicationPageComponent } from './pages/job-application-page/job-application-page.component';
import { RoleGuard } from '../../core/guards/role.guard';

const routes: Routes = [
  {
    path: 'job-offers/:id/applications',
    component: ApplicationPageComponent,
    canActivate: [RoleGuard],
    data: { roles: ['HR', 'HRD'] },
  },
  {
    path: 'job-offers/:id/applications/:applicationid',
    component: ApplicationDetailsPageComponent,
    canActivate: [RoleGuard],
    data: { roles: ['HR', 'HRD'] },
  },
  {
    path: 'job-offers',
    component: JobOffersPageComponent,
    canActivate: [RoleGuard],
    data: { roles: ['Manager', 'HR', 'Employee', 'HRD'] },
  },
  {
    path: 'job-offers/create',
    component: JobOfferFormComponent,
    canActivate: [RoleGuard],
    data: { roles: ['HR', 'HRD'] },
  },
  {
    path: 'job-offers/edit/:id',
    component: JobOfferFormComponent,
    canActivate: [RoleGuard],
    data: { roles: ['HR', 'HRD'] },
  },
  {
    path: 'job-offers/:id',
    component: JobOfferDetailsComponent,
    canActivate: [RoleGuard],
    data: { roles: ['Manager', 'HR', 'Employee', 'HRD'] },
  },
  {
    path: 'job-offers/:id/apply',
    component: JobApplicationPageComponent,
    canActivate: [RoleGuard],
    data: { roles: ['Manager', 'Employee'] },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RecruitmentRoutingModule {}
