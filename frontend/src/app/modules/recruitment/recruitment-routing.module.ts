import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ApplicationPageComponent } from './pages/application-page/application-page.component';
import { ApplicationDetailsPageComponent } from './pages/application-details-page/application-details-page.component';
import { JobOffersPageComponent } from './pages/job-offers-page/job-offers-page.component';
import { JobOfferFormComponent } from './pages/job-offer-form/job-offer-form.component';
import { JobOfferDetailsComponent } from './pages/job-offer-details/job-offer-details.component';
import { JobApplicationPageComponent } from './pages/job-application-page/job-application-page.component';

const routes: Routes = [
  { path: 'job-offers/:id/applications', component: ApplicationPageComponent },
  {
    path: 'job-offers/:id/applications/:applicationid',
    component: ApplicationDetailsPageComponent,
  },
  { path: 'job-offers', component: JobOffersPageComponent },
  { path: 'job-offers/create', component: JobOfferFormComponent },
  { path: 'job-offers/edit/:id', component: JobOfferFormComponent },
  { path: 'job-offers/:id', component: JobOfferDetailsComponent },
  {
    path: 'job-offers/:id/apply',
    component: JobApplicationPageComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RecruitmentRoutingModule {}
