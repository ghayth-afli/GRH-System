import { Component, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CalendarOptions, EventInput } from '@fullcalendar/core/index.js';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { LeaveRequestComponent } from '../../components/leave-request/leave-request.component';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-home-page',
  standalone: false,

  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css',
})
export class HomePageComponent {
  selectedStartDate: Date | null = null;
  selectedEndDate: Date | null = null;
  selectedLeaveType: string | null = null;
  selectedEvents: EventInput[] = [];

  private authService = inject(AuthService);

  isManager() {
    return this.authService.hasRole('Manager');
  }
  isHr() {
    return this.authService.hasRole('Hr');
  }
  isEmployee() {
    console.log(this.authService.hasRole('Manager'));
    console.log(this.authService.hasRole('Hr'));
    console.log(this.authService.hasRole('Employee'));

    return this.authService.hasRole('Employee');
  }

  updateCalendarEvents() {
    if (
      this.selectedStartDate &&
      this.selectedEndDate &&
      this.selectedLeaveType
    ) {
      const selectedColor = this.leaveTypes.find(
        (lt) => lt.name === this.selectedLeaveType
      )?.color;

      this.selectedEvents = [
        {
          start: this.selectedStartDate,
          end: this.selectedEndDate,
          display: 'background',
          color: selectedColor,
          allDay: true,
        },
      ];
    } else {
      this.selectedEvents = [];
    }
  }

  leaveTypes = [
    { name: 'ANNUEL', color: '#fd5b00' },
    { name: 'MALADIE', color: '#6195ed' },
    { name: 'MATERNITÉ', color: '#004f99' },
    { name: 'PATERNITÉ', color: '#0e274e' },
    { name: 'SANS_SOLDE', color: '#cacaca' },
    { name: 'DÉCÈS', color: '#666666' },
    { name: 'TÉLÉTRAVAIL', color: '#43475a' },
    { name: 'AUTORISATION', color: '#ffffff' },
  ];

  calendarOptions: CalendarOptions = {
    initialView: 'dayGridMonth',
    plugins: [dayGridPlugin, interactionPlugin],
    selectable: true,
    select: this.handleDateSelect.bind(this),
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,dayGridWeek',
    },
    events: this.selectedEvents,
    selectOverlap: false,
  };

  constructor(public dialog: MatDialog) {}

  handleDateSelect(selectInfo: any) {
    this.selectedStartDate = selectInfo.start;
    this.selectedEndDate = selectInfo.end;
    this.updateCalendarEvents();
  }

  selectLeaveType(type: string) {
    this.selectedLeaveType = type;
  }

  openDialog() {
    this.dialog.open(LeaveRequestComponent, {
      data: {
        startDate: this.selectedStartDate,
        endDate: this.selectedEndDate,
        leaveType: this.selectedLeaveType,
      },
    });
  }

  isFormValid(): boolean {
    return (
      !!this.selectedStartDate &&
      !!this.selectedEndDate &&
      !!this.selectedLeaveType
    );
  }
}
