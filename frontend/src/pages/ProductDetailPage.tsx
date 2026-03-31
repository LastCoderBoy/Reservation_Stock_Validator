import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { toast } from 'sonner';
import { productApi, reservationApi } from '../api';
import type { Product } from '../types';
import { useAuth } from '../contexts';

export const ProductDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const [product, setProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [isReserving, setIsReserving] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProduct = async () => {
      if (!id) return;
      
      try {
        const data = await productApi.getProductById(Number(id));
        setProduct(data);
      } catch (error: any) {
        toast.error(error.message || 'Failed to load product');
        navigate('/');
      } finally {
        setIsLoading(false);
      }
    };

    fetchProduct();

    // Poll for stock updates every 5 seconds
    const interval = setInterval(() => {
      fetchProduct();
    }, 5000);

    return () => clearInterval(interval);
  }, [id, navigate]);

  const handleQuantityChange = (delta: number) => {
    if (!product) return;
    
    const newQuantity = quantity + delta;
    if (newQuantity < 1) return;
    if (newQuantity > product.availableStock) {
      toast.error(`Only ${product.availableStock} items available`);
      return;
    }
    setQuantity(newQuantity);
  };

  const handleReserve = async () => {
    if (!product || !user) {
      navigate(`/login?returnUrl=/products/${id}`);
      return;
    }

    if (product.availableStock < quantity) {
      toast.error('Not enough stock available');
      return;
    }

    setIsReserving(true);
    try {
      await reservationApi.createReservation({
        productId: product.id,
        quantity,
      });

      toast.success(`Reserved ${quantity} ${product.name}! You have 5 minutes to complete checkout.`);
      navigate('/reservations');
    } catch (error: any) {
      toast.error(error.message || 'Failed to create reservation');
    } finally {
      setIsReserving(false);
    }
  };

  const getStockIndicator = (availableStock: number) => {
    if (availableStock === 0) {
      return { text: 'Sold Out', color: 'bg-text-secondary/10 text-text-secondary', icon: '❌' };
    } else if (availableStock <= 5) {
      return { text: `Only ${availableStock} left!`, color: 'bg-urgency-danger/10 text-urgency-danger', icon: '⚠️' };
    } else if (availableStock <= 20) {
      return { text: `${availableStock} left`, color: 'bg-urgency-warning/10 text-urgency-warning', icon: '🔥' };
    } else {
      return { text: `${availableStock} available`, color: 'bg-success/10 text-success', icon: '✅' };
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-primary-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent-600"></div>
      </div>
    );
  }

  if (!product) {
    return null;
  }

  const stockInfo = getStockIndicator(product.availableStock);
  const isSoldOut = product.availableStock === 0;

  return (
    <div className="min-h-screen bg-primary-50">
      {/* Header */}
      <header className="bg-primary-100 border-b border-secondary-200 py-4 px-6">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <Link to="/" className="text-2xl font-bold text-text-primary hover:text-accent-600 transition-colors">
            Limited <span className="text-accent-600">Drop</span>
          </Link>
          <nav className="flex gap-6">
            <Link to="/" className="text-text-secondary hover:text-accent-600 transition-colors">
              Products
            </Link>
            {user && (
              <Link to="/reservations" className="text-text-secondary hover:text-accent-600 transition-colors">
                My Reservations
              </Link>
            )}
          </nav>
        </div>
      </header>

      {/* Back Button */}
      <div className="max-w-7xl mx-auto px-6 py-6">
        <Link
          to="/"
          className="inline-flex items-center gap-2 text-text-secondary hover:text-accent-600 transition-colors font-semibold"
        >
          ← Back to Products
        </Link>
      </div>

      {/* Product Detail */}
      <main className="max-w-7xl mx-auto px-6 pb-12">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
          {/* Left: Product Image */}
          <div>
            <div className="aspect-square bg-secondary-50 rounded-lg flex items-center justify-center border border-secondary-200 overflow-hidden">
              {product.imageUrl ? (
                <img
                  src={product.imageUrl}
                  alt={product.name}
                  className="w-full h-full object-cover"
                />
              ) : (
                <span className="text-9xl">📦</span>
              )}
            </div>
          </div>

          {/* Right: Product Info */}
          <div>
            <h1 className="text-4xl font-bold text-text-primary mb-4">
              {product.name}
            </h1>

            <div className="flex items-center gap-4 mb-6">
              <span className="text-4xl font-bold text-accent-600">
                ${product.price.toFixed(2)}
              </span>
              <span className={`px-3 py-1 rounded-full text-sm font-semibold ${stockInfo.color}`}>
                {stockInfo.icon} {stockInfo.text}
              </span>
            </div>

            <div className="border-t border-secondary-200 pt-6 mb-6">
              <h3 className="text-sm font-semibold text-text-secondary mb-2">
                DESCRIPTION
              </h3>
              <p className="text-text-primary leading-relaxed">
                {product.description || 'This is a limited edition product with exclusive features. High demand and limited stock make this a must-have item. Reserve now before it\'s gone!'}
              </p>
            </div>

            {/* Quantity Selector */}
            {!isSoldOut && (
              <div className="mb-6">
                <label className="block text-sm font-semibold text-text-primary mb-2">
                  Quantity
                </label>
                <div className="flex items-center gap-4">
                  <button
                    onClick={() => handleQuantityChange(-1)}
                    disabled={quantity <= 1}
                    className="w-10 h-10 rounded-lg border border-secondary-200 hover:border-accent-600 transition-colors flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed font-bold text-lg"
                  >
                    -
                  </button>
                  <input
                    type="number"
                    value={quantity}
                    readOnly
                    className="w-20 text-center py-2 border border-secondary-200 rounded-lg font-semibold text-lg"
                  />
                  <button
                    onClick={() => handleQuantityChange(1)}
                    disabled={quantity >= product.availableStock}
                    className="w-10 h-10 rounded-lg border border-secondary-200 hover:border-accent-600 transition-colors flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed font-bold text-lg"
                  >
                    +
                  </button>
                </div>
              </div>
            )}

            {/* Reserve Button */}
            <button
              onClick={handleReserve}
              disabled={isSoldOut || isReserving}
              className={`w-full py-4 rounded-lg font-bold text-lg transition-all focus:outline-none focus:ring-2 focus:ring-accent-600 focus:ring-offset-2 ${
                isSoldOut
                  ? 'bg-secondary-200 text-text-secondary cursor-not-allowed'
                  : isReserving
                  ? 'bg-accent-500 text-white cursor-wait'
                  : 'bg-accent-600 text-white hover:bg-accent-500'
              }`}
            >
              {isSoldOut ? '❌ Sold Out' : isReserving ? 'Reserving...' : '🚀 Reserve Now'}
            </button>

            <p className="mt-4 text-sm text-text-secondary text-center">
              {!isSoldOut && '⏱️ Reservation locks stock for 5 minutes'}
            </p>

            {/* Additional Info */}
            <div className="mt-8 p-4 bg-secondary-50 rounded-lg border border-secondary-200">
              <h4 className="font-semibold text-text-primary mb-2">Product Details:</h4>
              <ul className="space-y-1 text-sm text-text-secondary">
                <li>• Total Stock: {product.totalStock}</li>
                <li>• Reserved: {product.reservedStock}</li>
                <li>• Available: {product.availableStock}</li>
                {product.category && <li>• Category: {product.category}</li>}
              </ul>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};
