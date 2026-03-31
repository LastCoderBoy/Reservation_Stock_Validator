// User Types
export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  phoneNumber: string | null;
  role: 'USER' | 'ADMIN';
  createdAt: string;
}

// Product Types
export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  totalStock: number;
  reservedStock: number;
  availableStock: number;
  imageUrl: string | null;
  category: string | null;
  createdAt: string;
  updatedAt: string;
}

// Reservation Types
export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'EXPIRED' | 'CANCELLED';

export interface Reservation {
  id: number;
  userId: number;
  productId: number;
  quantity: number;
  status: ReservationStatus;
  expiresAt: string;
  createdAt: string;
  productName?: string;
  productPrice?: number;
}

// Auth Types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  user: User;
  accessToken: string;
}

// API Response Wrapper
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

// Reservation Request/Response
export interface CreateReservationRequest {
  productId: number;
  quantity: number;
}

export interface ReservationResponse {
  id: number;
  productId: number;
  productName: string;
  productPrice: number;
  quantity: number;
  totalPrice: number;
  status: ReservationStatus;
  expiresAt: string;
  createdAt: string;
}

export interface CheckoutResponse {
  orderId: number;
  reservationId: number;
  totalAmount: number;
  orderDate: string;
  message: string;
}

// Error Types
export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
}
