import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { environment } from '../../environments/environment';
import { NotificationData } from '../models/NotificationData';

@Injectable({
  providedIn: 'root',
})
export class SseService<T> {
  private sseUrl = `${environment.apiUrl}/notifications/sse-endpoint`;
  private eventSource: EventSource | null = null;
  private notificationSubjects: Map<string, Subject<T>> = new Map();

  constructor() {}

  connect(eventType: string): Observable<T> {
    // Create a new subject for this event type if it doesn't exist
    if (!this.notificationSubjects.has(eventType)) {
      this.notificationSubjects.set(eventType, new Subject<T>());
    }

    // Initialize the EventSource if it doesn't exist
    if (!this.eventSource) {
      this.eventSource = new EventSource(this.sseUrl);

      // Set up error handling for the EventSource
      this.eventSource.onerror = (error) => {
        console.error('EventSource error:', error);
        // You could implement reconnection logic here
      };
    }

    // Add the event listener if it hasn't been added already
    this.setupEventListener(eventType);

    // Return the Observable for this specific event type
    return this.notificationSubjects.get(eventType)!.asObservable();
  }

  private setupEventListener(eventType: string): void {
    // We need to check if this specific event type already has a listener
    // This requires some custom handling, as the EventSource doesn't provide a way to check

    // Get the subject for this event type
    const subject = this.notificationSubjects.get(eventType);
    if (!subject) return;

    // Add event listener for this specific event type
    this.eventSource!.addEventListener(eventType, (event: MessageEvent) => {
      try {
        const notificationData: T = JSON.parse(event.data);
        subject.next(notificationData);
      } catch (error) {
        console.error(
          `Error parsing SSE message for event type ${eventType}:`,
          error
        );
      }
    });

    console.log(`Event listener set up for: ${eventType}`);
  }

  disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }

    // Clear all subjects
    this.notificationSubjects.forEach((subject) => subject.complete());
    this.notificationSubjects.clear();
  }
}
