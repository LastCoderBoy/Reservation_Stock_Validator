import type { Product } from '../../types';
import { ProductCard } from './ProductCard';
import { Spinner, ErrorMessage } from '../ui';

interface ProductGridProps {
    products: Product[];
    isLoading: boolean;
    isError: boolean;
    isFetching: boolean;
    onRetry: () => void;
}

export const ProductGrid = ({
                                products,
                                isLoading,
                                isError,
                                isFetching,
                                onRetry,
                            }: ProductGridProps) => {
    if (isLoading) {
        return (
            <div className="flex items-center justify-center py-24">
                <Spinner size="lg" />
            </div>
        );
    }

    if (isError) {
        return (
            <ErrorMessage
                message="Failed to load products. Please try again."
                onRetry={onRetry}
            />
        );
    }

    if (products.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center py-24 gap-3">
                <span className="text-5xl">🔍</span>
                <p className="text-gray-500 font-medium">No products found</p>
                <p className="text-gray-400 text-sm">Try adjusting your filters</p>
            </div>
        );
    }

    return (
        <div className="relative">
            {/* Subtle fetching indicator — doesn't replace content */}
            {isFetching && !isLoading && (
                <div className="absolute top-0 left-0 right-0 h-0.5 bg-indigo-100 overflow-hidden rounded">
                    <div className="h-full bg-indigo-500 animate-pulse" />
                </div>
            )}

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5 pt-1">
                {products.map(product => (
                    <ProductCard key={product.id} product={product} />
                ))}
            </div>
        </div>
    );
};