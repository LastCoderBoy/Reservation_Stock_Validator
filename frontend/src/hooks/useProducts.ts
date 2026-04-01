import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { useState, useCallback } from 'react';
import { productApi } from '../api/products';
import type { ProductFilterParams, SortField, SortDirection, Category } from '../types';

export const PRODUCTS_QUERY_KEY = 'products';

// ─── useProducts ─────────────────────────────────────────────────
// Fetches paginated product list. Refetches every 5s for live stock.
export const useProducts = (filters: ProductFilterParams) => {
    return useQuery({
        queryKey: [PRODUCTS_QUERY_KEY, filters],
        queryFn: () => productApi.getAll(filters),
        placeholderData: keepPreviousData,   // keeps previous page visible while fetching next
        refetchInterval: 5000,               // poll stock every 5s
        staleTime: 4000,
    });
};

// ─── useProductFilters ────────────────────────────────────────────
// Manages all filter/sort/pagination state in one place.
export const useProductFilters = () => {
    const [filters, setFilters] = useState<ProductFilterParams>({
        page: 0,
        size: 12,
        sortBy: 'name',
        sortDirection: 'asc',
        search: '',
        category: undefined,
        inStock: undefined,
        minPrice: undefined,
        maxPrice: undefined,
    });

    const setPage = useCallback((page: number) => {
        setFilters(prev => ({ ...prev, page }));
    }, []);

    const setSearch = useCallback((search: string) => {
        setFilters(prev => ({ ...prev, search, page: 0 }));
    }, []);

    const setCategory = useCallback((category: Category | undefined) => {
        setFilters(prev => ({ ...prev, category, page: 0 }));
    }, []);

    const setInStock = useCallback((inStock: boolean | undefined) => {
        setFilters(prev => ({ ...prev, inStock, page: 0 }));
    }, []);

    const setSortBy = useCallback((sortBy: SortField) => {
        setFilters(prev => ({ ...prev, sortBy, page: 0 }));
    }, []);

    const setSortDirection = useCallback((sortDirection: SortDirection) => {
        setFilters(prev => ({ ...prev, sortDirection, page: 0 }));
    }, []);

    const setPriceRange = useCallback((minPrice?: number, maxPrice?: number) => {
        setFilters(prev => ({ ...prev, minPrice, maxPrice, page: 0 }));
    }, []);

    const resetFilters = useCallback(() => {
        setFilters({
            page: 0,
            size: 12,
            sortBy: 'name',
            sortDirection: 'asc',
            search: '',
            category: undefined,
            inStock: undefined,
            minPrice: undefined,
            maxPrice: undefined,
        });
    }, []);

    return {
        filters,
        setPage,
        setSearch,
        setCategory,
        setInStock,
        setSortBy,
        setSortDirection,
        setPriceRange,
        resetFilters,
    };
};