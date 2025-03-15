import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-calendar-side-bar',
  standalone: false,

  templateUrl: './calendar-side-bar.component.html',
  styleUrl: './calendar-side-bar.component.css',
})
export class CalendarSideBarComponent {
  @Input() leaveTypes: { [key: string]: string } = {};

  transformString(input: string): string {
    const result = input.replace(/_/g, ' ').toLowerCase();
    return result.charAt(0).toUpperCase() + result.slice(1);
  }

  objectKeys(obj: any): string[] {
    return Object.keys(obj);
  }
}
