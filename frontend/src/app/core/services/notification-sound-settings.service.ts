import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class NotificationSoundSettingsService {
  private soundEnabledSubject = new BehaviorSubject<boolean>(true);
  soundEnabled$ = this.soundEnabledSubject.asObservable();

  private volumeSubject = new BehaviorSubject<number>(0.5); // Default volume 50%
  volume$ = this.volumeSubject.asObservable();

  constructor() {
    // Load saved preferences from localStorage if available
    this.loadSettings();
  }

  toggleSound(enabled: boolean): void {
    this.soundEnabledSubject.next(enabled);
    localStorage.setItem('notification_sound_enabled', JSON.stringify(enabled));
  }

  setVolume(volume: number): void {
    // Ensure volume is between 0 and 1
    const safeVolume = Math.max(0, Math.min(1, volume));
    this.volumeSubject.next(safeVolume);
    localStorage.setItem(
      'notification_sound_volume',
      JSON.stringify(safeVolume)
    );
  }

  private loadSettings(): void {
    try {
      const soundEnabled = localStorage.getItem('notification_sound_enabled');
      const volume = localStorage.getItem('notification_sound_volume');

      if (soundEnabled !== null) {
        this.soundEnabledSubject.next(JSON.parse(soundEnabled));
      }

      if (volume !== null) {
        this.volumeSubject.next(JSON.parse(volume));
      }
    } catch (error) {
      console.error('Error loading notification sound settings:', error);
    }
  }
}
