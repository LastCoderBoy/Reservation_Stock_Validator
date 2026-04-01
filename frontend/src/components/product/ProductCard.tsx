import { Link } from 'react-router-dom';
import type { Product } from '../../types';
import { Badge } from '../ui';
import { formatPrice, stockPercentage } from '../../utils/formatters';

interface ProductCardProps {
    product: Product;
}

const categoryVariant = {
    MEN:   'indigo',
    WOMEN: 'indigo',
    KIDS:  'indigo',
} as const;

export const ProductCard = ({ product }: ProductCardProps) => {
    const stockPct = stockPercentage(product.availableStock, product.totalStock);
    const isSoldOut = product.availableStock === 0;
    const isLowStock = !isSoldOut && product.availableStock <= 5;

    return (
        <Link
            to={`/products/${product.id}`}
            className="group block bg-white rounded-2xl border border-gray-100 overflow-hidden
                 hover:shadow-lg hover:border-indigo-100 transition-all duration-200"
        >
            {/* Image */}
            <div className="relative aspect-square bg-gray-50 overflow-hidden">
                {product.imageKey ? (
                    <img
                        src={product.imageKey}
                        alt={product.name}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                    />
                ) : (
                    <div className="w-full h-full flex items-center justify-center">
            <span className="text-5xl text-gray-200 select-none">
              {product.category === 'KIDS' ? '👟' : '👕'}
            </span>
                    </div>
                )}

                {/* Sold out overlay */}
                {isSoldOut && (
                    <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
            <span className="bg-white text-gray-800 text-sm font-semibold px-4 py-1.5 rounded-full">
              Sold Out
            </span>
                    </div>
                )}

                {/* Category badge */}
                <div className="absolute top-3 left-3">
                    <Badge variant={categoryVariant[product.category]}>
                        {product.category}
                    </Badge>
                </div>
            </div>

            {/* Info */}
            <div className="p-4 space-y-3">
                <div>
                    <h3 className="font-semibold text-gray-900 text-sm leading-snug line-clamp-2 group-hover:text-indigo-600 transition-colors">
                        {product.name}
                    </h3>
                    {product.description && (
                        <p className="text-xs text-gray-400 mt-1 line-clamp-1">{product.description}</p>
                    )}
                </div>

                <div className="flex items-center justify-between">
                    <span className="text-lg font-bold text-gray-900">{formatPrice(product.price)}</span>

                    {isLowStock && (
                        <Badge variant="yellow">Only {product.availableStock} left</Badge>
                    )}
                </div>

                {/* Stock bar */}
                {!isSoldOut && (
                    <div className="space-y-1">
                        <div className="h-1.5 bg-gray-100 rounded-full overflow-hidden">
                            <div
                                className={`h-full rounded-full transition-all duration-500 ${
                                    stockPct > 50 ? 'bg-green-400' :
                                        stockPct > 20 ? 'bg-yellow-400' : 'bg-red-400'
                                }`}
                                style={{ width: `${stockPct}%` }}
                            />
                        </div>
                        <p className="text-xs text-gray-400">
                            {product.availableStock} of {product.totalStock} available
                        </p>
                    </div>
                )}
            </div>
        </Link>
    );
};