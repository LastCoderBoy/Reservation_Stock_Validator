import axiosInstance from './axiosInstance';
import type {
  ApiResponse,
  CreateReservationRequest,
  ReservationResponse,
  CheckoutResponse,
  ReservationStatus,
  PaginatedResponse,
} from '../types';

export interface ReservationFilters {
  status?: ReservationStatus;
  page?: number;
  size?: number;
}

export const reservationApi = {
  /**
   * Create a new reservation
   */
  createReservation: async (request: CreateReservationRequest): Promise<ReservationResponse> => {
    const response = await axiosInstance.post<ApiResponse<ReservationResponse>>(
      '/reservations',
      request
    );
    return response.data.data;
  },

  /**
   * Get user's reservations with optional filters
   */
  getUserReservations: async (filters: ReservationFilters = {}): Promise<PaginatedResponse<ReservationResponse>> => {
    const params = new URLSearchParams();
    
    if (filters.status) params.append('status', filters.status);
    if (filters.page !== undefined) params.append('page', filters.page.toString());
    if (filters.size !== undefined) params.append('size', filters.size.toString());

    const response = await axiosInstance.get<ApiResponse<PaginatedResponse<ReservationResponse>>>(
      `/reservations/me?${params.toString()}`
    );
    return response.data.data;
  },

  /**
   * Get single reservation by ID
   */
  getReservationById: async (id: number): Promise<ReservationResponse> => {
    const response = await axiosInstance.get<ApiResponse<ReservationResponse>>(
      `/reservations/${id}`
    );
    return response.data.data;
  },

  /**
   * Complete checkout for a reservation
   */
  checkout: async (id: number): Promise<CheckoutResponse> => {
    const response = await axiosInstance.post<ApiResponse<CheckoutResponse>>(
      `/reservations/${id}/checkout`
    );
    return response.data.data;
  },

  /**
   * Cancel a reservation
   */
  cancelReservation: async (id: number): Promise<void> => {
    await axiosInstance.post(`/reservations/${id}/cancel`);
  },
};
