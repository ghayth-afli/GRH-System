import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';
import { RoleGuard } from './role.guard';

describe('roleGuard', () => {
  let roleGuard: RoleGuard;
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() =>
      roleGuard.canActivate(...guardParameters)
    );
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [RoleGuard],
    });
    roleGuard = TestBed.inject(RoleGuard);
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
