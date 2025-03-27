import { TestBed } from '@angular/core/testing';

import { MedicalVisitService } from './medical-visit.service';

describe('MedicalVisitService', () => {
  let service: MedicalVisitService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MedicalVisitService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
