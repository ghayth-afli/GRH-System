import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { RecruitmentRoutingModule } from './recruitment-routing.module';
import { ApplicationPageComponent } from './pages/application-page/application-page.component';
import { ApplicationDetailsPageComponent } from './pages/application-details-page/application-details-page.component';
import { FormsModule } from '@angular/forms';
import { JobOffersPageComponent } from './pages/job-offers-page/job-offers-page.component';
import { JobOfferFormComponent } from './pages/job-offer-form/job-offer-form.component';
import { JobOfferDetailsComponent } from './pages/job-offer-details/job-offer-details.component';
import { JobApplicationPageComponent } from './pages/job-application-page/job-application-page.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
@NgModule({
  declarations: [
    ApplicationPageComponent,
    ApplicationDetailsPageComponent,
    JobOffersPageComponent,
    JobOfferFormComponent,
    JobOfferDetailsComponent,
    JobApplicationPageComponent,
  ],
  imports: [
    CommonModule,
    RecruitmentRoutingModule,
    FormsModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTabsModule,
  ],
})
export class RecruitmentModule {}
