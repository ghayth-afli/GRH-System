import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, Observable, tap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ApiErrorResponse,
  ForgotPasswordResponse,
  LoginResponse,
  ResetPasswordResponse,
} from '../../features/auth/models/auth-responses.interface';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private _accessToken: string | null = null;
  private _refreshToken: string | null = null;
  private _accessExpiration: number = 0;

  // Inject HttpClient
  constructor(private http: HttpClient) {
    this._loadTokens();
  }

  // Getter for access token
  get accessToken(): string | null {
    return this._accessToken;
  }

  // login method to get access token and refresh token
  login(username: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, {
        username,
        password,
      })
      .pipe(
        tap((response: LoginResponse) => this._setTokens(response)),
        catchError((error: ApiErrorResponse) => {
          this._handleError(error);
          return throwError(() => new Error(error.message));
        })
      );
  }

  // Refresh token method to get new access token and refresh token
  refreshToken(): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/refresh`, {
        refreshToken: this._refreshToken,
      })
      .pipe(
        tap((response: LoginResponse) => this._setTokens(response)),
        catchError((error: ApiErrorResponse) => {
          this._handleError(error);
          return throwError(() => new Error(error.message));
        })
      );
  }

  // Logout method to clear tokens from local storage
  logout(): void {
    this._clearTokens();
  }

  // Forgot password method to send email with reset password link
  forgotPassword(email: string): Observable<ForgotPasswordResponse> {
    return this.http
      .post<ForgotPasswordResponse>(
        `${environment.apiUrl}/auth/v1/forgot-password`,
        {
          email,
        }
      )
      .pipe(
        catchError((error: ApiErrorResponse) => {
          this._handleError(error);
          return throwError(() => new Error(error.message));
        })
      );
  }

  // Reset password with token
  resetPassword(
    token: string,
    password: string
  ): Observable<ResetPasswordResponse> {
    return this.http
      .post<ResetPasswordResponse>(
        `${environment.apiUrl}/auth/reset-password?token=${token}`,
        {
          password,
        }
      )
      .pipe(
        catchError((error: ApiErrorResponse) => {
          this._handleError(error);
          return throwError(() => new Error(error.message));
        })
      );
  }

  // Set tokens to local storage
  private _setTokens(response: LoginResponse): void {
    this._accessToken = response.accessToken;
    this._refreshToken = response.refreshToken;
    this._accessExpiration = Date.now() + response.accessExpiration;

    localStorage.setItem(
      'auth_tokens',
      JSON.stringify({
        accessToken: this._accessToken,
        refreshToken: this._refreshToken,
        accessExpiration: this._accessExpiration,
      })
    );
  }

  // Load tokens from local storage
  private _loadTokens(): void {
    const tokens = localStorage.getItem('auth_tokens');
    if (tokens) {
      const parsed = JSON.parse(tokens);
      this._accessToken = parsed.accessToken;
      this._refreshToken = parsed.refreshToken;
      this._accessExpiration = parsed.accessExpiration;
    }
  }

  // Clear tokens from local storage
  private _clearTokens(): void {
    this._accessToken = null;
    this._refreshToken = null;
    localStorage.removeItem('auth_tokens');
  }

  // Handle API errors
  private _handleError(error: ApiErrorResponse): void {
    console.error('API Error:', error);
  }
}
