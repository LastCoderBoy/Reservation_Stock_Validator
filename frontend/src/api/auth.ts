import { api } from './client';
import type { ApiResponse, AuthResponse } from '../types';

export const authApi = {
  login: async (username: string, password: string) => {
    const { data } = await api.post<ApiResponse<AuthResponse>>('/auth/login', {
      username,
      password,
    });
    return data.data;
  },

  logout: async () => {
    await api.post('/auth/logout');
  },

  refreshToken: async () => {
    const { data } = await api.post<ApiResponse<AuthResponse>>('/auth/refresh-token');
    return data.data;
  },
};
