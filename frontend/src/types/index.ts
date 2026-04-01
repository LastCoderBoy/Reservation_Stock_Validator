export type Category = 'MEN' | 'WOMEN' | 'KIDS';

export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'EXPIRED' | 'CANCELLED';

export type SortField = 'name' | 'price' | 'stock';

export type SortDirection = 'asc' | 'desc';

// ─── API Wrappers ────────────────────────────────────────────────────────────

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
  size: number;
}

// ─── Product ─────────────────────────────────────────────────────────────────

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  imageKey: string | null;
  availableStock: number;
  totalStock: number;
  category: Category;
  active: boolean;
}

export interface ProductDetail extends Product {
  createdAt: string;
  updatedAt: string;
}

export interface StockResponse {
  productId: number;
  availableStock: number;
  totalStock: number;
  reservedStock: number;
}

export interface ProductFilterParams {
  page?: number;
  size?: number;
  sortBy?: SortField;
  sortDirection?: SortDirection;
  search?: string;
  category?: Category;
  inStock?: boolean;
  minPrice?: number;
  maxPrice?: number;
}

// ─── Reservation ─────────────────────────────────────────────────────────────

export interface Reservation {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  status: ReservationStatus;
  expiresAt: string;
  createdAt: string;
  remainingSeconds: number;
}

export interface CheckoutResponse {
  orderId: number;
  productName: string;
  quantity: number;
  totalPrice: number;
  status: string;
}

export interface CreateReservationRequest {
  productId: number;
  quantity: number;
}

// ─── Auth ─────────────────────────────────────────────────────────────────────

export interface AuthResponse {
  accessToken: string;
  username: string;
  role: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthUser {
  username: string;
  role: string;
}