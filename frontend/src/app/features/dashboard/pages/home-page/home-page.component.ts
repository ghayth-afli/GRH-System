import { Component, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CalendarOptions, EventInput } from '@fullcalendar/core/index.js';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { LeaveRequestComponent } from '../../components/leave-request/leave-request.component';
import { AuthService } from '../../../../core/services/auth.service';
import { LeaveService } from '../../services/leave.service';

@Component({
  selector: 'app-home-page',
  standalone: false,

  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css',
})
export class HomePageComponent {
  private authService = inject(AuthService);
  private leaveService = inject(LeaveService);
  public dialog = inject(MatDialog);

  calendarOptions: CalendarOptions = {
    initialView: 'dayGridMonth',
    plugins: [dayGridPlugin, interactionPlugin],
    selectable: true,
    //select: this.handleDateSelect.bind(this),
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,dayGridWeek',
    },
    //events: this.selectedEvents,
    selectOverlap: false,
  };

  // selectedStartDate: Date | null = null;
  // selectedEndDate: Date | null = null;
  // selectedLeaveType: string | null = null;
  // selectedEvents: EventInput[] = [];
  // leaveTypes = [
  //   { name: 'ANNUEL', color: '#fd5b00' },
  //   { name: 'MALADIE', color: '#6195ed' },
  //   { name: 'MATERNITÉ', color: '#004f99' },
  //   { name: 'PATERNITÉ', color: '#0e274e' },
  //   { name: 'SANS_SOLDE', color: '#cacaca' },
  //   { name: 'DÉCÈS', color: '#666666' },
  //   { name: 'TÉLÉTRAVAIL', color: '#43475a' },
  //   { name: 'AUTORISATION', color: '#ffffff' },
  // ];

  // updateCalendarEvents() {
  //   if (
  //     this.selectedStartDate &&
  //     this.selectedEndDate &&
  //     this.selectedLeaveType
  //   ) {
  //     const selectedColor = this.leaveTypes.find(
  //       (lt) => lt.name === this.selectedLeaveType
  //     )?.color;

  //     this.selectedEvents = [
  //       {
  //         start: this.selectedStartDate,
  //         end: this.selectedEndDate,
  //         display: 'background',
  //         color: selectedColor,
  //         allDay: true,
  //       },
  //     ];
  //   } else {
  //     this.selectedEvents = [];
  //   }
  // }

  // handleDateSelect(selectInfo: any) {
  //   this.selectedStartDate = selectInfo.start;
  //   this.selectedEndDate = selectInfo.end;
  //   this.updateCalendarEvents();
  // }

  // selectLeaveType(type: string) {
  //   this.selectedLeaveType = type;
  // }

  // openDialog() {
  //   this.dialog.open(LeaveRequestComponent, {
  //     data: {
  //       startDate: this.selectedStartDate,
  //       endDate: this.selectedEndDate,
  //       leaveType: this.selectedLeaveType,
  //     },
  //   });
  // }

  // isFormValid(): boolean {
  //   return (
  //     !!this.selectedStartDate &&
  //     !!this.selectedEndDate &&
  //     !!this.selectedLeaveType
  //   );
  // }

  openDialog() {
    this.dialog.open(LeaveRequestComponent, {});
  }
  isManager() {
    return this.authService.hasRole('Manager');
  }
  isHr() {
    return this.authService.hasRole('HR');
  }
  isEmployee() {
    return this.authService.hasRole('Employee');
  }
}
