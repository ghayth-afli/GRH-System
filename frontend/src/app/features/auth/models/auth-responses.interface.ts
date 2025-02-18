export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  accessExpiration: number;
  refreshExpiration: number;
}

export interface ForgotPasswordResponse {
  message: string;
}

export interface ResetPasswordResponse {
  message: string;
}

export interface ApiErrorResponse {
  message: string;
}
