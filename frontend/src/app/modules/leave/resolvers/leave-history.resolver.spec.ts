import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { leaveHistoryResolver } from './leave-history.resolver';

describe('leaveHistoryResolver', () => {
  const executeResolver: ResolveFn<boolean> = (...resolverParameters) => 
      TestBed.runInInjectionContext(() => leaveHistoryResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
