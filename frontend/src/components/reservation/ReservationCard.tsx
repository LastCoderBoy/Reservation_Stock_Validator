import { useState } from 'react';
import { Link } from 'react-router-dom';
import type { Reservation, CheckoutResponse } from '../../types';
import { CountdownTimer } from './CountdownTimer';
import { Badge } from '../ui';
import { Button } from '../ui/Button';
import { formatPrice, formatDate } from '../../utils/formatters';

interface ReservationCardProps {
    reservation: Reservation;
    onCheckout: (id: number) => Promise<CheckoutResponse>;
    onCancel: (id: number) => Promise<unknown>;
    isCheckingOut: boolean;
    isCancelling: boolean;
    isThisCheckingOut: boolean;
    isThisCancelling: boolean;
}

const statusConfig: Record<
    Reservation['status'],
    { variant: 'yellow' | 'green' | 'red' | 'gray'; label: string }
> = {
    PENDING:   { variant: 'yellow', label: 'Pending' },
    CONFIRMED: { variant: 'green',  label: 'Confirmed' },
    EXPIRED:   { variant: 'red',    label: 'Expired' },
    CANCELLED: { variant: 'gray',   label: 'Cancelled' },
};

export const ReservationCard = ({
                                    reservation,
                                    onCheckout,
                                    onCancel,
                                    isThisCheckingOut,
                                    isThisCancelling,
                                }: ReservationCardProps) => {
    const [error, setError] = useState<string | null>(null);
    const [isExpired, setIsExpired] = useState(false);
    const [orderDetails, setOrderDetails] = useState<CheckoutResponse | null>(null);

    const isPending = reservation.status === 'PENDING' && !isExpired;
    const config = statusConfig[reservation.status];

    const handleCheckout = async () => {
        setError(null);
        try {
            const result = await onCheckout(reservation.id);
            setOrderDetails(result);
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : 'Checkout failed.');
        }
    };

    const handleCancel = async () => {
        setError(null);
        try {
            await onCancel(reservation.id);
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : 'Cancel failed.');
        }
    };

    // ── Order confirmed state ─────────────────────────────────────
    if (orderDetails) {
        return (
            <div className="bg-white rounded-2xl border border-green-100 p-5 space-y-3">
                <div className="flex items-center gap-2">
                    <div className="w-8 h-8 rounded-full bg-green-100 flex items-center justify-center text-green-600 text-sm font-bold">
                        ✓
                    </div>
                    <div>
                        <p className="font-semibold text-gray-900 text-sm">Order confirmed</p>
                        <p className="text-xs text-gray-400">Order #{orderDetails.orderId}</p>
                    </div>
                    <div className="ml-auto">
                        <Badge variant="green">Confirmed</Badge>
                    </div>
                </div>
                <div className="bg-gray-50 rounded-xl p-3 text-sm space-y-1">
                    <div className="flex justify-between text-gray-600">
                        <span>{orderDetails.productName}</span>
                        <span>×{orderDetails.quantity}</span>
                    </div>
                    <div className="flex justify-between font-semibold text-gray-900">
                        <span>Total paid</span>
                        <span>{formatPrice(orderDetails.totalPrice)}</span>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className={`bg-white rounded-2xl border p-5 space-y-4 transition-all duration-200
      ${isPending
            ? 'border-indigo-100 shadow-sm shadow-indigo-50'
            : 'border-gray-100'
        }`}
        >
            {/* Header row */}
            <div className="flex items-start justify-between gap-3">
                <div className="flex-1 min-w-0">
                    <Link
                        to={`/products/${reservation.productId}`}
                        className="font-semibold text-gray-900 text-sm hover:text-indigo-600
                       transition-colors line-clamp-1"
                    >
                        {reservation.productName}
                    </Link>
                    <p className="text-xs text-gray-400 mt-0.5">
                        Reserved {formatDate(reservation.createdAt)}
                    </p>
                </div>

                <Badge variant={isExpired ? 'red' : config.variant}>
                    {isExpired ? 'Expired' : config.label}
                </Badge>
            </div>

            {/* Countdown — only for active PENDING */}
            {reservation.status === 'PENDING' && !isExpired && (
                <CountdownTimer
                    remainingSeconds={reservation.remainingSeconds}
                    onExpired={() => setIsExpired(true)}
                />
            )}

            {/* Expiry message */}
            {isExpired && (
                <div className="bg-red-50 border border-red-100 rounded-xl px-3 py-2.5 text-xs text-red-600">
                    This reservation expired. Stock has been released.
                </div>
            )}

            {/* Details */}
            <div className="bg-gray-50 rounded-xl p-3 space-y-1.5 text-sm">
                <div className="flex justify-between text-gray-600">
                    <span>Quantity</span>
                    <span className="font-medium text-gray-900">{reservation.quantity}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                    <span>Reservation ID</span>
                    <span className="font-mono text-xs text-gray-500">#{reservation.id}</span>
                </div>
            </div>

            {/* Error */}
            {error && (
                <p className="text-xs text-red-600 bg-red-50 rounded-lg px-3 py-2">{error}</p>
            )}

            {/* Actions — only for active PENDING */}
            {isPending && (
                <div className="flex gap-2">
                    <Button
                        variant="primary"
                        size="sm"
                        className="flex-1"
                        loading={isThisCheckingOut}
                        disabled={isThisCancelling}
                        onClick={handleCheckout}
                    >
                        Checkout
                    </Button>
                    <Button
                        variant="secondary"
                        size="sm"
                        loading={isThisCancelling}
                        disabled={isThisCheckingOut}
                        onClick={handleCancel}
                    >
                        Cancel
                    </Button>
                </div>
            )}

            {/* Reserve again link for expired/cancelled */}
            {(reservation.status === 'EXPIRED' || reservation.status === 'CANCELLED' || isExpired) && (
                <Link
                    to={`/products/${reservation.productId}`}
                    className="block text-center text-xs text-indigo-500 hover:text-indigo-700
                     transition-colors pt-1"
                >
                    View product →
                </Link>
            )}
        </div>
    );
};