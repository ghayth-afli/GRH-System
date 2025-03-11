import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ApiErrorResponse,
  ForgotPasswordResponse,
  LoginResponse,
  ResetPasswordResponse,
  User,
} from '../models/auth-responses.interface';

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

  // Getter for the authenticated user
  get authenticatedUser(): User | null {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  // login method to get access token and refresh token
  login(username: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, {
        username,
        password,
      })
      .pipe(
        tap({
          next: (response: LoginResponse) => {
            this._setTokens(response);
            this._setUser(response.user);
          },
          error: (error: ApiErrorResponse) => this._handleError(error),
        }),
        catchError((error: ApiErrorResponse) => {
          this._handleError(error);
          return throwError(() => new Error(error.message));
        })
      );
  }

  // Refresh token method to get a new access token and refresh token
  refreshToken(): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/refresh`, {
        refreshToken: this._refreshToken,
      })
      .pipe(
        tap({
          next: (response: LoginResponse) => {
            this._setTokens(response);
            this._setUser(response.user);
          },
          error: (error: ApiErrorResponse) => this._handleError(error),
        }),
        catchError((error: ApiErrorResponse) => {
          this._handleError(error);
          return throwError(() => new Error(error.message));
        })
      );
  }

  // Logout method to clear tokens and user data from local storage
  logout(): void {
    this._clearTokens();
  }

  // Forgot password method to send email with reset password link
  forgotPassword(email: string): Observable<ForgotPasswordResponse> {
    return this.http
      .post<ForgotPasswordResponse>(
        `${environment.apiUrl}/auth/v1/forgot-password`,
        { email }
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
        { password }
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

  private _setUser(user: User): void {
    localStorage.setItem('user', JSON.stringify(user));
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

  // Clear tokens and user data from local storage
  private _clearTokens(): void {
    this._accessToken = null;
    this._refreshToken = null;
    localStorage.removeItem('auth_tokens');
    localStorage.removeItem('user');
  }

  // Handle API errors
  private _handleError(error: ApiErrorResponse): void {
    console.error('API Error:', error);
  }

  // Check if the token has expired
  isTokenExpired(): boolean {
    return Date.now() > this._accessExpiration;
  }

  hasRole(role: string): boolean {
    return localStorage.getItem('user')?.includes(role) ?? false;
  }
}
