import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { reservationApi } from '../api/reservations';
import type { ReservationStatus } from '../types';

export const RESERVATIONS_QUERY_KEY = 'reservations';

interface ReservationFilterParams {
    page?: number;
    size?: number;
    status?: ReservationStatus;
}

// ─── useMyReservations ────────────────────────────────────────────
export const useMyReservations = (params: ReservationFilterParams) => {
    return useQuery({
        queryKey: [RESERVATIONS_QUERY_KEY, params],
        queryFn: () =>
            reservationApi.getMyReservations({
                page: params.page,
                size: params.size,
                status: params.status,
            }),
        refetchInterval: 10000,   // poll every 10s to catch server-side expiries
        staleTime: 8000,
        retry: (failureCount, error) => {
            // Don't retry on 401 (auth errors) - let the interceptor handle it
            if ((error as { response?: { status?: number } })?.response?.status === 401) {
                return false;
            }
            return failureCount < 3;
        },
    });
};

// ─── useReservationActions ────────────────────────────────────────
export const useReservationActions = () => {
    const queryClient = useQueryClient();

    const invalidate = () =>
        queryClient.invalidateQueries({ queryKey: [RESERVATIONS_QUERY_KEY] });

    const checkoutMutation = useMutation({
        mutationFn: (id: number) => reservationApi.checkout(id),
        onSuccess: invalidate,
    });

    const cancelMutation = useMutation({
        mutationFn: (id: number) => reservationApi.cancel(id),
        onSuccess: invalidate,
    });

    return {
        checkout: checkoutMutation.mutateAsync,
        isCheckingOut: checkoutMutation.isPending,
        checkoutingId: checkoutMutation.variables,

        cancel: cancelMutation.mutateAsync,
        isCancelling: cancelMutation.isPending,
        cancellingId: cancelMutation.variables,
    };
};