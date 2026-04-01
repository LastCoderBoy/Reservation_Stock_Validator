import { useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useMyReservations, useReservationActions } from '../hooks/useReservations';
import { ReservationCard } from '../components/reservation/ReservationCard';
import { Pagination, Spinner, ErrorMessage } from '../components/ui';
import type { ReservationStatus } from '../types';

// ─── Status filter tabs ───────────────────────────────────────────
const STATUS_TABS: { label: string; value: ReservationStatus | undefined }[] = [
    { label: 'All',       value: undefined },
    { label: 'Pending',   value: 'PENDING' },
    { label: 'Confirmed', value: 'CONFIRMED' },
    { label: 'Expired',   value: 'EXPIRED' },
    { label: 'Cancelled', value: 'CANCELLED' },
];

const PAGE_SIZE = 8;

export const ReservationsPage = () => {
    const { isAuthenticated, user } = useAuth();
    const [activeStatus, setActiveStatus] = useState<ReservationStatus | undefined>(undefined);
    const [page, setPage] = useState(0);

    const { checkout, cancel, isCheckingOut, isCancelling, checkoutingId, cancellingId } =
        useReservationActions();

    const { data, isLoading, isError, refetch } = useMyReservations({
        page,
        size: PAGE_SIZE,
        status: activeStatus,
    });

    // ── Guard — must be logged in ─────────────────────────────────
    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: '/reservations' }} replace />;
    }

    const reservations = data?.content ?? [];
    const totalPages = data?.totalPages ?? 0;
    const totalElements = data?.totalElements ?? 0;
    const currentPage = data?.currentPage ?? 0;

    const handleTabChange = (status: ReservationStatus | undefined) => {
        setActiveStatus(status);
        setPage(0);
    };

    const pendingCount = reservations.filter(r => r.status === 'PENDING').length;

    return (
        <div className="min-h-screen bg-gray-50">

            {/* ── Header ─────────────────────────────────────────────── */}
            <div className="bg-white border-b border-gray-100">
                <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
                    <div className="flex items-center justify-between flex-wrap gap-3">
                        <div>
                            <h1 className="text-2xl font-bold text-gray-900">My Reservations</h1>
                            <p className="text-sm text-gray-400 mt-0.5">
                                {user?.username} · {totalElements} total
                            </p>
                        </div>
                        <Link
                            to="/products"
                            className="text-sm text-indigo-600 hover:text-indigo-800 font-medium transition-colors"
                        >
                            ← Browse products
                        </Link>
                    </div>

                    {/* ── Status tabs ──────────────────────────────────── */}
                    <div className="flex items-center gap-1 mt-5 overflow-x-auto pb-px">
                        {STATUS_TABS.map(tab => (
                            <button
                                key={tab.label}
                                onClick={() => handleTabChange(tab.value)}
                                className={[
                                    'flex-shrink-0 px-4 py-1.5 text-sm font-medium rounded-lg transition-colors',
                                    activeStatus === tab.value
                                        ? 'bg-indigo-600 text-white'
                                        : 'text-gray-500 hover:text-gray-800 hover:bg-gray-100',
                                ].join(' ')}
                            >
                                {tab.label}
                                {tab.value === 'PENDING' && pendingCount > 0 && (
                                    <span className="ml-1.5 inline-flex items-center justify-center
                                   w-4 h-4 rounded-full bg-amber-400 text-white
                                   text-[10px] font-bold">
                    {pendingCount}
                  </span>
                                )}
                            </button>
                        ))}
                    </div>
                </div>
            </div>

            {/* ── Body ───────────────────────────────────────────────── */}
            <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

                {/* Loading */}
                {isLoading && (
                    <div className="flex items-center justify-center py-24">
                        <Spinner size="lg" />
                    </div>
                )}

                {/* Error */}
                {isError && (
                    <ErrorMessage
                        message="Failed to load reservations."
                        onRetry={() => refetch()}
                    />
                )}

                {/* Empty */}
                {!isLoading && !isError && reservations.length === 0 && (
                    <div className="flex flex-col items-center justify-center py-24 gap-4">
                        <div className="w-16 h-16 rounded-2xl bg-gray-100 flex items-center
                            justify-center text-3xl">
                            🎫
                        </div>
                        <div className="text-center">
                            <p className="font-semibold text-gray-700">No reservations found</p>
                            <p className="text-sm text-gray-400 mt-1">
                                {activeStatus
                                    ? `No ${activeStatus.toLowerCase()} reservations`
                                    : 'Start by reserving a product'}
                            </p>
                        </div>
                        <Link
                            to="/products"
                            className="mt-2 px-5 py-2.5 bg-indigo-600 text-white text-sm
                         font-medium rounded-xl hover:bg-indigo-700 transition-colors"
                        >
                            Browse products
                        </Link>
                    </div>
                )}

                {/* ── Reservation grid ─────────────────────────────── */}
                {!isLoading && !isError && reservations.length > 0 && (
                    <>
                        {/* Pending banner — shown only on All tab */}
                        {!activeStatus && pendingCount > 0 && (
                            <div className="mb-6 bg-amber-50 border border-amber-100 rounded-2xl
                              px-5 py-4 flex items-center gap-3">
                                <span className="w-2 h-2 rounded-full bg-amber-400 animate-pulse flex-shrink-0" />
                                <p className="text-sm text-amber-800">
                                    You have{' '}
                                    <span className="font-semibold">{pendingCount} active reservation{pendingCount > 1 ? 's' : ''}</span>
                                    {' '}— complete checkout before they expire.
                                </p>
                            </div>
                        )}

                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
                            {reservations.map(reservation => (
                                <ReservationCard
                                    key={reservation.id}
                                    reservation={reservation}
                                    onCheckout={checkout}
                                    onCancel={cancel}
                                    isCheckingOut={isCheckingOut}
                                    isCancelling={isCancelling}
                                    isThisCheckingOut={isCheckingOut && checkoutingId === reservation.id}
                                    isThisCancelling={isCancelling && cancellingId === reservation.id}
                                />
                            ))}
                        </div>

                        <Pagination
                            currentPage={currentPage}
                            totalPages={totalPages}
                            totalElements={totalElements}
                            pageSize={PAGE_SIZE}
                            onPageChange={setPage}
                        />
                    </>
                )}
            </div>
        </div>
    );
};