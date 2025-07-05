import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-process-request-dialog',
  standalone: false,
  templateUrl: './process-request-dialog.component.html',
  styleUrl: './process-request-dialog.component.css',
})
export class ProcessRequestDialogComponent {
  processForm: FormGroup;
  statuses = ['Pending', 'In Progress', 'Completed', 'Rejected'];

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ProcessRequestDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.processForm = this.fb.group({
      status: [data.status || 'Pending'],
      notes: [data.notes || ''],
      fileUrl: [data.fileUrl || ''],
    });
  }

  onSubmit() {
    if (this.processForm.valid) {
      this.dialogRef.close(this.processForm.value);
    }
  }

  onCancel() {
    this.dialogRef.close();
  }
}
