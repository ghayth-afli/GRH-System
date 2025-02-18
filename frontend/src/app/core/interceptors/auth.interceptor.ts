import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import {
  BehaviorSubject,
  catchError,
  filter,
  Observable,
  switchMap,
  take,
  throwError,
} from 'rxjs';
import { AuthService } from '../services/auth.service';
import { Injectable } from '@angular/core';
import { ApiErrorResponse } from '../../features/auth/models/auth-responses.interface';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;

  // BehaviorSubject to store the new token value when refreshing the token and to emit the new token value to the subscribers
  private refreshTokenSubject = new BehaviorSubject<string | null>(null);

  // Inject AuthService
  constructor(private authService: AuthService) {}

  // Intercept method to add the Authorization header to the request
  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    request = this._addAuthorizationHeader(request);

    return next.handle(request).pipe(
      catchError((err) => {
        if (err.status === 401) {
          return this._handle401Error(request, next);
        }

        return throwError(() => new Error(err.error.message));
      })
    );
  }

  // Method to add the Authorization header to the request
  private _addAuthorizationHeader(request: HttpRequest<any>): HttpRequest<any> {
    if (this.authService.accessToken) {
      return request.clone({
        setHeaders: { Authorization: `Bearer ${this.authService.accessToken}` },
      });
    }

    return request;
  }

  // Method to handle 401 error and refresh the token
  private _handle401Error(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    // If the token is already refreshing, wait for the new token to be emitted
    if (this.isRefreshing) {
      return this.refreshTokenSubject.pipe(
        filter((token) => !!token),
        take(1),
        switchMap(() => next.handle(this._addAuthorizationHeader(request)))
      );
    }

    // If the token is not refreshing, refresh the token
    this.isRefreshing = true;
    this.refreshTokenSubject.next(null);

    return this.authService.refreshToken().pipe(
      switchMap(() => {
        this.isRefreshing = false;
        this.refreshTokenSubject.next(this.authService.accessToken);

        return next.handle(this._addAuthorizationHeader(request));
      }),
      catchError((error: ApiErrorResponse) => {
        this.isRefreshing = false;
        this.authService.logout();

        return throwError(() => new Error(error.message));
      })
    );
  }
}
