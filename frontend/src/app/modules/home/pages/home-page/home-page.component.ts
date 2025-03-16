import { Component, inject } from '@angular/core';
import { EventInput } from '@fullcalendar/core/index.js';
import { Leave } from '../../../leave/models/leave';
import { ActivatedRoute } from '@angular/router';
import { map, Observable } from 'rxjs';
import { PublicHolidayService } from '../../services/public-holiday.service';

@Component({
  selector: 'app-home-page',
  standalone: false,

  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css',
})
export class HomePageComponent {
  leaves: EventInput[] = [];
  leaveHistory$: Observable<Leave[]>;
  holidays = inject(PublicHolidayService);

  leaveTypes: { [key: string]: string } = {
    Jour_Férié: '#ff0000',
    ANNUEL: '#fd5b00',
    MALADIE: '#6195ed',
    MATERNITÉ: '#004f99',
    PATERNITÉ: '#0e274e',
    SANS_SOLDE: '#cacaca',
    DÉCÈS: '#666666',
    TÉLÉTRAVAIL: '#43475a',
    AUTORISATION: '#030a23',
  };

  constructor(private route: ActivatedRoute) {
    this.leaveHistory$ = this.route.data.pipe(
      map((data) => data['leaveHistory'])
    );
    this.holidays.getPublicHolidays().subscribe({
      next: (data) => {
        console.log('Holidays', data);
      },
      error: (error) => {
        console.error('There was an error!', error);
      },
    });
  }

  ngOnInit(): void {
    this.loadLeaveHistory();
  }

  loadLeaveHistory(): void {
    this.leaveHistory$.subscribe({
      next: (data: Leave[]) => {
        const events: EventInput[] = data
          .filter((leave) => leave.status === 'APPROUVÉE')
          .map((leave) => {
            return {
              start: leave.startDate,
              end: leave.endDate,
              backgroundColor: this.getLeaveTypeColor(leave.leaveType),
            };
          }, []);
        this.leaves = events;
      },
      error: (error) => {
        console.error('There was an error!', error);
      },
    });
  }

  getLeaveTypeColor(leaveType: string): string {
    return this.leaveTypes[leaveType] || '#000000';
  }
}
