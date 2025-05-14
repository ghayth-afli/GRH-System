import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { JobOfferFilterParams } from '../models/job-offer-filter-params';
import { JobOfferResponse } from '../models/job-offer-response';
import { Observable } from 'rxjs';
import { JobOffer } from '../models/job-offer';
import { JobOfferRequest } from '../models/job-offer-request';

@Injectable({
  providedIn: 'root',
})
export class JobOfferService {
  private apiUrl = `${environment.apiUrl}/job-offers`;

  constructor(private http: HttpClient) {}

  /**
   * Gets all job offers with pagination and filtering
   */
  getAllJobOffers(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'id',
    sortDir: string = 'asc',
    filters?: {
      title?: string;
      department?: string;
      role?: string;
      isInternal?: boolean;
      status?: string;
    }
  ): Observable<JobOfferResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    return this.http.get<JobOfferResponse>(this.apiUrl, { params });
  }

  /**
   * Gets a specific job offer by its ID
   */
  getJobOfferById(id: number): Observable<JobOffer> {
    return this.http.get<JobOffer>(`${this.apiUrl}/${id}`);
  }

  /**
   * Creates a new job offer
   */
  createJobOffer(
    jobOffer: Omit<JobOffer, 'id' | 'createdAt' | 'updatedAt'>
  ): Observable<JobOffer> {
    return this.http.post<JobOffer>(this.apiUrl, jobOffer);
  }

  /**
   * Updates an existing job offer
   */
  updateJobOffer(
    id: number,
    jobOffer: Omit<JobOffer, 'id' | 'createdAt' | 'updatedAt'>
  ): Observable<JobOffer> {
    return this.http.put<JobOffer>(`${this.apiUrl}/${id}`, jobOffer);
  }

  /**
   * Deletes a job offer
   */
  deleteJobOffer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Toggle job offer between internal and external
   * Note: This would require a separate endpoint on your backend
   * or you can use the update method to change the isInternal field
   */
  toggleInternalExternal(
    id: number,
    currentStatus: boolean
  ): Observable<JobOffer> {
    return this.updateJobOffer(id, { isInternal: !currentStatus } as any);
  }

  /**
   * Change job offer status (OPEN/CLOSED)
   * Note: This would require a separate endpoint on your backend
   * or you can use the update method to change the status field
   */
  changeJobOfferStatus(
    id: number,
    newStatus: 'OPEN' | 'CLOSED'
  ): Observable<JobOffer> {
    return this.updateJobOffer(id, { status: newStatus } as any);
  }
}
