import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { OnboardingPageRoutingModule } from './onboarding-page-routing.module';
import { OnboardingPageComponent } from './pages/onboarding-page/onboarding-page.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [OnboardingPageComponent],
  imports: [
    CommonModule,
    OnboardingPageRoutingModule,
    FormsModule,
    ReactiveFormsModule,
  ],
})
export class OnboardingPageModule {}
