export interface User {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  department: string;
  role: string;
  jobTitle: string;
  phoneNumber1: string;
  phoneNumber2: string;
  gender?: string;
  birthDate?: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  accessExpiration: number;
  refreshExpiration: number;
  user: User;
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
