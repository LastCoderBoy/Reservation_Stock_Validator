import axiosInstance from './axiosInstance';
import type { ApiResponse, Product, PaginatedResponse } from '../types';

export interface ProductFilters {
  search?: string;
  category?: string;
  minPrice?: number;
  maxPrice?: number;
  inStock?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}

export const productApi = {
  /**
   * Get all products with optional filters
   */
  getProducts: async (filters: ProductFilters = {}): Promise<PaginatedResponse<Product>> => {
    const params = new URLSearchParams();
    
    if (filters.search) params.append('search', filters.search);
    if (filters.category) params.append('category', filters.category);
    if (filters.minPrice !== undefined) params.append('minPrice', filters.minPrice.toString());
    if (filters.maxPrice !== undefined) params.append('maxPrice', filters.maxPrice.toString());
    if (filters.inStock !== undefined) params.append('inStock', filters.inStock.toString());
    if (filters.page !== undefined) params.append('page', filters.page.toString());
    if (filters.size !== undefined) params.append('size', filters.size.toString());
    if (filters.sort) params.append('sort', filters.sort);

    const response = await axiosInstance.get<ApiResponse<PaginatedResponse<Product>>>(
      `/products?${params.toString()}`
    );
    return response.data.data;
  },

  /**
   * Get single product by ID
   */
  getProductById: async (id: number): Promise<Product> => {
    const response = await axiosInstance.get<ApiResponse<Product>>(`/products/${id}`);
    return response.data.data;
  },

  /**
   * Check product stock availability
   */
  checkStock: async (id: number): Promise<{ available: boolean; quantity: number }> => {
    const response = await axiosInstance.get<ApiResponse<{ available: boolean; quantity: number }>>(
      `/products/${id}/stock`
    );
    return response.data.data;
  },
};
