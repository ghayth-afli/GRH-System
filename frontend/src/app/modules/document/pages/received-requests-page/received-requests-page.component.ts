import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ProcessRequestDialogComponent } from '../../components/process-request-dialog/process-request-dialog.component';
import { DocumentService } from '../../service/document.service';

@Component({
  selector: 'app-received-requests-page',
  standalone: false,
  templateUrl: './received-requests-page.component.html',
  styleUrl: './received-requests-page.component.css',
})
export class ReceivedRequestsPageComponent implements OnInit {
  requests: any[] = [];
  isLoading = true;
  errorMessage: string | null = null;
  isHRD = true; // Mock HRD role
  currentPage = 1;
  pageSize = 5;
  totalItems = 0;
  Math = Math; // Expose Math for template

  constructor(
    private documentService: DocumentService,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.loadRequests();
  }

  loadRequests() {
    this.isLoading = true;
    this.documentService
      .getAllRequests(this.currentPage, this.pageSize)
      .subscribe({
        next: ({ data, total }) => {
          this.requests = data;
          this.totalItems = total;
          this.isLoading = false;
          console.log('Received requests loaded:', data, 'Total:', total);
        },
        error: (err) => {
          this.errorMessage = 'Failed to load requests.';
          this.isLoading = false;
          console.error('Error loading received requests:', err);
        },
      });
  }

  openProcessDialog(request: any) {
    const dialogRef = this.dialog.open(ProcessRequestDialogComponent, {
      width: '400px',
      data: request,
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.documentService.processRequest(request.id, result).subscribe({
          next: () => this.loadRequests(),
          error: (err) => console.error('Error processing request:', err),
        });
      }
    });
  }

  changePage(page: number) {
    if (page >= 1 && page <= Math.ceil(this.totalItems / this.pageSize)) {
      this.currentPage = page;
      this.loadRequests();
    }
  }

  getPageNumbers(): number[] {
    const pageCount = Math.ceil(this.totalItems / this.pageSize);
    return Array.from({ length: pageCount }, (_, i) => i + 1);
  }
}
