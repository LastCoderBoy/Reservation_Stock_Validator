import { api } from './client';
import type { ApiResponse, PaginatedResponse, Reservation, CheckoutResponse } from '../types';

export const reservationApi = {
  create: async (productId: number, quantity: number) => {
    const { data } = await api.post<ApiResponse<Reservation>>('/reservations', {
      productId,
      quantity,
    });
    return data.data;
  },

  getMyReservations: async (params?: { page?: number; size?: number; status?: string }) => {
    const { data } = await api.get<ApiResponse<PaginatedResponse<Reservation>>>('/reservations/me', { params });
    return data.data;
  },

  getById: async (id: number) => {
    const { data } = await api.get<ApiResponse<Reservation>>(`/reservations/${id}`);
    return data.data;
  },

  checkout: async (id: number) => {
    const { data } = await api.post<ApiResponse<CheckoutResponse>>(`/reservations/${id}/checkout`);
    return data.data;
  },

  cancel: async (id: number) => {
    const { data } = await api.post<ApiResponse<void>>(`/reservations/${id}/cancel`);
    return data;
  },
};
