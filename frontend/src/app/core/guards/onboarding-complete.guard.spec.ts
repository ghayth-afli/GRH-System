import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { onboardingCompleteGuard } from './onboarding-complete.guard';

describe('onboardingCompleteGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => onboardingCompleteGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
