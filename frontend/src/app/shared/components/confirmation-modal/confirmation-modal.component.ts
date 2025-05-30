import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
export interface ConfirmationModalData {
  title: string;
  message: string;
  confirmButtonText?: string;
  cancelButtonText?: string;
}
@Component({
  selector: 'app-confirmation-modal',
  standalone: false,
  templateUrl: './confirmation-modal.component.html',
  styleUrl: './confirmation-modal.component.css',
})
export class ConfirmationModalComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmationModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmationModalData
  ) {}

  onConfirm() {
    console.log(this.data);
    this.dialogRef.close({ confirmed: true });
  }

  onCancel() {
    this.dialogRef.close({ confirmed: false });
  }
}
