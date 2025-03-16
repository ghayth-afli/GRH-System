import { Component, inject, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { LeaveRequestFormModalComponent } from '../../../leave/components/leave-request-form-modal/leave-request-form-modal.component';

@Component({
  selector: 'app-calendar-side-bar',
  standalone: false,

  templateUrl: './calendar-side-bar.component.html',
  styleUrl: './calendar-side-bar.component.css',
})
export class CalendarSideBarComponent {
  @Input() leaveTypes: { [key: string]: string } = {};
  public dialog = inject(MatDialog);

  transformString(input: string): string {
    const result = input.replace(/_/g, ' ').toLowerCase();
    return result.charAt(0).toUpperCase() + result.slice(1);
  }

  objectKeys(obj: any): string[] {
    return Object.keys(obj);
  }

  openDialog() {
    this.dialog.open(LeaveRequestFormModalComponent, {});
  }
}
