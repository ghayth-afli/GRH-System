import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MedicalVisit } from '../models/medical-visit';

@Injectable({
  providedIn: 'root',
})
export class MedicalVisitService {
  private apiUrl = `${environment.apiUrl}/medical-visits`;
  private http = inject(HttpClient);

  getMedicalVisits(): Observable<MedicalVisit[]> {
    return this.http.get<MedicalVisit[]>(this.apiUrl);
  }

  getMedicalVisit(id: number): Observable<MedicalVisit> {
    return this.http.get<MedicalVisit>(`${this.apiUrl}/${id}`);
  }

  createMedicalVisit(medicalVisit: {
    doctorName: string;
    visitDate: Date;
    startTime: string;
    endTime: string;
  }): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(this.apiUrl, medicalVisit);
  }

  updateMedicalVisit(
    id: number,
    medicalVisit: Partial<MedicalVisit>
  ): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(
      `${this.apiUrl}/${id}`,
      medicalVisit
    );
  }

  deleteMedicalVisit(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${id}`);
  }
}
