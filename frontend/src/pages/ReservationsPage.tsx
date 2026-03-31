import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import { reservationApi } from '../api';
import type { ReservationResponse, ReservationStatus as RS } from '../types';
import { CountdownTimer } from '../components/features';
import { useAuth } from '../contexts';

export const ReservationsPage = () => {
  const [activeReservations, setActiveReservations] = useState<ReservationResponse[]>([]);
  const [historyReservations, setHistoryReservations] = useState<ReservationResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const fetchReservations = async () => {
    try {
      const response = await reservationApi.getUserReservations({ size: 50 });
      
      // Separate active (PENDING) from history (CONFIRMED, EXPIRED, CANCELLED)
      const active = response.content.filter(r => r.status === 'PENDING');
      const history = response.content.filter(r => r.status !== 'PENDING');
      
      setActiveReservations(active);
      setHistoryReservations(history);
    } catch (error: any) {
      toast.error(error.message || 'Failed to load reservations');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchReservations();

    // Refresh every 10 seconds
    const interval = setInterval(() => {
      fetchReservations();
    }, 10000);

    return () => clearInterval(interval);
  }, []);

  const handleCheckout = async (id: number) => {
    try {
      const response = await reservationApi.checkout(id);
      toast.success(response.message || 'Order placed successfully!');
      fetchReservations(); // Refresh list
    } catch (error: any) {
      toast.error(error.message || 'Checkout failed');
    }
  };

  const handleCancel = async (id: number) => {
    if (!confirm('Are you sure you want to cancel this reservation?')) {
      return;
    }

    try {
      await reservationApi.cancelReservation(id);
      toast.success('Reservation cancelled successfully');
      fetchReservations(); // Refresh list
    } catch (error: any) {
      toast.error(error.message || 'Failed to cancel reservation');
    }
  };

  const handleExpire = (id: number) => {
    // When timer expires, mark reservation as expired in UI
    setActiveReservations(prev => prev.filter(r => r.id !== id));
    toast.error('Reservation expired!');
    fetchReservations(); // Refresh from server
  };

  const handleLogout = async () => {
    try {
      await logout();
      toast.success('Logged out successfully');
      navigate('/');
    } catch (error) {
      toast.error('Logout failed');
    }
  };

  const getStatusBadge = (status: RS) => {
    switch (status) {
      case 'PENDING':
        return 'px-3 py-1 bg-urgency-warning/10 text-urgency-warning rounded-full text-sm font-semibold';
      case 'CONFIRMED':
        return 'px-3 py-1 bg-success/10 text-success rounded-full text-sm font-semibold';
      case 'EXPIRED':
        return 'px-3 py-1 bg-text-secondary/10 text-text-secondary rounded-full text-sm font-semibold';
      case 'CANCELLED':
        return 'px-3 py-1 bg-text-secondary/10 text-text-secondary rounded-full text-sm font-semibold';
      default:
        return 'px-3 py-1 bg-secondary-200 text-text-secondary rounded-full text-sm font-semibold';
    }
  };

  const getStatusIcon = (status: RS) => {
    switch (status) {
      case 'CONFIRMED':
        return '✓';
      case 'EXPIRED':
        return '⏰';
      case 'CANCELLED':
        return '✕';
      default:
        return '';
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-primary-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-primary-50">
      {/* Header */}
      <header className="bg-primary-100 border-b border-secondary-200 py-4 px-6">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <Link to="/" className="text-2xl font-bold text-text-primary hover:text-accent-600 transition-colors">
            Limited <span className="text-accent-600">Drop</span>
          </Link>
          <nav className="flex items-center gap-6">
            <Link to="/" className="text-text-secondary hover:text-accent-600 transition-colors">
              Products
            </Link>
            <Link to="/reservations" className="text-accent-600 font-semibold">
              My Reservations
            </Link>
            {user && (
              <div className="flex items-center gap-3">
                <span className="text-sm text-text-secondary">
                  👤 {user.username}
                </span>
                <button
                  onClick={handleLogout}
                  className="text-sm text-text-secondary hover:text-error transition-colors"
                >
                  Logout
                </button>
              </div>
            )}
          </nav>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-5xl mx-auto px-6 py-12">
        <h1 className="text-4xl font-bold text-text-primary mb-8">
          My Reservations
        </h1>

        {/* Active Reservations */}
        <section className="mb-12">
          <h2 className="text-2xl font-semibold text-text-primary mb-4 flex items-center gap-2">
            <span>⏱️</span> Active Reservations
          </h2>

          {activeReservations.length === 0 ? (
            <div className="bg-white rounded-lg border border-secondary-200 p-8 text-center">
              <span className="text-4xl mb-2 block">📭</span>
              <p className="text-text-secondary">No active reservations</p>
              <Link
                to="/"
                className="inline-block mt-4 px-6 py-2 bg-accent-600 text-white rounded-lg font-semibold hover:bg-accent-500 transition-colors"
              >
                Browse Products
              </Link>
            </div>
          ) : (
            <div className="space-y-4">
              {activeReservations.map((reservation) => (
                <div
                  key={reservation.id}
                  className="bg-white rounded-lg border border-secondary-200 shadow-md p-6"
                >
                  <div className="flex items-start justify-between mb-4">
                    <div>
                      <h3 className="text-xl font-semibold text-text-primary">
                        {reservation.productName}
                      </h3>
                      <p className="text-text-secondary">
                        Quantity: {reservation.quantity} × ${reservation.productPrice.toFixed(2)} = ${reservation.totalPrice.toFixed(2)}
                      </p>
                    </div>
                    <span className={getStatusBadge(reservation.status)}>
                      {reservation.status}
                    </span>
                  </div>

                  {/* Countdown Timer */}
                  <div className="mb-6">
                    <CountdownTimer
                      expiresAt={reservation.expiresAt}
                      onExpire={() => handleExpire(reservation.id)}
                    />
                  </div>

                  {/* Actions */}
                  <div className="flex gap-3">
                    <button
                      onClick={() => handleCheckout(reservation.id)}
                      className="flex-1 py-3 bg-accent-600 text-white rounded-lg font-semibold hover:bg-accent-500 transition-colors"
                    >
                      Complete Checkout
                    </button>
                    <button
                      onClick={() => handleCancel(reservation.id)}
                      className="px-6 py-3 border border-secondary-200 text-text-secondary rounded-lg font-semibold hover:border-error hover:text-error transition-colors"
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* History */}
        {historyReservations.length > 0 && (
          <section>
            <h2 className="text-2xl font-semibold text-text-primary mb-4 flex items-center gap-2">
              <span>📜</span> History
            </h2>

            <div className="space-y-3">
              {historyReservations.map((reservation) => (
                <div
                  key={reservation.id}
                  className={`bg-white rounded-lg border border-secondary-200 p-6 ${
                    reservation.status !== 'CONFIRMED' ? 'opacity-60' : ''
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-lg font-semibold text-text-primary">
                        {reservation.productName}
                      </h3>
                      <p className="text-sm text-text-secondary">
                        {new Date(reservation.createdAt).toLocaleDateString('en-US', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                        })} • Quantity: {reservation.quantity}
                      </p>
                    </div>
                    <span className={getStatusBadge(reservation.status)}>
                      {getStatusIcon(reservation.status)} {reservation.status}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </section>
        )}
      </main>
    </div>
  );
};
