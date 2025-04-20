import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Invitation } from '../models/invitation';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class InvitationService {
  private readonly BASE_URL = `${environment.apiUrl}/invitations`;

  constructor(private http: HttpClient) {}

  getAllInvitations(): Observable<Invitation[]> {
    return this.http.get<Invitation[]>(this.BASE_URL);
  }

  confirmInvitation(id: number): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(
      `${this.BASE_URL}/confirm/${id}`,
      {}
    );
  }

  rejectInvitation(id: number): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(
      `${this.BASE_URL}/reject/${id}`,
      {}
    );
  }
}
