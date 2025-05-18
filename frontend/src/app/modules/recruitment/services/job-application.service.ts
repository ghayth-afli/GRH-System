import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApplicationDetailsResponseDTO,
  ApplicationResponseDTO,
} from '../models/application-response';

@Injectable({
  providedIn: 'root',
})
export class JobApplicationService {
  private baseUrl = `${environment.apiUrl}/internal-applications`;

  constructor(private http: HttpClient) {}

  /**
   * Creates a new job application for a specific job offer
   * @param jobOfferId - ID of the job offer
   * @param resume - Resume file to be uploaded
   * @returns Observable that completes when the operation is done
   */
  createApplication(jobOfferId: number, resume: File): Observable<void> {
    const formData = new FormData();
    formData.append('resume', resume);

    return this.http.post<void>(
      `${this.baseUrl}/job-offer/${jobOfferId}`,
      formData
    );
  }

  /**
   * Deletes an application by its ID (HR only)
   * @param applicationId - ID of the application to delete
   * @returns Observable that completes when deletion is done
   */
  deleteApplication(applicationId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${applicationId}`);
  }

  /**
   * Gets an application by its ID (HR and Manager only)
   * @param applicationId - ID of the application to retrieve
   * @returns Observable with the application data
   */
  getApplicationById(
    applicationId: number
  ): Observable<ApplicationResponseDTO> {
    return this.http.get<ApplicationResponseDTO>(
      `${this.baseUrl}/${applicationId}`
    );
  }

  /**
   * Gets all applications for a specific job offer
   * @param jobId - ID of the job offer to get applications for
   * @returns Observable with a list of applications
   */
  getAllApplications(jobId: number): Observable<ApplicationResponseDTO[]> {
    return this.http.get<ApplicationResponseDTO[]>(
      `${this.baseUrl}/job-offer/${jobId}`
    );
  }

  /**
   * Gets detailed information about an application (HR and Manager only)
   * @param applicationId - ID of the application to get details for
   * @returns Observable with detailed application data
   */
  getApplicationDetails(
    applicationId: number
  ): Observable<ApplicationDetailsResponseDTO> {
    return this.http.get<ApplicationDetailsResponseDTO>(
      `${this.baseUrl}/details/${applicationId}`
    );
  }

  /**
   * Updates the status of an application (HR only)
   * @param applicationId - ID of the application to update
   * @param status - New status for the application
   * @returns Observable that completes when the operation is done
   */
  updateApplicationStatus(
    applicationId: number,
    status: string
  ): Observable<void> {
    const params = new HttpParams().set('status', status);
    return this.http.put<void>(
      `${this.baseUrl}/${applicationId}/status?status=${status}`,
      null
    );
  }
}
