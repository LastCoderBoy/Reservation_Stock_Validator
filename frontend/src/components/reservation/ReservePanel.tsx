import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import type { Reservation } from '../../types';
import { useReservation } from '../../hooks/useProduct';
import { useAuth } from '../../contexts/AuthContext';
import { CountdownTimer } from './CountdownTimer';
import { Button } from '../ui/Button';
import { formatPrice } from '../../utils/formatters';

interface ReservePanelProps {
    productId: number;
    productName: string;
    price: number;
    availableStock: number;
}

export const ReservePanel = ({
                                 productId,
                                 productName,
                                 price,
                                 availableStock,
                             }: ReservePanelProps) => {
    const navigate = useNavigate();
    const { isAuthenticated } = useAuth();
    const { reserve, isReserving, checkout, isCheckingOut, cancel, isCancelling } = useReservation();

    const [quantity, setQuantity] = useState(1);
    const [reservation, setReservation] = useState<Reservation | null>(null);
    const [errorMsg, setErrorMsg] = useState<string | null>(null);
    const [successMsg, setSuccessMsg] = useState<string | null>(null);
    const [isExpired, setIsExpired] = useState(false);

    const isSoldOut = availableStock === 0;

    // Clear expired state when stock updates
    useEffect(() => {
        if (availableStock > 0 && isExpired && !reservation) {
            setIsExpired(false);
        }
    }, [availableStock, isExpired, reservation]);

    const handleReserve = async () => {
        if (!isAuthenticated) {
            navigate('/login', { state: { from: `/products/${productId}` } });
            return;
        }

        setErrorMsg(null);
        setSuccessMsg(null);

        try {
            const res = await reserve({ productId, quantity });
            setReservation(res ?? null);
            setIsExpired(false);
        } catch (err: unknown) {
            const message = err instanceof Error ? err.message : 'Failed to reserve. Please try again.';
            setErrorMsg(message);
        }
    };

    const handleCheckout = async () => {
        if (!reservation) return;
        setErrorMsg(null);

        try {
            await checkout(reservation.id);
            setReservation(null);
            setSuccessMsg(`Order placed for "${productName}"!`);
        } catch (err: unknown) {
            const message = err instanceof Error ? err.message : 'Checkout failed. Please try again.';
            setErrorMsg(message);
        }
    };

    const handleCancel = async () => {
        if (!reservation) return;
        setErrorMsg(null);

        try {
            await cancel(reservation.id);
            setReservation(null);
            setSuccessMsg(null);
        } catch (err: unknown) {
            const message = err instanceof Error ? err.message : 'Failed to cancel reservation.';
            setErrorMsg(message);
        }
    };

    const handleExpired = () => {
        setIsExpired(true);
        setReservation(null);
    };

    // ── Success state ──────────────────────────────────────────────
    if (successMsg) {
        return (
            <div className="space-y-4">
                <div className="bg-green-50 border border-green-100 rounded-xl p-5 text-center space-y-2">
                    <div className="text-3xl">🎉</div>
                    <p className="font-semibold text-green-800">{successMsg}</p>
                    <p className="text-sm text-green-600">Check your reservations page for details.</p>
                </div>
                <Button variant="secondary" size="lg" className="w-full" onClick={() => navigate('/reservations')}>
                    View my orders
                </Button>
            </div>
        );
    }

    // ── Active reservation state ───────────────────────────────────
    if (reservation && !isExpired) {
        return (
            <div className="space-y-4">
                <CountdownTimer expiresAt={reservation.expiresAt} onExpired={handleExpired} />

                <div className="bg-gray-50 rounded-xl p-4 text-sm space-y-1.5">
                    <div className="flex justify-between text-gray-600">
                        <span>Quantity</span>
                        <span className="font-medium text-gray-900">{reservation.quantity}</span>
                    </div>
                    <div className="flex justify-between text-gray-600">
                        <span>Total</span>
                        <span className="font-bold text-gray-900">{formatPrice(price * reservation.quantity)}</span>
                    </div>
                </div>

                {errorMsg && (
                    <p className="text-sm text-red-600 bg-red-50 rounded-lg px-3 py-2">{errorMsg}</p>
                )}

                <Button
                    variant="primary"
                    size="lg"
                    className="w-full"
                    loading={isCheckingOut}
                    onClick={handleCheckout}
                >
                    Complete checkout
                </Button>

                <Button
                    variant="ghost"
                    size="sm"
                    className="w-full text-gray-400"
                    loading={isCancelling}
                    onClick={handleCancel}
                >
                    Cancel reservation
                </Button>
            </div>
        );
    }

    // ── Default / reserve state ────────────────────────────────────
    return (
        <div className="space-y-4">
            {/* Expiry message */}
            {isExpired && (
                <div className="bg-red-50 border border-red-100 rounded-xl px-4 py-3 text-sm text-red-700">
                    Your reservation expired. You can reserve again below.
                </div>
            )}

            {/* Quantity selector */}
            {!isSoldOut && (
                <div className="flex items-center justify-between">
                    <span className="text-sm font-medium text-gray-700">Quantity</span>
                    <div className="flex items-center gap-3">
                        <button
                            onClick={() => setQuantity(q => Math.max(1, q - 1))}
                            disabled={quantity <= 1}
                            className="w-8 h-8 rounded-lg border border-gray-200 text-gray-600
                         hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed
                         flex items-center justify-center text-lg leading-none"
                        >
                            −
                        </button>
                        <span className="w-6 text-center font-medium text-gray-900">{quantity}</span>
                        <button
                            onClick={() => setQuantity(q => Math.min(availableStock, q + 1))}
                            disabled={quantity >= availableStock}
                            className="w-8 h-8 rounded-lg border border-gray-200 text-gray-600
                         hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed
                         flex items-center justify-center text-lg leading-none"
                        >
                            +
                        </button>
                    </div>
                </div>
            )}

            {/* Price summary */}
            {!isSoldOut && (
                <div className="flex justify-between items-center py-2 border-t border-gray-100">
                    <span className="text-sm text-gray-500">Total</span>
                    <span className="text-xl font-bold text-gray-900">{formatPrice(price * quantity)}</span>
                </div>
            )}

            {/* Error */}
            {errorMsg && (
                <p className="text-sm text-red-600 bg-red-50 rounded-lg px-3 py-2">{errorMsg}</p>
            )}

            {/* Reserve button */}
            <Button
                variant="primary"
                size="lg"
                className="w-full"
                disabled={isSoldOut}
                loading={isReserving}
                onClick={handleReserve}
            >
                {isSoldOut
                    ? 'Sold out'
                    : !isAuthenticated
                        ? 'Log in to reserve'
                        : 'Reserve now'}
            </Button>

            {!isSoldOut && (
                <p className="text-xs text-center text-gray-400">
                    Reservation holds for 5 minutes. Complete checkout to confirm.
                </p>
            )}
        </div>
    );
};