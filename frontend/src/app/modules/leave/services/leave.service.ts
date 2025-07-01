import { Injectable } from '@angular/core';
import { Leave } from '../models/leave';
import { Observable } from 'rxjs';
import { LeaveType } from '../models/leave-type';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { LeaveBalance } from '../models/leave-balance';

@Injectable({
  providedIn: 'root',
})
export class LeaveService {
  private apiUrl = `${environment.apiUrl}/leave`;

  constructor(private http: HttpClient) {}

  getAllLeaveRequests(): Observable<Leave[]> {
    return this.http.get<Leave[]>(`${this.apiUrl}/all`);
  }

  // Get leave balance (Employee role required)
  getLeaveBalance(): Observable<LeaveBalance> {
    return this.http.get<LeaveBalance>(`${this.apiUrl}/balance`);
  }

  // Get leave history (Employee role required)
  getLeaveHistory(): Observable<Leave[]> {
    return this.http.get<Leave[]>(`${this.apiUrl}/myLeaves`);
  }

  // Apply for leave (Employee role required)
  applyLeave({
    leaveType,
    startDate,
    endDate,
    startTime,
    endTime,
    attachment,
  }: {
    leaveType: LeaveType;
    startDate: string;
    endDate: string;
    startTime?: string;
    endTime?: string;
    attachment?: File;
  }): Observable<{ message: string }> {
    const formData = new FormData();
    formData.append('leaveType', leaveType);
    formData.append('startDate', startDate);
    formData.append('endDate', endDate);
    if (startTime) formData.append('startHOURLY', startTime);
    if (endTime) formData.append('endHOURLY', endTime);
    if (attachment) formData.append('attachment', attachment);
    console.log(attachment);
    return this.http.post<any>(`${this.apiUrl}/apply`, formData);
  }

  // Cancel leave (Employee role required)
  cancelLeave(leaveId: number): Observable<{ message: string }> {
    return this.http.put<any>(`${this.apiUrl}/cancel/${leaveId}`, {});
  }

  // Approve leave (Manager role required)
  approveLeave(leaveId: number): Observable<{ message: string }> {
    return this.http.put<any>(`${this.apiUrl}/approve/${leaveId}`, {});
  }

  // Reject leave (Manager role required)
  rejectLeave(leaveId: number): Observable<{ message: string }> {
    return this.http.put<any>(`${this.apiUrl}/reject/${leaveId}`, {});
  }

  // Update leave request (Employee role required)
  updateLeave(
    leaveId: number,
    leaveRequest: Partial<Leave>
  ): Observable<{ message: string }> {
    return this.http.put<any>(`${this.apiUrl}/update/${leaveId}`, leaveRequest);
  }

  // Download received attachment (Manager or HR role required)
  getReceivedAttachment(leaveId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${leaveId}/receivedAttachment`, {
      responseType: 'blob',
    });
  }
}
