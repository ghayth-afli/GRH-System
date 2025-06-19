import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { AttendanceRecord } from '../models/attendance-record';
import { Exception } from '../models/exception';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class AttendanceService {
  private apiUrl = `${environment.apiUrl}/attendances`;

  constructor(private http: HttpClient) {}

  getAttendanceRecords(date?: string): Observable<AttendanceRecord[]> {
    let params = {
      date: date || '',
    };
    return this.http.get<AttendanceRecord[]>(
      `${this.apiUrl}/attendance-records`,
      {
        params,
      }
    );
  }
  getExceptions() {}
}
