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
import { map } from 'rxjs';

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

  stats: LeaveStats = { balance: 15, used: 5, totalRequests: 3 };
  leaveRequests: Leave[] = [];
  holidays: Holiday[] = [
    {
      id: 'h_default',
      name: 'Eid al-Fitr (Example)',
      date: '2025-03-30',
      country: 'Global',
      flag: '🌐',
    },
  ];
  legendItems: LegendItem[] = [
    { type: 'ANNUEL', label: 'Vacation', color: '#007bff', visible: true },
    { type: 'MALADIE', label: 'Sick', color: '#28a745', visible: true },
    { type: 'MATERNITE', label: 'Maternity', color: '#ffc107', visible: true },
    { type: 'PATERNITE', label: 'Paternity', color: '#17a2b8', visible: true },
    {
      type: 'SANS_SOLDE',
      label: 'Unpaid Leave',
      color: '#6c757d',
      visible: true,
    },
    { type: 'DECES', label: 'Bereavement', color: '#dc3545', visible: true },
    {
      type: 'TELETRAVAIL',
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
    plugins: [dayGridPlugin, timeGridPlugin],
    initialView: 'dayGridMonth',
    headerToolbar: false,
    events: [],
    eventContent: this.renderEventContent.bind(this),
    dateClick: this.handleDateClick.bind(this),
    weekends: true,
    eventDisplay: 'block',
    height: 'auto',
    eventTimeFormat: {
      hour: 'numeric',
      minute: '2-digit',
      meridiem: false,
      omitZeroMinute: true,
    },
    displayEventEnd: true,
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
      // Ensure calendar is properly initialized
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
      error: (error) => console.error('Error fetching leave balance:', error),
    });

    this.leaveService
      .getLeaveHistory()
      .pipe(
        //only leave requests with status 'ACCEPTE'
        map((requests) =>
          requests.filter((request) => request.status === 'APPROUVÉE')
        )
      )
      .subscribe({
        next: (requests) => {
          this.leaveRequests = requests;
          console.log(
            'Fetched Leave Requests:',
            JSON.stringify(this.leaveRequests, null, 2)
          );
          this.updateCalendarEvents();
        },
        error: (error) => {
          console.error('Error fetching leave history:', error);
          this.snackBar.openFromComponent(CustomSnackbarComponent, {
            data: { message: 'Failed to load leave requests', type: 'error' },
            duration: 5000,
            panelClass: ['custom-snackbar'],
            horizontalPosition: 'end',
            verticalPosition: 'top',
          });
          this.updateCalendarEvents();
        },
      });
    this.leaveService.getLeaveHistory().subscribe({
      next: (requests) => {
        console.log(
          'Fetched Leave Requests:',
          JSON.stringify(this.leaveRequests, null, 2)
        );
        this.stats.totalRequests = requests.length;
      },
      error: (error) => {
        console.error('Error fetching leave history:', error);
        this.snackBar.openFromComponent(CustomSnackbarComponent, {
          data: { message: 'Failed to load leave requests', type: 'error' },
          duration: 5000,
          panelClass: ['custom-snackbar'],
          horizontalPosition: 'end',
          verticalPosition: 'top',
        });
        this.updateCalendarEvents();
      },
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
        console.log(
          'Fetched and Mapped Holidays:',
          JSON.stringify(this.holidays, null, 2)
        );
        this.updateCalendarEvents();
      },
      error: (error) => {
        console.error('Error fetching public holidays:', error);
        this.snackBar.openFromComponent(CustomSnackbarComponent, {
          data: { message: 'Failed to load public holidays', type: 'error' },
          duration: 5000,
          panelClass: ['custom-snackbar'],
          horizontalPosition: 'end',
          verticalPosition: 'top',
        });
        this.updateCalendarEvents();
      },
    });
  }

  get calendarApi(): Calendar | undefined {
    return this.calendarComponent?.getApi();
  }

  renderEventContent(arg: any) {
    // For regular event display, show the title properly
    if (arg.event) {
      const event = arg.event;
      const title = event.title || 'Untitled Event';
      const backgroundColor =
        event.backgroundColor || event.borderColor || '#ccc';

      return {
        html: `<div class="fc-event-main-frame">
                 <div class="fc-event-title-container">
                   <div class="fc-event-title fc-sticky" style="color: ${
                     event.textColor || '#fff'
                   }">${title}</div>
                 </div>
               </div>`,
      };
    }

    // For day grid cells with dots (if needed)
    const calendarApi = this.calendarApi;
    if (!calendarApi) {
      console.warn('Calendar API not available in renderEventContent');
      return {
        html: `<div class="fc-daygrid-day-number">${
          arg.dayNumberText || ''
        }</div>`,
      };
    }

    try {
      const dayEvents = calendarApi.getEvents().filter((event) => {
        if (!event.start || !arg.date) return false;
        const eventDate = new Date(event.start as Date);
        const argDate = new Date(arg.date);
        return eventDate.toDateString() === argDate.toDateString();
      });

      let dotsHtml = '';
      if (dayEvents && dayEvents.length > 0) {
        dotsHtml =
          `<div class="event-dots">` +
          dayEvents
            .slice(0, 3)
            .map(
              (event) =>
                `<div class="event-dot" title="${
                  event.title || 'Event'
                }" style="background-color: ${
                  event.backgroundColor || event.borderColor || '#ccc'
                }"></div>`
            )
            .join('') +
          `</div>`;
      }

      return {
        html: `<div class="fc-daygrid-day-number">${
          arg.dayNumberText || ''
        }</div>${dotsHtml}`,
      };
    } catch (error) {
      console.error('Error in renderEventContent:', error);
      return {
        html: `<div class="fc-daygrid-day-number">${
          arg.dayNumberText || ''
        }</div>`,
      };
    }
  }

  handleDateClick(arg: any) {
    if (!arg || !arg.dateStr) {
      console.warn('Invalid dateClick argument:', arg);
      return;
    }

    const date = arg.dateStr;
    const calendarApi = this.calendarApi;

    if (!calendarApi) {
      console.warn('Calendar API not available in handleDateClick');
      return;
    }

    try {
      const clickedEvents = calendarApi.getEvents().filter((event) => {
        if (!event.start) return false;
        const eventDate = new Date(event.start as Date);
        const clickDate = new Date(date);
        return eventDate.toDateString() === clickDate.toDateString();
      });

      const leaves = clickedEvents
        .filter((e) => e.extendedProps && e.extendedProps['type'] === 'leave')
        .map((e) => ({
          name: e.title || e.extendedProps['requesterName'] || 'Unknown',
          type: e.extendedProps['leaveType'] || 'N/A',
          duration: e.extendedProps['duration'] || 'N/A',
          status: e.extendedProps['status'] || 'N/A',
        }));

      const holidayEvent = clickedEvents.find(
        (e) => e.extendedProps && e.extendedProps['type'] === 'holiday'
      );

      const userLeave = leaves.find(
        (l) => l.name && l.name.includes('Ali Ben Salah')
      );

      this.dialog.open(DateDetailsDialogComponent, {
        width: '400px',
        data: {
          date,
          leaves,
          holiday: holidayEvent
            ? {
                name:
                  holidayEvent.title ||
                  holidayEvent.extendedProps['originalName'] ||
                  'Holiday',
                date,
              }
            : null,
          userLeave,
        },
      });
    } catch (error) {
      console.error('Error in handleDateClick:', error);
    }
  }

  updateCalendarEvents() {
    try {
      const events: EventInput[] = [];
      const userName = 'Ali Ben Salah';

      if (!this.showOnlyHolidays && this.leaveRequests) {
        this.leaveRequests.forEach((request) => {
          if (
            !request ||
            !request.startDate ||
            !request.endDate ||
            !request.leaveType
          ) {
            console.warn(
              'Skipping leave request due to missing data:',
              request
            );
            return;
          }

          if (this.showOnlyUser && request.Name !== userName) return;

          if (
            this.selectedLeaveTypes.length &&
            !this.selectedLeaveTypes.includes(request.leaveType)
          )
            return;

          const legendItem = this.legendItems.find(
            (item) => item.type === request.leaveType
          );

          if (legendItem?.visible) {
            const leaveLabel = legendItem?.label || request.leaveType;
            const duration = this.calculateDays(
              request.startDate,
              request.endDate
            );

            // Ensure title is always defined and not empty
            const eventTitle = `${leaveLabel}`;

            events.push({
              id: String(request.id),
              title: eventTitle, // This ensures title is never undefined
              start: request.startDate,
              end: new Date(
                new Date(request.endDate).getTime() + 24 * 3600 * 1000
              )
                .toISOString()
                .split('T')[0],
              color: legendItem?.color || '#808080',
              backgroundColor: legendItem?.color || '#808080',
              borderColor: legendItem?.color || '#808080',
              textColor: '#ffffff', // Ensure text is visible
              extendedProps: {
                type: 'leave',
                leaveType: request.leaveType,
                duration,
                status: request.status,
                leaveLabel: leaveLabel,
              },
            });
          }
        });
      }

      const holidayLegendItem = this.legendItems.find(
        (item) => item.type === 'holiday'
      );

      if (
        this.showHolidays &&
        !this.showOnlyUser &&
        holidayLegendItem?.visible &&
        this.holidays
      ) {
        this.holidays.forEach((holiday) => {
          if (!holiday || !holiday.date) {
            console.warn('Skipping holiday due to missing date:', holiday);
            return;
          }

          // Ensure holiday title is never undefined
          const holidayName = holiday.name || 'Unnamed Holiday';
          const holidayTitle = `🏖️ ${holidayName}`;

          events.push({
            id: holiday.id,
            title: holidayTitle, // This ensures title is never undefined
            start: holiday.date,
            allDay: true,
            color: holidayLegendItem?.color || '#ff9800',
            backgroundColor: holidayLegendItem?.color || '#ff9800',
            borderColor: holidayLegendItem?.color || '#ff9800',
            textColor: '#000000',
            extendedProps: {
              type: 'holiday',
              country: holiday.country,
              flag: holiday.flag,
              originalName: holidayName,
            },
          });
        });
      }

      const weekendLegendItem = this.legendItems.find(
        (item) => item.type === 'weekend'
      );

      if (weekendLegendItem?.visible) {
        const year = new Date().getFullYear();
        const startYear = new Date(`${year}-01-01`);
        const endYear = new Date(`${year}-12-31`);

        for (
          let d = new Date(startYear);
          d <= endYear;
          d.setDate(d.getDate() + 1)
        ) {
          if (d.getDay() === 0 || d.getDay() === 6) {
            const dayName = d.getDay() === 0 ? 'Sunday' : 'Saturday';
            events.push({
              id: `w-${d.toISOString().split('T')[0]}`,
              title: `Weekend - ${dayName}`, // Even background events can have titles
              start: d.toISOString().split('T')[0],
              display: 'background',
              color: weekendLegendItem?.color || '#e0e0e0',
              backgroundColor: weekendLegendItem?.color || '#e0e0e0',
              extendedProps: {
                type: 'weekend',
                dayName: dayName,
              },
            });
          }
        }
      }

      console.log(
        'Generated events with titles:',
        events.map((e) => ({
          id: e.id,
          title: e.title,
          type: e.extendedProps?.['type'],
        }))
      );

      // Update calendar events using the calendar API directly
      const calendarApi = this.calendarApi;
      if (calendarApi) {
        // Remove all existing events
        calendarApi.removeAllEvents();
        // Add new events one by one to ensure proper handling
        events.forEach((event) => {
          try {
            calendarApi.addEvent(event);
          } catch (eventError) {
            console.error('Error adding individual event:', eventError, event);
          }
        });
      } else {
        // Fallback to updating the options
        this.calendarOptions = { ...this.calendarOptions, events };
      }

      // Trigger change detection
      this.cdr.detectChanges();
    } catch (error) {
      console.error('Error updating calendar events:', error);
    }
  }

  calculateDays(startDate: string, endDate: string): number {
    try {
      const start = new Date(startDate);
      const end = new Date(endDate);
      if (isNaN(start.getTime()) || isNaN(end.getTime())) return 0;
      return (
        Math.ceil((end.getTime() - start.getTime()) / (1000 * 3600 * 24)) + 1
      );
    } catch (error) {
      console.error('Error calculating days:', error);
      return 0;
    }
  }

  goToday() {
    try {
      this.calendarApi?.today();
    } catch (error) {
      console.error('Error navigating to today:', error);
    }
  }

  toggleView() {
    try {
      this.calendarView =
        this.calendarView === 'dayGridMonth' ? 'timeGridWeek' : 'dayGridMonth';
      this.calendarApi?.changeView(this.calendarView);
    } catch (error) {
      console.error('Error toggling view:', error);
    }
  }

  toggleLegendItem(type: string) {
    try {
      const item = this.legendItems.find((i) => i.type === type);
      if (item) {
        item.visible = !item.visible;
        this.updateCalendarEvents();
      }
    } catch (error) {
      console.error('Error toggling legend item:', error);
    }
  }

  filterByLeaveType(type: string) {
    try {
      this.selectedLeaveTypes = this.selectedLeaveTypes.includes(type)
        ? this.selectedLeaveTypes.filter((t) => t !== type)
        : [...this.selectedLeaveTypes, type];
      this.showOnlyHolidays = false;
      this.updateCalendarEvents();
    } catch (error) {
      console.error('Error filtering by leave type:', error);
    }
  }

  openSubmitDialog() {
    const dialogRef = this.dialog.open(SubmitLeaveRequestDialogComponent, {
      width: '500px',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        try {
          const newLeaveId =
            this.leaveRequests && this.leaveRequests.length > 0
              ? Math.max(...this.leaveRequests.map((r) => Number(r.id) || 0)) +
                1
              : 1;

          const newLeave: Leave = {
            id: newLeaveId,
            Name: 'Ali Ben Salah',
            Department: 'Unknown',
            startDate: result.startDate,
            endDate: result.endDate,
            leaveType: result.leaveType,
            status: 'EN_ATTENTE',
          };

          if (!this.leaveRequests) {
            this.leaveRequests = [];
          }

          this.leaveRequests.push(newLeave);
          this.stats.totalRequests++;
          this.updateCalendarEvents();

          console.log(
            'Leave request submitted (locally simulated):',
            JSON.stringify(
              { action: 'submit_leave_request', ...result },
              null,
              2
            )
          );

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
        } catch (error) {
          console.error('Error submitting leave request:', error);
        }
      }
    });
  }

  toggleDrawer() {
    this.drawerOpen = !this.drawerOpen;
  }
}
