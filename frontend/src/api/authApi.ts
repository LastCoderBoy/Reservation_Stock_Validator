import axiosInstance from './axiosInstance';
import type {
  ApiResponse,
  LoginRequest,
  LoginResponse,
  User,
} from '../types';

export const authApi = {
  /**
   * Login with username and password
   */
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await axiosInstance.post<ApiResponse<LoginResponse>>(
      '/auth/login',
      credentials
    );
    return response.data.data;
  },

  /**
   * Refresh access token using the refresh token cookie
   */
  refreshToken: async (): Promise<string> => {
    const response = await axiosInstance.post<ApiResponse<{ accessToken: string }>>(
      '/auth/refresh-token'
    );
    return response.data.data.accessToken;
  },

  /**
   * Logout current user
   */
  logout: async (): Promise<void> => {
    await axiosInstance.post('/auth/logout');
  },

  /**
   * Get current user profile
   */
  getCurrentUser: async (): Promise<User> => {
    const response = await axiosInstance.get<ApiResponse<User>>('/users/me');
    return response.data.data;
  },
};
