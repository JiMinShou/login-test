export type Role = "USER" | "ADMIN";

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  role: Role;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  role: Role;
}

export interface UserSummary {
  id: number;
  username: string;
  email: string;
  role: Role;
  createdAt: string;
}

export interface ApiError {
  code: string;
  message: string;
  status: number;
  path: string;
  timestamp: string;
}
