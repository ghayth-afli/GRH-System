import { Component } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-confirmation-modal',
  standalone: false,
  templateUrl: './confirmation-modal.component.html',
  styleUrl: './confirmation-modal.component.css',
})
export class ConfirmationModalComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmationModalComponent> //@Inject(MAT_DIALOG_DATA) public data: ConfirmationModalData
  ) {}

  onConfirm() {
    //this.dialogRef.close({ confirmed: true, jobId: this.data.jobId });
  }

  onCancel() {
    this.dialogRef.close({ confirmed: false });
  }
}
