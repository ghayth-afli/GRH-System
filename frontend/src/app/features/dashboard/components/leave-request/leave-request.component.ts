import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-leave-request',
  templateUrl: './leave-request.component.html',
  styleUrl: './leave-request.component.css',
})
export class LeaveRequestComponent {
  selectedFile: File | null = null;

  constructor(
    public dialogRef: MatDialogRef<LeaveRequestComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  onSubmit() {
    this.dialogRef.close({ ...this.data, attachment: this.selectedFile });
  }

  onCancel() {
    this.dialogRef.close();
  }
}
