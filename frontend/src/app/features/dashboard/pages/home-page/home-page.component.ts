import { Component, inject, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CalendarOptions, EventInput } from '@fullcalendar/core/index.js';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { LeaveRequestComponent } from '../../components/leave-request/leave-request.component';
import { AuthService } from '../../../../core/services/auth.service';
import { LeaveService } from '../../services/leave.service';
import { Leave } from '../../models/leave-responses.interface';

@Component({
  selector: 'app-home-page',
  standalone: false,
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css',
})
export class HomePageComponent implements OnInit {
  private authService = inject(AuthService);
  private leaveService = inject(LeaveService);
  public dialog = inject(MatDialog);

  calendarOptions: CalendarOptions = {
    initialView: 'dayGridMonth',
    plugins: [dayGridPlugin, interactionPlugin],
    selectable: true,
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,dayGridWeek',
    },
    events: [],
    selectOverlap: false,
  };

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

  ngOnInit(): void {
    if (this.isEmployee()) {
      this.loadLeaveHistory();
    }
  }

  loadLeaveHistory(): void {
    this.leaveService.getLeaveHistory().subscribe({
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
        this.calendarOptions.events = events;
      },
      error: (error) => {
        console.error('There was an error!', error);
      },
    });
  }

  transformString(input: string): string {
    // Remove underscores, lowercase the string, and capitalize the first letter
    const result = input.replace(/_/g, ' ').toLowerCase();
    return result.charAt(0).toUpperCase() + result.slice(1);
  }

  objectKeys(obj: any): string[] {
    return Object.keys(obj);
  }

  getLeaveTypeColor(leaveType: string): string {
    return this.leaveTypes[leaveType] || '#000000';
  }

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
