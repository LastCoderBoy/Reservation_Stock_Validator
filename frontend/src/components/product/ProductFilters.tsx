import { useState, useEffect } from 'react';
import type { Category, SortField, SortDirection, ProductFilterParams } from '../../types';
import { Button } from '../ui/Button';

interface ProductFiltersProps {
    filters: ProductFilterParams;
    onSearch: (search: string) => void;
    onCategory: (category: Category | undefined) => void;
    onInStock: (inStock: boolean | undefined) => void;
    onSortBy: (sortBy: SortField) => void;
    onSortDirection: (dir: SortDirection) => void;
    onPriceRange: (min?: number, max?: number) => void;
    onReset: () => void;
}

const CATEGORIES: Category[] = ['MEN', 'WOMEN', 'KIDS'];
const SORT_FIELDS: { value: SortField; label: string }[] = [
    { value: 'name',  label: 'Name' },
    { value: 'price', label: 'Price' },
    { value: 'stock', label: 'Stock' },
];

export const ProductFilters = ({
                                   filters,
                                   onSearch,
                                   onCategory,
                                   onInStock,
                                   onSortBy,
                                   onSortDirection,
                                   onPriceRange,
                                   onReset,
                               }: ProductFiltersProps) => {
    const [searchInput, setSearchInput] = useState(filters.search ?? '');
    const [minPrice, setMinPrice] = useState(filters.minPrice?.toString() ?? '');
    const [maxPrice, setMaxPrice] = useState(filters.maxPrice?.toString() ?? '');

    // Debounce search — wait 400ms after user stops typing
    useEffect(() => {
        const timer = setTimeout(() => {
            onSearch(searchInput);
        }, 400);
        return () => clearTimeout(timer);
    }, [searchInput, onSearch]);

    const handlePriceApply = () => {
        const min = minPrice ? parseFloat(minPrice) : undefined;
        const max = maxPrice ? parseFloat(maxPrice) : undefined;
        onPriceRange(min, max);
    };

    const hasActiveFilters =
        filters.search ||
        filters.category ||
        filters.inStock !== undefined ||
        filters.minPrice !== undefined ||
        filters.maxPrice !== undefined;

    return (
        <div className="bg-white border border-gray-100 rounded-2xl p-5 space-y-5">

            {/* Search */}
            <div>
                <label className="block text-xs font-medium text-gray-500 mb-1.5 uppercase tracking-wide">
                    Search
                </label>
                <div className="relative">
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm">
            &#128269;
          </span>
                    <input
                        type="text"
                        value={searchInput}
                        onChange={e => setSearchInput(e.target.value)}
                        placeholder="Search by name..."
                        className="w-full pl-9 pr-4 py-2 text-sm border border-gray-200 rounded-lg
                       focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                    />
                    {searchInput && (
                        <button
                            onClick={() => { setSearchInput(''); onSearch(''); }}
                            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 text-sm"
                        >
                            ✕
                        </button>
                    )}
                </div>
            </div>

            {/* Category */}
            <div>
                <label className="block text-xs font-medium text-gray-500 mb-1.5 uppercase tracking-wide">
                    Category
                </label>
                <div className="flex flex-wrap gap-2">
                    {CATEGORIES.map(cat => (
                        <button
                            key={cat}
                            onClick={() => onCategory(filters.category === cat ? undefined : cat)}
                            className={[
                                'px-3 py-1.5 text-xs font-medium rounded-lg border transition-colors',
                                filters.category === cat
                                    ? 'bg-indigo-600 text-white border-indigo-600'
                                    : 'border-gray-200 text-gray-600 hover:border-indigo-300 hover:text-indigo-600',
                            ].join(' ')}
                        >
                            {cat}
                        </button>
                    ))}
                </div>
            </div>

            {/* In Stock toggle */}
            <div className="flex items-center justify-between">
                <label className="text-xs font-medium text-gray-500 uppercase tracking-wide">
                    In Stock Only
                </label>
                <button
                    role="switch"
                    aria-checked={!!filters.inStock}
                    onClick={() => onInStock(filters.inStock ? undefined : true)}
                    className={[
                        'relative w-10 h-5 rounded-full transition-colors duration-200',
                        filters.inStock ? 'bg-indigo-600' : 'bg-gray-200',
                    ].join(' ')}
                >
          <span
              className={[
                  'absolute top-0.5 w-4 h-4 bg-white rounded-full shadow transition-transform duration-200',
                  filters.inStock ? 'translate-x-5' : 'translate-x-0.5',
              ].join(' ')}
          />
                </button>
            </div>

            {/* Sort */}
            <div>
                <label className="block text-xs font-medium text-gray-500 mb-1.5 uppercase tracking-wide">
                    Sort by
                </label>
                <div className="flex gap-2">
                    <select
                        value={filters.sortBy ?? 'name'}
                        onChange={e => onSortBy(e.target.value as SortField)}
                        className="flex-1 text-sm border border-gray-200 rounded-lg px-3 py-2
                       focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    >
                        {SORT_FIELDS.map(f => (
                            <option key={f.value} value={f.value}>{f.label}</option>
                        ))}
                    </select>
                    <button
                        onClick={() => onSortDirection(filters.sortDirection === 'asc' ? 'desc' : 'asc')}
                        className="px-3 py-2 border border-gray-200 rounded-lg text-sm text-gray-600
                       hover:bg-gray-50 transition-colors"
                        title={filters.sortDirection === 'asc' ? 'Ascending' : 'Descending'}
                    >
                        {filters.sortDirection === 'asc' ? '↑ Asc' : '↓ Desc'}
                    </button>
                </div>
            </div>

            {/* Price Range */}
            <div>
                <label className="block text-xs font-medium text-gray-500 mb-1.5 uppercase tracking-wide">
                    Price Range
                </label>
                <div className="flex items-center gap-2">
                    <input
                        type="number"
                        value={minPrice}
                        onChange={e => setMinPrice(e.target.value)}
                        placeholder="Min"
                        min={0}
                        className="w-full text-sm border border-gray-200 rounded-lg px-3 py-2
                       focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <span className="text-gray-400 text-sm flex-shrink-0">–</span>
                    <input
                        type="number"
                        value={maxPrice}
                        onChange={e => setMaxPrice(e.target.value)}
                        placeholder="Max"
                        min={0}
                        className="w-full text-sm border border-gray-200 rounded-lg px-3 py-2
                       focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                </div>
                <Button
                    variant="secondary"
                    size="sm"
                    onClick={handlePriceApply}
                    className="w-full mt-2"
                >
                    Apply Price
                </Button>
            </div>

            {/* Reset */}
            {hasActiveFilters && (
                <Button variant="ghost" size="sm" onClick={onReset} className="w-full">
                    Clear all filters
                </Button>
            )}
        </div>
    );
};