import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class DocumentService {
  private mockDocuments = [
    {
      id: 1,
      title: 'Company Policy 2025',
      description: 'Updated employee handbook',
      category: 'Policy',
      visibility: 'All',
      fileUrl: 'assets/docs/policy.pdf',
    },
    {
      id: 2,
      title: 'Remote Work Guidelines',
      description: 'Guidelines for remote work',
      category: 'Guideline',
      visibility: 'All',
      fileUrl: 'assets/docs/guidelines.pdf',
    },
    {
      id: 3,
      title: 'Employee Benefits 2025',
      description: 'Benefits overview',
      category: 'Policy',
      visibility: 'All',
      fileUrl: 'assets/docs/benefits.pdf',
    },
    {
      id: 4,
      title: 'Safety Regulations',
      description: 'Workplace safety rules',
      category: 'Guideline',
      visibility: 'All',
      fileUrl: 'assets/docs/safety.pdf',
    },
    {
      id: 5,
      title: 'HR Announcement',
      description: 'New HR updates',
      category: 'Announcement',
      visibility: 'HR',
      fileUrl: 'assets/docs/announcement.pdf',
    },
    {
      id: 6,
      title: 'Code of Conduct',
      description: 'Employee conduct guidelines',
      category: 'Policy',
      visibility: 'All',
      fileUrl: 'assets/docs/conduct.pdf',
    },
  ];

  private mockRequests = [
    {
      id: 1,
      employeeName: 'John Doe',
      documentType: 'Employment Contract',
      notes: 'Need for personal records',
      submissionDate: '2025-06-01',
      status: 'Completed',
      fileUrl: 'assets/docs/contract.pdf',
    },
    {
      id: 2,
      employeeName: 'Jane Smith',
      documentType: 'Work Certificate',
      notes: '',
      submissionDate: '2025-06-15',
      status: 'Pending',
      fileUrl: null,
    },
    {
      id: 3,
      employeeName: 'John Doe',
      documentType: 'Job Description',
      notes: 'For role clarification',
      submissionDate: '2025-06-20',
      status: 'In Progress',
      fileUrl: null,
    },
    {
      id: 4,
      employeeName: 'Jane Smith',
      documentType: 'End-of-Contract Certificate',
      notes: '',
      submissionDate: '2025-06-25',
      status: 'Rejected',
      fileUrl: null,
    },
    {
      id: 5,
      employeeName: 'John Doe',
      documentType: 'Work Certificate',
      notes: 'For visa application',
      submissionDate: '2025-06-28',
      status: 'Completed',
      fileUrl: 'assets/docs/certificate.pdf',
    },
    {
      id: 6,
      employeeName: 'Jane Smith',
      documentType: 'Employment Contract',
      notes: '',
      submissionDate: '2025-06-30',
      status: 'Pending',
      fileUrl: null,
    },
  ];

  private mockPayslips = [
    { id: 1, month: '2025-06', fileUrl: 'assets/payslips/june-2025.pdf' },
    { id: 2, month: '2025-05', fileUrl: 'assets/payslips/may-2025.pdf' },
    { id: 3, month: '2025-04', fileUrl: 'assets/payslips/april-2025.pdf' },
    { id: 4, month: '2025-03', fileUrl: 'assets/payslips/march-2025.pdf' },
    { id: 5, month: '2025-02', fileUrl: 'assets/payslips/february-2025.pdf' },
    { id: 6, month: '2025-01', fileUrl: 'assets/payslips/january-2025.pdf' },
  ];

  getInternalDocuments(
    page: number,
    pageSize: number
  ): Observable<{ data: any[]; total: number }> {
    console.log(
      'DocumentService: Fetching internal documents, page:',
      page,
      'pageSize:',
      pageSize
    );
    const start = (page - 1) * pageSize;
    const end = start + pageSize;
    const paginatedData = this.mockDocuments.slice(start, end);
    return of({ data: paginatedData, total: this.mockDocuments.length }).pipe(
      delay(1000)
    );
  }

  getDocumentRequests(
    employeeName: string,
    page: number,
    pageSize: number
  ): Observable<{ data: any[]; total: number }> {
    console.log(
      'DocumentService: Fetching requests for',
      employeeName,
      'page:',
      page,
      'pageSize:',
      pageSize
    );
    const filteredRequests = this.mockRequests.filter(
      (req) => req.employeeName === employeeName
    );
    const start = (page - 1) * pageSize;
    const end = start + pageSize;
    const paginatedData = filteredRequests.slice(start, end);
    return of({ data: paginatedData, total: filteredRequests.length }).pipe(
      delay(1000)
    );
  }

  getAllRequests(
    page: number,
    pageSize: number
  ): Observable<{ data: any[]; total: number }> {
    console.log(
      'DocumentService: Fetching all requests, page:',
      page,
      'pageSize:',
      pageSize
    );
    const start = (page - 1) * pageSize;
    const end = start + pageSize;
    const paginatedData = this.mockRequests.slice(start, end);
    return of({ data: paginatedData, total: this.mockRequests.length }).pipe(
      delay(1000)
    );
  }

  getPayslips(
    page: number,
    pageSize: number
  ): Observable<{ data: any[]; total: number }> {
    console.log(
      'DocumentService: Fetching payslips, page:',
      page,
      'pageSize:',
      pageSize
    );
    const start = (page - 1) * pageSize;
    const end = start + pageSize;
    const paginatedData = this.mockPayslips.slice(start, end);
    return of({ data: paginatedData, total: this.mockPayslips.length }).pipe(
      delay(1000)
    );
  }

  submitDocumentRequest(request: any): Observable<any> {
    const newRequest = {
      id: this.mockRequests.length + 1,
      ...request,
      submissionDate: new Date().toISOString().split('T')[0],
      status: 'Pending',
      fileUrl: null,
    };
    this.mockRequests.push(newRequest);
    console.log('DocumentService: Submitted request', newRequest);
    return of(newRequest).pipe(delay(1000));
  }

  publishDocument(document: any): Observable<any> {
    const newDocument = { id: this.mockDocuments.length + 1, ...document };
    this.mockDocuments.push(newDocument);
    console.log('DocumentService: Published document', newDocument);
    return of(newDocument).pipe(delay(1000));
  }

  processRequest(requestId: number, updates: any): Observable<any> {
    const request = this.mockRequests.find((req) => req.id === requestId);
    if (request) {
      Object.assign(request, updates);
      console.log('DocumentService: Processed request', request);
    }
    return of(request).pipe(delay(1000));
  }
}
