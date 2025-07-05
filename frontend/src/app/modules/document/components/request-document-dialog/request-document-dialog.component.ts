import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-request-document-dialog',
  standalone: false,
  templateUrl: './request-document-dialog.component.html',
  styleUrl: './request-document-dialog.component.css',
})
export class RequestDocumentDialogComponent {
  requestForm: FormGroup;
  documentTypes = [
    'Employment Contract',
    'Job Description',
    'Work Certificate',
    'End-of-Contract Certificate',
  ];

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<RequestDocumentDialogComponent>
  ) {
    this.requestForm = this.fb.group({
      documentType: ['', Validators.required],
      notes: [''],
    });
  }

  onSubmit() {
    if (this.requestForm.valid) {
      this.dialogRef.close({
        employeeName: 'John Doe', // Mock employee
        ...this.requestForm.value,
      });
    }
  }

  onCancel() {
    this.dialogRef.close();
  }
}
