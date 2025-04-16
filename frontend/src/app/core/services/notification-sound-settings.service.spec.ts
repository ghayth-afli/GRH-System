import { TestBed } from '@angular/core/testing';

import { NotificationSoundSettingsService } from './notification-sound-settings.service';

describe('NotificationSoundSettingsService', () => {
  let service: NotificationSoundSettingsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NotificationSoundSettingsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
