import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { productApi } from '../api/products';
import { reservationApi } from '../api/reservations';
import { PRODUCTS_QUERY_KEY } from './useProducts';

// ─── useProduct ───────────────────────────────────────────────────
// Fetches single product detail. Polls stock every 5s.
export const useProduct = (id: number) => {
    return useQuery({
        queryKey: [PRODUCTS_QUERY_KEY, id],
        queryFn: () => productApi.getById(id),
        refetchInterval: 5000,
        staleTime: 4000,
        enabled: !!id,
    });
};

// ─── useProductStock ──────────────────────────────────────────────
// Lightweight stock-only polling — used to refresh badge without full refetch.
export const useProductStock = (id: number) => {
    return useQuery({
        queryKey: [PRODUCTS_QUERY_KEY, id, 'stock'],
        queryFn: () => productApi.getStock(id),
        refetchInterval: 5000,
        staleTime: 4000,
        enabled: !!id,
    });
};

// ─── useReservation ───────────────────────────────────────────────
export const useReservation = () => {
    const queryClient = useQueryClient();

    const createMutation = useMutation({
        mutationFn: ({ productId, quantity }: { productId: number; quantity: number }) =>
            reservationApi.create(productId, quantity),
        onSuccess: () => {
            // Invalidate stock so UI reflects the reservation immediately
            queryClient.invalidateQueries({ queryKey: [PRODUCTS_QUERY_KEY] });
        },
    });

    const checkoutMutation = useMutation({
        mutationFn: (reservationId: number) => reservationApi.checkout(reservationId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [PRODUCTS_QUERY_KEY] });
            queryClient.invalidateQueries({ queryKey: ['reservations'] });
        },
    });

    const cancelMutation = useMutation({
        mutationFn: (reservationId: number) => reservationApi.cancel(reservationId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: [PRODUCTS_QUERY_KEY] });
            queryClient.invalidateQueries({ queryKey: ['reservations'] });
        },
    });

    return {
        // Create
        reserve: createMutation.mutateAsync,
        isReserving: createMutation.isPending,
        reserveError: createMutation.error,

        // Checkout
        checkout: checkoutMutation.mutateAsync,
        isCheckingOut: checkoutMutation.isPending,
        checkoutError: checkoutMutation.error,
        checkoutData: checkoutMutation.data,

        // Cancel
        cancel: cancelMutation.mutateAsync,
        isCancelling: cancelMutation.isPending,

        // Reset errors
        resetReserve: createMutation.reset,
        resetCheckout: checkoutMutation.reset,
    };
};