import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { DocumentService } from '../../service/document.service';

@Component({
  selector: 'app-publish-document-dialog',
  standalone: false,
  templateUrl: './publish-document-dialog.component.html',
  styleUrl: './publish-document-dialog.component.css',
})
export class PublishDocumentDialogComponent {
  publishForm: FormGroup;
  categories = ['Policy', 'Guideline', 'Announcement'];
  visibilities = ['All', 'Engineering', 'HR', 'Sales', 'Marketing'];

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<PublishDocumentDialogComponent>,
    private documentService: DocumentService
  ) {
    this.publishForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      category: ['', Validators.required],
      visibility: ['All', Validators.required],
      fileUrl: ['', Validators.required],
    });
  }

  onSubmit() {
    if (this.publishForm.valid) {
      this.documentService.publishDocument(this.publishForm.value).subscribe({
        next: () => this.dialogRef.close(this.publishForm.value),
        error: (err) => console.error('Error publishing document:', err),
      });
    }
  }

  onCancel() {
    this.dialogRef.close();
  }
}
