import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { ApiResponse, ApiError } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

// Create Axios instance
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Include cookies for refresh token
});

// Request interceptor - Add JWT token to headers
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Extract error messages from backend
axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError<ApiResponse<null>>) => {
    const apiError: ApiError = {
      message: 'An unexpected error occurred',
      status: error.response?.status || 500,
      timestamp: new Date().toISOString(),
    };

    if (error.response) {
      // Extract backend error message
      const backendMessage = error.response.data?.message;
      if (backendMessage) {
        apiError.message = backendMessage;
      } else {
        // Fallback to HTTP status messages
        switch (error.response.status) {
          case 401:
            apiError.message = 'Authentication required. Please login.';
            // Clear token and redirect to login
            localStorage.removeItem('accessToken');
            if (window.location.pathname !== '/login') {
              window.location.href = `/login?returnUrl=${encodeURIComponent(window.location.pathname)}`;
            }
            break;
          case 403:
            apiError.message = 'Access denied. You do not have permission.';
            break;
          case 404:
            apiError.message = 'Resource not found.';
            break;
          case 409:
            apiError.message = 'Conflict. Resource already exists.';
            break;
          case 422:
            apiError.message = 'Validation failed. Please check your input.';
            break;
          case 500:
            apiError.message = 'Server error. Please try again later.';
            break;
          default:
            apiError.message = `Error: ${error.response.statusText}`;
        }
      }
    } else if (error.request) {
      // Network error
      apiError.message = 'Network error. Please check your connection.';
    }

    return Promise.reject(apiError);
  }
);

export default axiosInstance;
