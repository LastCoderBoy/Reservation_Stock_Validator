import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import { productApi } from '../api';
import type { Product } from '../types';
import { useAuth } from '../contexts';

export const HomePage = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const fetchProducts = async () => {
    try {
      const response = await productApi.getProducts({ size: 20 });
      setProducts(response.content);
    } catch (error: any) {
      toast.error(error.message || 'Failed to load products');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();

    // Poll for stock updates every 5 seconds
    const interval = setInterval(() => {
      fetchProducts();
    }, 5000);

    return () => clearInterval(interval);
  }, []);

  const getStockIndicator = (product: Product) => {
    const available = product.availableStock;
    
    if (available === 0) {
      return { icon: '❌', text: 'Sold Out', color: 'text-text-secondary' };
    } else if (available <= 5) {
      return { icon: '⚠️', text: `${available} left`, color: 'text-urgency-danger' };
    } else if (available <= 20) {
      return { icon: '🔥', text: `${available} left`, color: 'text-urgency-warning' };
    } else {
      return { icon: '✅', text: `${available} available`, color: 'text-success' };
    }
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

  return (
    <div className="min-h-screen bg-primary-50">
      {/* Header/Navbar */}
      <header className="bg-primary-100 border-b border-secondary-200 py-4 px-6 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <Link to="/" className="text-2xl font-bold text-text-primary hover:text-accent-600 transition-colors">
            Limited <span className="text-accent-600">Drop</span>
          </Link>
          <nav className="flex items-center gap-6">
            <Link to="/" className="text-text-primary font-semibold hover:text-accent-600 transition-colors">
              Products
            </Link>
            {user ? (
              <>
                <Link to="/reservations" className="text-text-secondary hover:text-accent-600 transition-colors">
                  My Reservations
                </Link>
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
              </>
            ) : (
              <Link
                to="/login"
                className="px-4 py-2 bg-accent-600 text-white rounded-lg font-semibold hover:bg-accent-500 transition-colors"
              >
                Login
              </Link>
            )}
          </nav>
        </div>
      </header>

      {/* Hero Section */}
      <section className="bg-gradient-to-b from-secondary-50 to-primary-50 py-16 px-6">
        <div className="max-w-7xl mx-auto text-center">
          <h2 className="text-5xl font-bold text-text-primary mb-4">
            Limited Stock Drops
          </h2>
          <p className="text-xl text-text-secondary mb-8">
            Exclusive products. Limited quantities. Act fast! ⚡
          </p>
          <div className="inline-flex items-center gap-2 px-6 py-3 bg-accent-600 text-white rounded-lg font-semibold">
            🔥 Live Now - Don't Miss Out!
          </div>
        </div>
      </section>

      {/* Products Grid */}
      <main className="max-w-7xl mx-auto py-12 px-6">
        {isLoading ? (
          // Loading Skeleton
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {[...Array(8)].map((_, i) => (
              <div key={i} className="bg-white rounded-lg shadow-md border border-secondary-100 p-6 animate-pulse">
                <div className="aspect-square bg-secondary-100 rounded-md mb-4"></div>
                <div className="h-6 bg-secondary-100 rounded mb-2"></div>
                <div className="h-4 bg-secondary-100 rounded w-2/3 mb-4"></div>
                <div className="h-8 bg-secondary-100 rounded mb-4"></div>
                <div className="h-10 bg-secondary-100 rounded"></div>
              </div>
            ))}
          </div>
        ) : products.length === 0 ? (
          // Empty State
          <div className="text-center py-16">
            <span className="text-6xl mb-4 block">📦</span>
            <h3 className="text-2xl font-bold text-text-primary mb-2">No Products Available</h3>
            <p className="text-text-secondary">Check back later for new drops!</p>
          </div>
        ) : (
          // Products Grid
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {products.map((product) => {
              const stockInfo = getStockIndicator(product);
              const isSoldOut = product.availableStock === 0;

              return (
                <Link
                  key={product.id}
                  to={`/products/${product.id}`}
                  className="bg-white rounded-lg shadow-md border border-secondary-100 p-6 hover:shadow-lg hover:border-accent-600 transition-all cursor-pointer group"
                >
                  <div className="aspect-square bg-secondary-50 rounded-md mb-4 flex items-center justify-center overflow-hidden">
                    {product.imageUrl ? (
                      <img
                        src={product.imageUrl}
                        alt={product.name}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                      />
                    ) : (
                      <span className="text-4xl">📦</span>
                    )}
                  </div>

                  <h3 className="text-lg font-semibold text-text-primary mb-2 group-hover:text-accent-600 transition-colors line-clamp-1">
                    {product.name}
                  </h3>

                  <p className="text-text-secondary text-sm mb-4 line-clamp-2">
                    {product.description || 'Limited edition product'}
                  </p>

                  <div className="flex items-center justify-between mb-4">
                    <span className="text-2xl font-bold text-accent-600">
                      ${product.price.toFixed(2)}
                    </span>
                    <span className={`text-sm font-semibold ${stockInfo.color}`}>
                      {stockInfo.icon} {stockInfo.text}
                    </span>
                  </div>

                  <button
                    disabled={isSoldOut}
                    className={`w-full py-2 rounded-md font-semibold transition-colors ${
                      isSoldOut
                        ? 'bg-secondary-200 text-text-secondary cursor-not-allowed'
                        : 'bg-accent-600 text-white hover:bg-accent-500'
                    }`}
                    onClick={(e) => {
                      if (!user) {
                        e.preventDefault();
                        navigate(`/login?returnUrl=/products/${product.id}`);
                      }
                    }}
                  >
                    {isSoldOut ? 'Sold Out' : 'Reserve Now'}
                  </button>
                </Link>
              );
            })}
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="bg-primary-100 border-t border-secondary-200 py-8 px-6 mt-16">
        <div className="max-w-7xl mx-auto text-center text-text-secondary">
          <p className="text-sm">Limited Stock Drop System © 2026</p>
        </div>
      </footer>
    </div>
  );
};
