import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { JobOfferResponse } from '../models/job-offer-response';
import { Observable } from 'rxjs';
import { JobOfferRequest } from '../models/job-offer-request';
import { EjobOfferStatus } from '../models/EjobOfferStatus';

@Injectable({
  providedIn: 'root',
})
export class JobOfferService {
  private apiUrl = `${environment.apiUrl}/job-offers`;

  constructor(private http: HttpClient) {}

  createJobOffer(jobOffer: JobOfferRequest): Observable<void> {
    return this.http.post<void>(this.apiUrl, jobOffer);
  }

  updateJobOffer(id: number, jobOffer: JobOfferRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}`, jobOffer);
  }

  deleteJobOffer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getJobOfferById(id: number): Observable<JobOfferResponse> {
    return this.http.get<JobOfferResponse>(`${this.apiUrl}/${id}`);
  }

  getAllJobOffers(): Observable<JobOfferResponse[]> {
    return this.http.get<JobOfferResponse[]>(this.apiUrl);
  }

  toggleJobOfferStatus(id: number, status: string): Observable<void> {
    const params = new HttpParams().set('status', status);
    return this.http.put<void>(
      `${this.apiUrl}/${id}/status?status=${status}`,
      null
    );
  }
}
