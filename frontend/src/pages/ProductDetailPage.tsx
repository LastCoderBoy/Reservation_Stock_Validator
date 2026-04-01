import { useParams, useNavigate, Link } from 'react-router-dom';
import { useProduct } from '../hooks/useProduct';
import { ReservePanel } from '../components/reservation/ReservePanel';
import { Badge, Spinner, ErrorMessage } from '../components/ui';
import { formatPrice, formatDate, stockPercentage } from '../utils/formatters';

export const ProductDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const productId = Number(id);

  const { data: product, isLoading, isError, refetch } = useProduct(productId);

  if (isLoading) {
    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center">
          <Spinner size="lg" />
        </div>
    );
  }

  if (isError || !product) {
    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center">
          <ErrorMessage
              message="Failed to load product."
              onRetry={() => refetch()}
          />
        </div>
    );
  }

  const stockPct = stockPercentage(product.availableStock, product.totalStock);
  const isSoldOut = product.availableStock === 0;
  const isLowStock = !isSoldOut && product.availableStock <= 5;

  return (
      <div className="min-h-screen bg-gray-50">
        {/* Breadcrumb */}
        <div className="bg-white border-b border-gray-100">
          <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-3">
            <nav className="flex items-center gap-2 text-sm text-gray-400">
              <Link to="/products" className="hover:text-indigo-600 transition-colors">
                Products
              </Link>
              <span>/</span>
              <span className="text-gray-700 font-medium truncate">{product.name}</span>
            </nav>
          </div>
        </div>

        {/* Main */}
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">

            {/* Left — Image */}
            <div className="space-y-4">
              <div className="relative aspect-square bg-white rounded-2xl border border-gray-100 overflow-hidden">
                {product.imageKey ? (
                    <img
                        src={product.imageKey}
                        alt={product.name}
                        className="w-full h-full object-cover"
                    />
                ) : (
                    <div className="w-full h-full flex flex-col items-center justify-center gap-3">
                  <span className="text-8xl select-none text-gray-200">
                    {product.category === 'KIDS' ? '👟' : '👕'}
                  </span>
                      <span className="text-sm text-gray-300">No image available</span>
                    </div>
                )}

                {isSoldOut && (
                    <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
                  <span className="bg-white text-gray-800 font-bold text-lg px-6 py-2 rounded-full">
                    Sold Out
                  </span>
                    </div>
                )}
              </div>

              {/* Stock bar */}
              {!isSoldOut && (
                  <div className="bg-white rounded-xl border border-gray-100 p-4 space-y-2">
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-500">Stock availability</span>
                      <span className={`font-medium ${
                          isLowStock ? 'text-amber-600' : 'text-gray-700'
                      }`}>
                    {product.availableStock} of {product.totalStock} left
                  </span>
                    </div>
                    <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                      <div
                          className={`h-full rounded-full transition-all duration-500 ${
                              stockPct > 50 ? 'bg-green-400' :
                                  stockPct > 20 ? 'bg-yellow-400' : 'bg-red-400'
                          }`}
                          style={{ width: `${stockPct}%` }}
                      />
                    </div>
                    {isLowStock && (
                        <p className="text-xs text-amber-600 font-medium">
                          Only {product.availableStock} left — reserve now!
                        </p>
                    )}
                  </div>
              )}
            </div>

            {/* Right — Info + Reserve */}
            <div className="space-y-6">
              {/* Header */}
              <div className="space-y-3">
                <div className="flex items-center gap-2 flex-wrap">
                  <Badge variant="indigo">{product.category}</Badge>
                  {isSoldOut && <Badge variant="red">Sold out</Badge>}
                  {isLowStock && <Badge variant="yellow">Low stock</Badge>}
                  {!product.active && <Badge variant="gray">Inactive</Badge>}
                </div>

                <h1 className="text-3xl font-bold text-gray-900 leading-tight">
                  {product.name}
                </h1>

                <p className="text-3xl font-bold text-indigo-600">
                  {formatPrice(product.price)}
                </p>
              </div>

              {/* Description */}
              {product.description && (
                  <div className="prose prose-sm text-gray-600 max-w-none">
                    <p className="leading-relaxed">{product.description}</p>
                  </div>
              )}

              {/* Live stock indicator */}
              <div className="flex items-center gap-1.5 text-xs text-gray-400">
                <span className="w-1.5 h-1.5 rounded-full bg-green-400 animate-pulse" />
                Stock updates every 5 seconds
              </div>

              {/* Divider */}
              <div className="border-t border-gray-100" />

              {/* Reserve Panel */}
              <ReservePanel
                  productId={product.id}
                  productName={product.name}
                  price={product.price}
                  availableStock={product.availableStock}
              />

              {/* Meta */}
              <div className="text-xs text-gray-300 space-y-1 pt-2">
                {'createdAt' in product && (
                    <p>Listed {formatDate((product as { createdAt: string }).createdAt)}</p>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Back button */}
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 pb-10">
          <button
              onClick={() => navigate(-1)}
              className="text-sm text-gray-400 hover:text-indigo-600 transition-colors flex items-center gap-1"
          >
            ← Back to products
          </button>
        </div>
      </div>
  );
};