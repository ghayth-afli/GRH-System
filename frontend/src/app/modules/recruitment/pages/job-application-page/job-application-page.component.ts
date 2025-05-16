import { Component, ElementRef, inject, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CustomSnackbarComponent } from '../../../../shared/components/custom-snackbar/custom-snackbar.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { JobApplicationService } from '../../services/job-application.service';

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
  jobApllicationService = inject(JobApplicationService);

  constructor(
    private router: Router,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute
  ) {}
  isLoading: boolean = false;
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
    if (this.isLoading) return;
    this.isLoading = true;

    if (this.selectedFile && !this.fileError) {
      const idParam = this.route.snapshot.paramMap.get('id');
      const id = idParam !== null ? Number(idParam) : null;
      if (id === null || isNaN(id)) {
        this.snackBar.openFromComponent(CustomSnackbarComponent, {
          data: { message: 'Invalid job offer ID', type: 'error' },
          duration: 5000,
          panelClass: ['custom-snackbar'],
          horizontalPosition: 'end',
          verticalPosition: 'top',
        });
        this.isLoading = false;
        return;
      }
      this.jobApllicationService
        .createApplication(id, this.selectedFile)
        .subscribe({
          next: () => {
            this.isLoading = false;
            this.snackBar.openFromComponent(CustomSnackbarComponent, {
              data: {
                message: 'Resume uploaded successfully!',
                type: 'success',
              },
              duration: 5000,
              panelClass: ['custom-snackbar'],
              horizontalPosition: 'end',
              verticalPosition: 'top',
            });
            this.router.navigate([`/recruitment/job-offers/${id}`]);
          },
          error: (error) => {
            this.isLoading = false;
            console.error('Error uploading resume:', error);
            this.snackBar.openFromComponent(CustomSnackbarComponent, {
              data: { message: 'Error uploading resume', type: 'error' },
              duration: 5000,
              panelClass: ['custom-snackbar'],
              horizontalPosition: 'end',
              verticalPosition: 'top',
            });
          },
        });
      // const fileDetails = {
      //   name: this.selectedFile.name,
      //   size: this.selectedFile.size,
      //   type: this.selectedFile.type,
      // };
      // console.log('Submitting file:', JSON.stringify(fileDetails, null, 2));
      // this.snackBar.openFromComponent(CustomSnackbarComponent, {
      //   data: { message: 'Resume uploaded successfully!', type: 'success' },
      //   duration: 5000,
      //   panelClass: ['custom-snackbar'],
      //   horizontalPosition: 'end',
      //   verticalPosition: 'top',
      // });
      // // In a real app, send file to backend via HttpClient
      // this.router.navigate(['/recruitment/job-offers']);
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
  onCancel() {
    this.isSubmitted = false;
    this.fileTouched = false;
    this.selectedFile = null;
    this.uploadForm.resetForm();
    const idParam = this.route.snapshot.paramMap.get('id');
    this.router.navigate(['/recruitment/job-offers', idParam]);
  }
}
