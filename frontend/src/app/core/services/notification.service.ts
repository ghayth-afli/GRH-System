import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { BehaviorSubject, map, Observable, ReplaySubject, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import SockJS from 'sockjs-client';
import { CompatClient, Stomp } from '@stomp/stompjs';
import { Notification } from '../models/Notification';
import { NotificationSoundSettingsService } from './notification-sound-settings.service';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private apiUrl: string = `${environment.apiUrl}/notifications`;
  private stompClient: CompatClient | null = null;
  private connected$ = new BehaviorSubject<boolean>(false);
  private notificationSound: HTMLAudioElement;
  private soundEnabled: boolean = true;
  private soundVolume: number = 0.5;

  // Store notifications
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  notifications$ = this.notificationsSubject.asObservable();

  // Store unread count
  private unreadCountSubject = new BehaviorSubject<number>(0);
  unreadCount$ = this.unreadCountSubject.asObservable();

  // Most recent notification
  private latestNotificationSubject = new ReplaySubject<Notification>(1);
  latestNotification$ = this.latestNotificationSubject.asObservable();

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private soundSettingsService: NotificationSoundSettingsService
  ) {
    // Initialize notification sound
    this.notificationSound = new Audio(
      '/assets/sound/mixkit-message-pop-alert-2354.mp3'
    );

    // Subscribe to sound settings
    this.soundSettingsService.soundEnabled$.subscribe((enabled) => {
      this.soundEnabled = enabled;
    });

    this.soundSettingsService.volume$.subscribe((volume) => {
      this.soundVolume = volume;
      this.notificationSound.volume = volume;
    });

    if (this.authService.authenticatedUser) {
      this.connectWebSocket();
      this.loadNotifications();
      this.loadUnreadCount();
    } else {
      this.disconnect();
    }
  }

  // WebSocket Management
  connectWebSocket(): void {
    // Create a factory function instead of a direct instance
    this.stompClient = Stomp.over(() => {
      return new SockJS(`http://localhost:8086/ws-notification`);
    });
    this.stompClient.reconnectDelay = 5000;

    const headers = {
      Authorization: `Bearer ${this.authService.accessToken}`,
    };

    this.stompClient.connect(
      headers,
      () => {
        this.connected$.next(true);

        this.subscribeToNotifications();
        this.sendConnectionMessage();
      },
      (error: any) => {
        console.error('WebSocket Connection Error:', error);
        setTimeout(() => {
          if (this.authService.authenticatedUser) {
            this.connectWebSocket();
          }
        }, 5000);
      }
    );
  }

  disconnect(): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect(() => {
        this.connected$.next(false);
      });
    }
  }

  isConnected(): Observable<boolean> {
    return this.connected$.asObservable();
  }

  // Sound related methods
  playNotificationSound(): void {
    if (!this.soundEnabled) return;

    try {
      this.notificationSound.currentTime = 0; // Reset sound to beginning
      this.notificationSound.play();
    } catch (error) {
      console.error('Error playing notification sound:', error);
    }
  }

  // API Calls
  loadNotifications(): void {
    this.http
      .get<Notification[]>(this.apiUrl)
      .pipe(
        map((notifications) =>
          notifications.map((notification) => ({
            ...notification,
            createdAt: new Date(notification.createdAt),
          }))
        )
      )
      .subscribe({
        next: (notifications) => {
          console.log('Loaded notifications:', notifications);
          this.notificationsSubject.next(notifications);
        },
        error: (error) => {
          console.error('Error loading notifications', error);
        },
      });
  }

  loadUnreadCount(): void {
    this.http.get<number>(`${this.apiUrl}/unread/count`).subscribe({
      next: (count) => {
        this.unreadCountSubject.next(count);
      },
      error: (error) => {
        console.error('Error loading unread count', error);
      },
    });
  }

  markAsRead(id: number): Observable<Notification> {
    return this.http.put<Notification>(`${this.apiUrl}/${id}/read`, {}).pipe(
      tap((notification) => {
        this.unreadCountSubject.next(
          Math.max(0, this.unreadCountSubject.value - 1)
        );

        const currentNotifications = this.notificationsSubject.value;
        const updatedNotifications = currentNotifications.map((n) =>
          n.id === id ? { ...n, read: true } : n
        );
        this.notificationsSubject.next(updatedNotifications);
      })
    );
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/read-all`, {}).pipe(
      tap(() => {
        this.unreadCountSubject.next(0);

        const currentNotifications = this.notificationsSubject.value;
        const updatedNotifications = currentNotifications.map((n) => ({
          ...n,
          read: true,
        }));
        this.notificationsSubject.next(updatedNotifications);
      })
    );
  }

  private subscribeToNotifications(): void {
    if (!this.stompClient) return;

    this.stompClient.subscribe(
      `/user/${this.authService.authenticatedUser?.username}/queue/notifications`,
      (message: any) => {
        console.log('Received private notification:', message.body);
        this.handleNotification(JSON.parse(message.body));
      }
    );

    this.stompClient.subscribe(`/topic/notifications`, (message: any) => {
      console.log('Received public notification:', message.body);
      this.handleNotification(JSON.parse(message.body));
    });
  }

  private sendConnectionMessage(): void {
    if (!this.stompClient) return;

    this.stompClient.send(
      '/app/connect',
      {},
      JSON.stringify({ userId: this.authService.authenticatedUser?.id })
    );
  }

  // Notification Handling
  private handleNotification(payload: any): void {
    if (payload.type === 'NOTIFICATION') {
      const notification = payload.data as Notification;

      if (
        notification.recipient === this.authService.authenticatedUser?.username
      ) {
        console.log('Received personal notification:', notification);

        // Play notification sound for new notifications
        this.playNotificationSound();

        this.latestNotificationSubject.next(notification);

        const currentNotifications = this.notificationsSubject.value;
        this.notificationsSubject.next([notification, ...currentNotifications]);

        if (!notification.read) {
          this.unreadCountSubject.next(this.unreadCountSubject.value + 1);
        }
      }
    }
  }
}
