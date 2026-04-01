import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { usePendingCount } from '../hooks/usePendingCount';
import { authApi } from '../api/auth';

export const Navbar = () => {
    const { isAuthenticated, user, logout } = useAuth();
    const navigate = useNavigate();
    const pendingCount = usePendingCount();
    const [menuOpen, setMenuOpen] = useState(false);
    const [isLoggingOut, setIsLoggingOut] = useState(false);

    const handleLogout = async () => {
        setIsLoggingOut(true);
        try {
            await authApi.logout();
        } catch {
            // always clear local state even if server call fails
        } finally {
            logout();
            setIsLoggingOut(false);
            navigate('/products', { replace: true });
        }
    };

    const closeMenu = () => setMenuOpen(false);

    const navLinkClass = ({ isActive }: { isActive: boolean }) =>
        [
            'text-sm font-medium transition-colors duration-150',
            isActive
                ? 'text-indigo-600'
                : 'text-gray-500 hover:text-gray-900',
        ].join(' ');

    return (
        <header className="sticky top-0 z-50 bg-white/90 backdrop-blur-sm border-b border-gray-100">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-14">

                    {/* ── Logo ─────────────────────────────────────── */}
                    <Link
                        to="/products"
                        className="flex items-center gap-2 flex-shrink-0"
                        onClick={closeMenu}
                    >
                        <div className="w-2 h-2 rounded-full bg-indigo-600 shadow-[0_0_8px_#6366f1]" />
                        <span className="text-sm font-bold text-gray-900 tracking-tight">
              LimitedDrop
            </span>
                    </Link>

                    {/* ── Desktop nav ───────────────────────────────── */}
                    <nav className="hidden sm:flex items-center gap-6">
                        <NavLink to="/products" className={navLinkClass}>
                            Products
                        </NavLink>

                        {isAuthenticated && (
                            <NavLink to="/reservations" className={navLinkClass}>
                <span className="relative inline-flex items-center gap-1.5">
                  Reservations
                    {pendingCount > 0 && (
                        <span className="relative flex h-2 w-2">
                      <span className="animate-ping absolute inline-flex h-full w-full
                                       rounded-full bg-amber-400 opacity-75" />
                      <span className="relative inline-flex rounded-full h-2 w-2 bg-amber-400" />
                    </span>
                    )}
                </span>
                            </NavLink>
                        )}
                    </nav>

                    {/* ── Desktop auth controls ─────────────────────── */}
                    <div className="hidden sm:flex items-center gap-3">
                        {isAuthenticated ? (
                            <>
                                {/* Username chip */}
                                <div className="flex items-center gap-2 px-3 py-1.5 bg-gray-50
                                rounded-lg border border-gray-100">
                                    <div className="w-5 h-5 rounded-full bg-indigo-100 flex items-center
                                  justify-center text-indigo-600 text-[10px] font-bold">
                                        {user?.username?.[0]?.toUpperCase() ?? 'U'}
                                    </div>
                                    <span className="text-xs font-medium text-gray-700 max-w-[120px] truncate">
                    {user?.username}
                  </span>
                                    {user?.role === 'ROLE_ADMIN' && (
                                        <span className="text-[10px] font-semibold text-indigo-500
                                     bg-indigo-50 border border-indigo-100
                                     px-1.5 py-0.5 rounded">
                      Admin
                    </span>
                                    )}
                                </div>

                                {/* Pending badge */}
                                {pendingCount > 0 && (
                                    <Link
                                        to="/reservations"
                                        className="flex items-center gap-1.5 text-xs font-medium
                               text-amber-700 bg-amber-50 border border-amber-100
                               px-2.5 py-1.5 rounded-lg hover:bg-amber-100 transition-colors"
                                    >
                                        <span className="w-1.5 h-1.5 rounded-full bg-amber-400 animate-pulse" />
                                        {pendingCount} pending
                                    </Link>
                                )}

                                {/* Log out */}
                                <button
                                    onClick={handleLogout}
                                    disabled={isLoggingOut}
                                    className="text-sm font-medium text-gray-400 hover:text-gray-700
                             transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                >
                                    {isLoggingOut ? 'Logging out…' : 'Log out'}
                                </button>
                            </>
                        ) : (
                            <Link
                                to="/login"
                                className="px-4 py-1.5 bg-indigo-600 text-white text-sm font-medium
                           rounded-lg hover:bg-indigo-700 transition-colors"
                            >
                                Log in
                            </Link>
                        )}
                    </div>

                    {/* ── Mobile hamburger ──────────────────────────── */}
                    <button
                        className="sm:hidden flex flex-col justify-center items-center
                       w-8 h-8 gap-1.5 relative"
                        onClick={() => setMenuOpen(v => !v)}
                        aria-label="Toggle menu"
                    >
            <span className={`block w-5 h-0.5 bg-gray-600 transition-all duration-200
              ${menuOpen ? 'rotate-45 translate-y-2' : ''}`} />
                        <span className={`block w-5 h-0.5 bg-gray-600 transition-all duration-200
              ${menuOpen ? 'opacity-0' : ''}`} />
                        <span className={`block w-5 h-0.5 bg-gray-600 transition-all duration-200
              ${menuOpen ? '-rotate-45 -translate-y-2' : ''}`} />

                        {/* Dot on hamburger when pending */}
                        {pendingCount > 0 && !menuOpen && (
                            <span className="absolute top-0.5 right-0.5 w-2 h-2 rounded-full
                               bg-amber-400 border-2 border-white" />
                        )}
                    </button>
                </div>
            </div>

            {/* ── Mobile menu ───────────────────────────────────── */}
            {menuOpen && (
                <div className="sm:hidden border-t border-gray-100 bg-white">
                    <div className="max-w-7xl mx-auto px-4 py-4 space-y-1">

                        <NavLink
                            to="/products"
                            className={({ isActive }) =>
                                `block px-3 py-2.5 rounded-lg text-sm font-medium transition-colors
                 ${isActive ? 'bg-indigo-50 text-indigo-600' : 'text-gray-600 hover:bg-gray-50'}`
                            }
                            onClick={closeMenu}
                        >
                            Products
                        </NavLink>

                        {isAuthenticated && (
                            <NavLink
                                to="/reservations"
                                className={({ isActive }) =>
                                    `flex items-center justify-between px-3 py-2.5 rounded-lg text-sm
                   font-medium transition-colors
                   ${isActive ? 'bg-indigo-50 text-indigo-600' : 'text-gray-600 hover:bg-gray-50'}`
                                }
                                onClick={closeMenu}
                            >
                                <span>Reservations</span>
                                {pendingCount > 0 && (
                                    <span className="flex items-center gap-1 text-xs font-semibold
                                   text-amber-700 bg-amber-50 border border-amber-100
                                   px-2 py-0.5 rounded-full">
                    <span className="w-1.5 h-1.5 rounded-full bg-amber-400 animate-pulse" />
                                        {pendingCount}
                  </span>
                                )}
                            </NavLink>
                        )}

                        {/* Divider */}
                        <div className="border-t border-gray-100 my-2" />

                        {isAuthenticated ? (
                            <>
                                {/* User info row */}
                                <div className="flex items-center gap-2 px-3 py-2">
                                    <div className="w-7 h-7 rounded-full bg-indigo-100 flex items-center
                                  justify-center text-indigo-600 text-xs font-bold">
                                        {user?.username?.[0]?.toUpperCase() ?? 'U'}
                                    </div>
                                    <div>
                                        <p className="text-sm font-medium text-gray-800">{user?.username}</p>
                                        {user?.role === 'ROLE_ADMIN' && (
                                            <p className="text-xs text-indigo-500">Admin</p>
                                        )}
                                    </div>
                                </div>

                                <button
                                    onClick={() => { closeMenu(); handleLogout(); }}
                                    disabled={isLoggingOut}
                                    className="w-full text-left px-3 py-2.5 rounded-lg text-sm font-medium
                             text-red-500 hover:bg-red-50 transition-colors
                             disabled:opacity-50 disabled:cursor-not-allowed"
                                >
                                    {isLoggingOut ? 'Logging out…' : 'Log out'}
                                </button>
                            </>
                        ) : (
                            <Link
                                to="/login"
                                onClick={closeMenu}
                                className="block px-3 py-2.5 rounded-lg text-sm font-medium
                           text-indigo-600 hover:bg-indigo-50 transition-colors"
                            >
                                Log in
                            </Link>
                        )}
                    </div>
                </div>
            )}
        </header>
    );
};