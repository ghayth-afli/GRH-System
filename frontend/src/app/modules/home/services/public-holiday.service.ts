import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { map } from 'rxjs';
import { HolidayTranslationsServiceService } from './holiday-translations-service.service';

@Injectable({
  providedIn: 'root',
})
export class PublicHolidayService {
  http = inject(HttpClient);
  currentYear = new Date().getFullYear();
  translationService = inject(HolidayTranslationsServiceService);
  getPublicHolidays() {
    const currentYear = new Date().getFullYear();
    return this.http
      .get(
        `${environment.calendarificApiUrl}?api_key=${environment.calendarificApiKey}&country=TN&year=${currentYear}`
      )
      .pipe(
        map((response: any) => {
          return response.response.holidays.map((holiday: any) => ({
            name: this.translationService.translateHolidayName(holiday.name),
            date: holiday.date.iso,
            canonical_url: holiday.canonical_url,
          }));
        })
      );
  }
}
