import { TestBed } from '@angular/core/testing';

import { HolidayTranslationsServiceService } from './holiday-translations-service.service';

describe('HolidayTranslationsServiceService', () => {
  let service: HolidayTranslationsServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HolidayTranslationsServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
