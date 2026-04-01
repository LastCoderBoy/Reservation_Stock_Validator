import { useQuery } from '@tanstack/react-query';
import { reservationApi } from '../api/reservations';
import { useAuth } from '../contexts/AuthContext';

export const usePendingCount = () => {
    const { isAuthenticated } = useAuth();

    const { data } = useQuery({
        queryKey: ['reservations', 'pending-count'],
        queryFn: () =>
            reservationApi.getMyReservations({ page: 0, size: 1, status: 'PENDING' }),
        enabled: isAuthenticated,   // only runs when logged in
        refetchInterval: 30_000,    // poll every 30s
        staleTime: 25_000,
    });

    return data?.totalElements ?? 0;
};