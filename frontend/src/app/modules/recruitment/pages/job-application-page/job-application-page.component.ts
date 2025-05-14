import { Component, ElementRef, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-job-application-page',
  standalone: false,
  templateUrl: './job-application-page.component.html',
  styleUrl: './job-application-page.component.css',
})
export class JobApplicationPageComponent {
  @ViewChild('resumeInput') resumeInput!: ElementRef<HTMLInputElement>;
  @ViewChild('uploadForm') uploadForm!: NgForm;
  selectedFile: File | null = null;
  fileError: 'type' | 'size' | null = null;
  isDragging = false;
  fileTouched = false;
  isSubmitted = false;
  readonly MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes

  constructor(private router: Router, private snackBar: MatSnackBar) {}

  triggerFileInput() {
    this.resumeInput.nativeElement.click();
  }

  onFileSelect(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.processFile(input.files[0]);
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragEnter(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      this.processFile(event.dataTransfer.files[0]);
    }
  }

  processFile(file: File) {
    this.fileTouched = true;
    this.selectedFile = file;
    this.fileError = null;

    if (file.type !== 'application/pdf') {
      this.fileError = 'type';
      this.selectedFile = null;
      this.snackBar.openFromComponent(CustomSnackbarComponent, {
        data: { message: 'Only PDF files are allowed', type: 'error' },
        duration: 5000,
        panelClass: ['custom-snackbar'],
        horizontalPosition: 'end',
        verticalPosition: 'top',
      });
    } else if (file.size > this.MAX_FILE_SIZE) {
      this.fileError = 'size';
      this.selectedFile = null;
      this.snackBar.openFromComponent(CustomSnackbarComponent, {
        data: { message: 'File size must not exceed 10MB', type: 'error' },
        duration: 5000,
        panelClass: ['custom-snackbar'],
        horizontalPosition: 'end',
        verticalPosition: 'top',
      });
    }
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  onSubmit() {
    this.isSubmitted = true;
    this.fileTouched = true;

    if (this.selectedFile && !this.fileError) {
      const fileDetails = {
        name: this.selectedFile.name,
        size: this.selectedFile.size,
        type: this.selectedFile.type,
      };
      console.log('Submitting file:', JSON.stringify(fileDetails, null, 2));
      this.snackBar.openFromComponent(CustomSnackbarComponent, {
        data: { message: 'Resume uploaded successfully!', type: 'success' },
        duration: 5000,
        panelClass: ['custom-snackbar'],
        horizontalPosition: 'end',
        verticalPosition: 'top',
      });
      // In a real app, send file to backend via HttpClient
      this.router.navigate(['/recruitment/job-offers']);
    } else {
      this.snackBar.openFromComponent(CustomSnackbarComponent, {
        data: { message: 'Please upload a valid PDF file', type: 'error' },
        duration: 5000,
        panelClass: ['custom-snackbar'],
        horizontalPosition: 'end',
        verticalPosition: 'top',
      });
    }
  }
}
