import { Component, OnInit } from '@angular/core';
import { DocumentService } from '../../service/document.service';
import { MatDialog } from '@angular/material/dialog';
import { PublishDocumentDialogComponent } from '../../components/publish-document-dialog/publish-document-dialog.component';

@Component({
  selector: 'app-documents-page',
  standalone: false,
  templateUrl: './documents-page.component.html',
  styleUrl: './documents-page.component.css',
})
export class DocumentsPageComponent implements OnInit {
  documents: any[] = [];
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
    this.loadDocuments();
  }

  loadDocuments() {
    this.isLoading = true;
    this.documentService
      .getInternalDocuments(this.currentPage, this.pageSize)
      .subscribe({
        next: ({ data, total }) => {
          this.documents = data;
          this.totalItems = total;
          this.isLoading = false;
          console.log('Documents loaded:', data, 'Total:', total);
        },
        error: (err) => {
          this.errorMessage = 'Failed to load documents.';
          this.isLoading = false;
          console.error('Error loading documents:', err);
        },
      });
  }

  openPublishDialog() {
    const dialogRef = this.dialog.open(PublishDocumentDialogComponent, {
      width: '400px',
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadDocuments();
      }
    });
  }

  changePage(page: number) {
    if (page >= 1 && page <= Math.ceil(this.totalItems / this.pageSize)) {
      this.currentPage = page;
      this.loadDocuments();
    }
  }

  getPageNumbers(): number[] {
    const pageCount = Math.ceil(this.totalItems / this.pageSize);
    return Array.from({ length: pageCount }, (_, i) => i + 1);
  }
}
