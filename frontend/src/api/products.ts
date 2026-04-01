import { api } from './client';
import type { ApiResponse, PaginatedResponse, Product, ProductDetail } from '../types';

export const productApi = {
  getAll: async (params?: { page?: number; size?: number; sortBy?: string; sortDir?: string }) => {
    const { data } = await api.get<ApiResponse<PaginatedResponse<Product>>>('/products', { params });
    return data.data;
  },

  getById: async (id: number) => {
    const { data } = await api.get<ApiResponse<ProductDetail>>(`/products/${id}`);
    return data.data;
  },

  getStock: async (id: number) => {
    const { data } = await api.get<ApiResponse<{ availableStock: number }>>(`/products/${id}/stock`);
    return data.data;
  },
};
