import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Appointment } from '../models/appointment';

@Injectable({
  providedIn: 'root',
})
export class AppointmentService {
  private apiUrl = `${environment.apiUrl}/appointments`;
  private http = inject(HttpClient);

  getAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(this.apiUrl);
  }

  getAppointment(id: number): Observable<Appointment> {
    return this.http.get<Appointment>(`${this.apiUrl}/${id}`);
  }

  getAppointmentsByEmployeeId(employeeId: string): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(
      `${this.apiUrl}/employee/${employeeId}`
    );
  }

  getAppointmentsByMedicalVisitId(
    medVisitId: string
  ): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(
      `${this.apiUrl}/medVisit/${medVisitId}`
    );
  }

  createAppointment(appointment: Appointment): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(this.apiUrl, appointment);
  }

  updateAppointment(
    id: number,
    appointment: Partial<Appointment>
  ): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(
      `${this.apiUrl}/${id}`,
      appointment
    );
  }

  deleteAppointment(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${id}`);
  }
}
