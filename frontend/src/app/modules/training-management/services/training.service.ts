import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Training } from '../models/training';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class TrainingService {
  private readonly BASE_URL = `${environment.apiUrl}/trainings`;

  constructor(private http: HttpClient) {}

  createTraining(training: Training): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(this.BASE_URL, training);
  }

  updateTraining(
    id: number,
    training: Training
  ): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(
      `${this.BASE_URL}/${id}`,
      training
    );
  }

  deleteTraining(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.BASE_URL}/${id}`);
  }

  getAllTrainings(): Observable<Training[]> {
    return this.http.get<Training[]>(this.BASE_URL);
  }

  getTrainingById(id: number): Observable<Training> {
    return this.http.get<Training>(`${this.BASE_URL}/${id}`);
  }
}
