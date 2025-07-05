import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { RequestDocumentDialogComponent } from '../../components/request-document-dialog/request-document-dialog.component';
import { DocumentService } from '../../service/document.service';

@Component({
  selector: 'app-document-requests-page',
  standalone: false,
  templateUrl: './document-requests-page.component.html',
  styleUrl: './document-requests-page.component.css',
})
export class DocumentRequestsPageComponent implements OnInit {
  requests: any[] = [];
  payslips: any[] = [];
  isLoading = true;
  errorMessage: string | null = null;
  isHRD = false; // Mock role; set true for HRD
  currentRequestPage = 1;
  requestPageSize = 5;
  totalRequestItems = 0;
  currentPayslipPage = 1;
  payslipPageSize = 5;
  totalPayslipItems = 0;
  Math = Math; // Expose Math for template

  constructor(
    private documentService: DocumentService,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.loadRequests();
    this.loadPayslips();
  }

  loadRequests() {
    this.isLoading = true;
    this.documentService
      .getDocumentRequests(
        'John Doe',
        this.currentRequestPage,
        this.requestPageSize
      )
      .subscribe({
        next: ({ data, total }) => {
          this.requests = data;
          this.totalRequestItems = total;
          this.isLoading = false;
          console.log('Requests loaded:', data, 'Total:', total);
        },
        error: (err) => {
          this.errorMessage = 'Failed to load requests.';
          this.isLoading = false;
          console.error('Error loading requests:', err);
        },
      });
  }

  loadPayslips() {
    this.documentService
      .getPayslips(this.currentPayslipPage, this.payslipPageSize)
      .subscribe({
        next: ({ data, total }) => {
          this.payslips = data;
          this.totalPayslipItems = total;
          this.isLoading = false;
          console.log('Payslips loaded:', data, 'Total:', total);
        },
        error: (err) => {
          this.errorMessage = 'Failed to load payslips.';
          this.isLoading = false;
          console.error('Error loading payslips:', err);
        },
      });
  }

  openRequestDialog() {
    const dialogRef = this.dialog.open(RequestDocumentDialogComponent, {
      width: '400px',
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.documentService.submitDocumentRequest(result).subscribe({
          next: () => this.loadRequests(),
          error: (err) => console.error('Error submitting request:', err),
        });
      }
    });
  }

  changeRequestPage(page: number) {
    if (
      page >= 1 &&
      page <= Math.ceil(this.totalRequestItems / this.requestPageSize)
    ) {
      this.currentRequestPage = page;
      this.loadRequests();
    }
  }

  changePayslipPage(page: number) {
    if (
      page >= 1 &&
      page <= Math.ceil(this.totalPayslipItems / this.payslipPageSize)
    ) {
      this.currentPayslipPage = page;
      this.loadPayslips();
    }
  }

  getRequestPageNumbers(): number[] {
    const pageCount = Math.ceil(this.totalRequestItems / this.requestPageSize);
    return Array.from({ length: pageCount }, (_, i) => i + 1);
  }

  getPayslipPageNumbers(): number[] {
    const pageCount = Math.ceil(this.totalPayslipItems / this.payslipPageSize);
    return Array.from({ length: pageCount }, (_, i) => i + 1);
  }
}
