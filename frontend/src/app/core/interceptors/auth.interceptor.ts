import {
  HttpEvent,
  HttpHandlerFn,
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
import { inject } from '@angular/core';
import { ApiErrorResponse } from '../models/auth-responses.interface';

// Global variables to handle token refresh state
let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (
  request: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const authService = inject(AuthService);

  // Add authorization header to the request
  request = addAuthorizationHeader(request, authService);

  return next(request).pipe(
    catchError((err) => {
      if (err.status === 401) {
        return handle401Error(request, next, authService);
      }

      return throwError(() => new Error(err.error.message));
    })
  );
};

// Helper function to add the Authorization header to the request
function addAuthorizationHeader(
  request: HttpRequest<unknown>,
  authService: AuthService
): HttpRequest<unknown> {
  if (authService.accessToken) {
    return request.clone({
      setHeaders: { Authorization: `Bearer ${authService.accessToken}` },
    });
  }

  return request;
}

// Helper function to handle 401 error and refresh the token
function handle401Error(
  request: HttpRequest<unknown>,
  next: HttpHandlerFn,
  authService: AuthService
): Observable<HttpEvent<unknown>> {
  // If the token is already refreshing, wait for the new token to be emitted
  if (isRefreshing) {
    return refreshTokenSubject.pipe(
      filter((token) => !!token),
      take(1),
      switchMap(() => next(addAuthorizationHeader(request, authService)))
    );
  }

  // If the token is not refreshing, refresh the token
  isRefreshing = true;
  refreshTokenSubject.next(null);

  return authService.refreshToken().pipe(
    switchMap(() => {
      isRefreshing = false;
      refreshTokenSubject.next(authService.accessToken);

      return next(addAuthorizationHeader(request, authService));
    }),
    catchError((error: ApiErrorResponse) => {
      isRefreshing = false;
      authService.logout();

      return throwError(() => new Error(error.message));
    })
  );
}
