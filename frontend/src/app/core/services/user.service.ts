import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ProfilePicture } from '../models/ProfilePicture';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  http = inject(HttpClient);

  getProfilePicture(username: string) {
    return this.http.get<ProfilePicture>(
      `${environment.apiUrl}/employee/profilePicture`,
      {
        params: { username },
      }
    );
  }

  updateEmployeeInfo(
    firstName: string,
    lastName: string,
    email: string,
    jobTitle: string,
    picture?: File
  ): Observable<{ message: string }> {
    const formData = new FormData();
    formData.append('firstName', firstName);
    formData.append('lastName', lastName);
    formData.append('email', email);
    formData.append('jobTitle', jobTitle);
    if (picture) {
      formData.append('picture', picture);
    }

    return this.http.put<{ message: string }>(
      `${environment.apiUrl}/employee/update`,
      formData
    );
  }
}
