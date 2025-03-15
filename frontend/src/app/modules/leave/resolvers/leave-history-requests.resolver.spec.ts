import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { leaveHistoryRequestsResolver } from './leave-history-requests.resolver';

describe('leaveHistoryRequestsResolver', () => {
  const executeResolver: ResolveFn<boolean> = (...resolverParameters) => 
      TestBed.runInInjectionContext(() => leaveHistoryRequestsResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
