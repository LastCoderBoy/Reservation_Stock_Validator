import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Separate axios instance for refresh token requests to avoid interceptor loop
const refreshApi = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Track if a refresh is in progress to prevent concurrent refresh calls
let isRefreshing = false;
let refreshFailed = false; // Prevent retry loops after refresh failure
let refreshSubscribers: ((token: string) => void)[] = [];

const subscribeTokenRefresh = (callback: (token: string) => void) => {
  refreshSubscribers.push(callback);
};

const onTokenRefreshed = (token: string) => {
  refreshSubscribers.forEach((callback) => callback(token));
  refreshSubscribers = [];
};

const clearAuthAndReject = (error: unknown) => {
  localStorage.removeItem('accessToken');
  refreshFailed = true;
  // Don't redirect here - let the app handle it naturally
  return Promise.reject(error);
};

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Don't retry refresh-token requests to prevent infinite loop
    if (originalRequest.url?.includes('/auth/refresh-token')) {
      return Promise.reject(error);
    }

    // Don't retry if refresh already failed this session
    if (refreshFailed) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      // Skip refresh if no access token exists (user not logged in)
      if (!localStorage.getItem('accessToken')) {
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // Wait for the ongoing refresh to complete
        return new Promise((resolve, reject) => {
          subscribeTokenRefresh((token: string) => {
            if (token) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
              resolve(api(originalRequest));
            } else {
              reject(error);
            }
          });
        });
      }

      isRefreshing = true;

      try {
        const { data } = await refreshApi.post('/auth/refresh-token');
        const newToken = data.data.accessToken;
        localStorage.setItem('accessToken', newToken);
        refreshFailed = false; // Reset on successful refresh
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        onTokenRefreshed(newToken);
        return api(originalRequest);
      } catch {
        // Clear subscribers with empty token to signal failure
        refreshSubscribers.forEach((callback) => callback(''));
        refreshSubscribers = [];
        return clearAuthAndReject(error);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// Export function to reset refresh state (call on login/logout)
export const resetRefreshState = () => {
  refreshFailed = false;
  isRefreshing = false;
  refreshSubscribers = [];
};
