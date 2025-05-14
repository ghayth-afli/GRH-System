import { Component, Inject } from '@angular/core';
import {
  MAT_SNACK_BAR_DATA,
  MatSnackBarRef,
} from '@angular/material/snack-bar';

@Component({
  selector: 'app-custom-snackbar',
  standalone: false,
  templateUrl: './custom-snackbar.component.html',
  styleUrl: './custom-snackbar.component.css',
})
export class CustomSnackbarComponent {
  constructor(
    public snackBarRef: MatSnackBarRef<CustomSnackbarComponent>,
    @Inject(MAT_SNACK_BAR_DATA)
    public data: {
      message: string;
      type: 'success' | 'error' | 'warning' | 'info';
    }
  ) {}

  getIconClass(): string {
    switch (this.data.type) {
      case 'success':
        return 'fa-solid fa-check-circle';
      case 'error':
        return 'fa-solid fa-exclamation-circle';
      case 'warning':
        return 'fa-solid fa-exclamation-triangle';
      case 'info':
        return 'fa-solid fa-info-circle';
      default:
        return '';
    }
  }

  dismiss() {
    this.snackBarRef.dismiss();
  }
}
