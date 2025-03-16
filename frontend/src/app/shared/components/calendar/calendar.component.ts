import { Component, inject, Input } from '@angular/core';
import { CalendarOptions, EventInput } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { AuthService } from '../../../core/services/auth.service';
import { LeaveService } from '../../../modules/leave/services/leave.service';
import { Leave } from '../../../modules/leave/models/leave';
import { PublicHolidayService } from '../../../modules/home/services/public-holiday.service';

@Component({
  selector: 'app-calendar',
  standalone: false,

  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.css',
})
export class CalendarComponent {
  @Input() events: EventInput[] = [];
  holidays = inject(PublicHolidayService);

  calendarOptions: CalendarOptions = {
    initialView: 'dayGridMonth',
    plugins: [dayGridPlugin, interactionPlugin],
    selectable: true,
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,dayGridWeek',
    },
    events: this.events,
    selectOverlap: false,
  };

  ngOnInit(): void {
    this.calendarOptions.events = this.events;
    this.loadHolidays();
  }

  loadHolidays(): void {
    this.holidays.getPublicHolidays().subscribe({
      next: (data) => {
        const calendarEvents = data.map(
          (holiday: { name: string; date: string }) => {
            return {
              title: holiday.name,
              date: holiday.date,
              backgroundColor: '#014601',
            };
          }
        );
        this.calendarOptions.events = [...this.events, ...calendarEvents];
      },
      error: (error) => {
        console.error('There was an error!', error);
      },
    });
  }
}
