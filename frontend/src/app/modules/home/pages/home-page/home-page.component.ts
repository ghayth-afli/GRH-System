import {
  Component,
  ElementRef,
  inject,
  OnInit,
  ViewChild,
  AfterViewInit,
  ChangeDetectorRef,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FullCalendarComponent } from '@fullcalendar/angular';
import { CalendarOptions, Calendar, EventInput } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import { DateDetailsDialogComponent } from '../../components/date-details-dialog/date-details-dialog.component';
import { SubmitLeaveRequestDialogComponent } from '../../components/submit-leave-request-dialog/submit-leave-request-dialog.component';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';
import { LeaveService } from '../../../leave/services/leave.service';
import { Leave } from '../../../leave/models/leave';
import { PublicHolidayService } from '../../services/public-holiday.service';
import interactionPlugin from '@fullcalendar/interaction';

interface Holiday {
  id: string;
  name: string;
  date: string;
  country: string;
  flag: string;
}

interface LeaveStats {
  balance: number;
  used: number;
  totalRequests: number;
}

interface LegendItem {
  type: string;
  label: string;
  color: string;
  visible: boolean;
}

@Component({
  selector: 'app-homepage',
  standalone: false,
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css'],
})
export class HomePageComponent implements OnInit, AfterViewInit {
  @ViewChild('calendar', { static: false })
  calendarComponent?: FullCalendarComponent;
  @ViewChild('balanceValue') balanceValue?: ElementRef;
  @ViewChild('usedValue') usedValue?: ElementRef;
  @ViewChild('requestsValue') requestsValue?: ElementRef;

  stats: LeaveStats = { balance: 0, used: 0, totalRequests: 0 };
  leaveRequests: Leave[] = [];
  holidays: Holiday[] = [];
  legendItems: LegendItem[] = [
    { type: 'ANNUEL', label: 'Vacation', color: '#007bff', visible: true },
    { type: 'MALADIE', label: 'Sick', color: '#28a745', visible: true },
    { type: 'MATERNITÉ', label: 'Maternity', color: '#ffc107', visible: true },
    { type: 'PATERNITÉ', label: 'Paternity', color: '#17a2b8', visible: true },
    {
      type: 'SANS_SOLDE',
      label: 'Unpaid Leave',
      color: '#6c757d',
      visible: true,
    },
    { type: 'DÉCÈS', label: 'Bereavement', color: '#dc3545', visible: true },
    {
      type: 'TÉLÉTRAVAIL',
      label: 'Remote Work',
      color: '#6610f2',
      visible: true,
    },
    {
      type: 'AUTORISATION',
      label: 'Authorization',
      color: '#e83e8c',
      visible: true,
    },
    { type: 'holiday', label: 'Holiday', color: '#ff9800', visible: true },
    { type: 'weekend', label: 'Weekend', color: '#e0e0e0', visible: true },
  ];
  selectedLeaveTypes: string[] = [];
  showOnlyHolidays = false;
  showOnlyUser = false;
  showHolidays = true;
  calendarView = 'dayGridMonth';
  drawerOpen = false;
  animateBalance = false;
  animateUsed = false;
  animateRequests = false;

  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'dayGridMonth',
    headerToolbar: false,
    events: [],
    eventContent: this.renderEventContent.bind(this),
    dateClick: this.handleDateClick.bind(this),
    weekends: true,
    eventDisplay: 'block',
    height: 'auto',
    eventTimeFormat: {
      hour: '2-digit',
      minute: '2-digit',
      meridiem: false,
    },
    displayEventEnd: true,
    selectable: true, // Ensure dates are selectable
  };

  leaveService = inject(LeaveService);
  publicHolidayService = inject(PublicHolidayService);

  constructor(
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.fetchData();
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.animateBalance = true;
      this.animateUsed = true;
      this.animateRequests = true;
      this.cdr.detectChanges();
    }, 100);
  }

  fetchData() {
    this.leaveService.getLeaveBalance().subscribe({
      next: (stats) => {
        this.stats.balance = stats.remainingLeave;
        this.stats.used = stats.usedLeave;
        this.cdr.detectChanges();
      },
      error: (err) => this.showErrorSnackbar('Failed to load leave balance'),
    });

    this.leaveService.getLeaveHistory().subscribe({
      next: (allRequests) => {
        this.stats.totalRequests = allRequests.length;
        this.leaveRequests = allRequests.filter(
          (request) => request.status === 'APPROVED'
        );
        this.updateCalendarEvents();
      },
      error: (err) => this.showErrorSnackbar('Failed to load leave history'),
    });

    this.publicHolidayService.getPublicHolidays().subscribe({
      next: (holidaysData) => {
        this.holidays = holidaysData.map((holiday: any, index: number) => ({
          id: holiday.id || `h${index + 1}`,
          name: holiday.name,
          date: holiday.date,
          country: holiday.country,
          flag: holiday.flag,
        }));
        this.updateCalendarEvents();
      },
      error: (err) => this.showErrorSnackbar('Failed to load public holidays'),
    });
  }

  get calendarApi(): Calendar | undefined {
    return this.calendarComponent?.getApi();
  }

  renderEventContent(arg: any) {
    const title = arg.event.title || 'Untitled Event';
    const timeText = arg.timeText || '';

    let timeHtml = '';
    if (timeText && this.calendarView !== 'dayGridMonth') {
      timeHtml = `<div class="fc-event-time">${timeText}</div>`;
    }

    return {
      html: `<div class="fc-event-main-frame">
                   ${timeHtml}
                   <div class="fc-event-title-container">
                       <div class="fc-event-title fc-sticky">${title}</div>
                   </div>
               </div>`,
    };
  }

  handleDateClick(arg: any) {
    if (!arg?.dateStr) return;
    const date = arg.dateStr;
    const calendarApi = this.calendarApi;
    if (!calendarApi) {
      return;
    }

    const events = calendarApi.getEvents().filter((event) => {
      let eventStart = event.startStr;
      let eventEnd = event.endStr;

      if (event.allDay && eventEnd) {
        const endDate = new Date(eventEnd);
        endDate.setDate(endDate.getDate() - 1);
        eventEnd = endDate.toISOString().split('T')[0];
      }

      if (!eventEnd) eventEnd = eventStart;

      return date >= eventStart && date <= eventEnd;
    });

    const leaves = events
      .filter((e) => e.extendedProps['type'] === 'leave')
      .map((e) => ({
        name: e.title,
        type: e.extendedProps['leaveType'],
        duration: e.extendedProps['duration'],
        status: e.extendedProps['status'],
        color:
          this.legendItems.find(
            (item) => item.type === e.extendedProps['leaveType']
          )?.color || '#000000',
      }));

    const holiday = events.find((e) => e.extendedProps['type'] === 'holiday');

    this.dialog.open(DateDetailsDialogComponent, {
      width: '400px',
      data: {
        date,
        leaves,
        holiday: holiday
          ? {
              name: holiday.title,
              date: holiday.startStr,
              country: holiday.extendedProps['country'],
              flag: holiday.extendedProps['flag'],
            }
          : null,
      },
    });
  }

  updateCalendarEvents() {
    const events: EventInput[] = [];

    if (!this.showOnlyHolidays && this.leaveRequests) {
      this.leaveRequests.forEach((request) => {
        const legendItem = this.legendItems.find(
          (item) => item.type === request.leaveType
        );

        if (!legendItem?.visible) return;

        const eventTitle = legendItem?.label || request.leaveType;
        const duration = this.calculateDurationString(request);

        if (
          request.leaveType === 'AUTORISATION' &&
          request.startHOURLY &&
          request.endHOURLY
        ) {
          events.push({
            id: String(request.id),
            title: eventTitle,
            start: `${request.startDate}T${request.startHOURLY}`,
            end: `${request.endDate}T${request.endHOURLY}`,
            allDay: false,
            color: legendItem.color,
            extendedProps: {
              type: 'leave',
              leaveType: request.leaveType,
              duration,
              status: request.status,
            },
          });
        } else {
          events.push({
            id: String(request.id),
            title: eventTitle,
            start: request.startDate,
            end: new Date(
              new Date(request.endDate).getTime() + 24 * 3600 * 1000
            )
              .toISOString()
              .split('T')[0],
            allDay: true,
            color: legendItem.color,
            extendedProps: {
              type: 'leave',
              leaveType: request.leaveType,
              duration,
              status: request.status,
            },
          });
        }
      });
    }

    if (this.showHolidays && this.holidays) {
      this.holidays.forEach((holiday) => {
        if (!this.legendItems.find((item) => item.type === 'holiday')?.visible)
          return;

        events.push({
          id: holiday.id,
          title: holiday.name,
          start: holiday.date,
          allDay: true,
          color: '#ff9800',
          extendedProps: {
            type: 'holiday',
            country: holiday.country,
            flag: holiday.flag,
          },
        });
      });
    }

    if (this.calendarApi) {
      this.calendarApi.removeAllEvents();
      this.calendarApi.addEventSource(events);
    } else {
      this.calendarOptions.events = events;
    }

    this.cdr.detectChanges();
  }

  calculateDurationString(request: Leave): string {
    try {
      if (
        request.leaveType === 'AUTORISATION' &&
        request.startHOURLY &&
        request.endHOURLY
      ) {
        const start = new Date(`${request.startDate}T${request.startHOURLY}`);
        const end = new Date(`${request.endDate}T${request.endHOURLY}`);
        if (isNaN(start.getTime()) || isNaN(end.getTime())) return 'N/A';

        let diffMs = end.getTime() - start.getTime();
        const hours = Math.floor(diffMs / 3600000);
        diffMs -= hours * 3600000;
        const minutes = Math.floor(diffMs / 60000);

        let durationStr = '';
        if (hours > 0) durationStr += `${hours}h `;
        if (minutes > 0) durationStr += `${minutes}m`;
        return durationStr.trim() || '0m';
      } else {
        const days = this.calculateDays(request.startDate, request.endDate);
        return `${days} day${days > 1 ? 's' : ''}`;
      }
    } catch (e) {
      return 'N/A';
    }
  }

  calculateDays(startDate: string, endDate: string): number {
    try {
      const start = new Date(startDate);
      const end = new Date(endDate);
      if (isNaN(start.getTime()) || isNaN(end.getTime())) return 0;
      const diffTime = Math.abs(end.getTime() - start.getTime());
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
      return diffDays;
    } catch (error) {
      return 0;
    }
  }

  private showErrorSnackbar(message: string) {
    this.snackBar.openFromComponent(CustomSnackbarComponent, {
      data: { message, type: 'error' },
      duration: 5000,
      panelClass: ['custom-snackbar'],
      horizontalPosition: 'end',
      verticalPosition: 'top',
    });
  }

  goToday() {
    this.calendarApi?.today();
  }

  toggleView() {
    this.calendarView =
      this.calendarView === 'dayGridMonth' ? 'timeGridWeek' : 'dayGridMonth';
    this.calendarApi?.changeView(this.calendarView);
  }

  toggleLegendItem(type: string) {
    const item = this.legendItems.find((i) => i.type === type);
    if (item) {
      item.visible = !item.visible;
      this.updateCalendarEvents();
    }
  }

  openSubmitDialog() {
    const dialogRef = this.dialog.open(SubmitLeaveRequestDialogComponent, {
      width: '500px',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.fetchData();
        this.snackBar.openFromComponent(CustomSnackbarComponent, {
          data: {
            message: 'Leave request submitted successfully!',
            type: 'success',
          },
          duration: 5000,
          panelClass: ['custom-snackbar'],
          horizontalPosition: 'end',
          verticalPosition: 'top',
        });
      }
    });
  }

  toggleDrawer() {
    this.drawerOpen = !this.drawerOpen;
  }
}
