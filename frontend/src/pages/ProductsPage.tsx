import { useProducts, useProductFilters } from '../hooks/useProducts';
import { ProductFilters } from '../components/product/ProductFilters';
import { ProductGrid } from '../components/product/ProductGrid';
import { Pagination } from '../components/ui';

export const ProductsPage = () => {
  const {
    filters,
    setPage,
    setSearch,
    setCategory,
    setInStock,
    setSortBy,
    setSortDirection,
    setPriceRange,
    resetFilters,
  } = useProductFilters();

  const { data, isLoading, isError, isFetching, refetch } = useProducts(filters);

  const products = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;
  const currentPage = data?.currentPage ?? 0;

  return (
      <div className="min-h-screen bg-gray-50">
        {/* Header */}
        <div className="bg-white border-b border-gray-100">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-2xl font-bold text-gray-900">Products</h1>
                {!isLoading && (
                    <p className="text-sm text-gray-400 mt-0.5">
                      {totalElements} item{totalElements !== 1 ? 's' : ''} found
                    </p>
                )}
              </div>

              {/* Live indicator */}
              <div className="flex items-center gap-1.5 text-xs text-gray-400">
                <span className="w-1.5 h-1.5 rounded-full bg-green-400 animate-pulse" />
                Live stock
              </div>
            </div>
          </div>
        </div>

        {/* Body */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex flex-col lg:flex-row gap-8">

            {/* Sidebar filters */}
            <aside className="w-full lg:w-72 flex-shrink-0">
              <div className="lg:sticky lg:top-6">
                <ProductFilters
                    filters={filters}
                    onSearch={setSearch}
                    onCategory={setCategory}
                    onInStock={setInStock}
                    onSortBy={setSortBy}
                    onSortDirection={setSortDirection}
                    onPriceRange={setPriceRange}
                    onReset={resetFilters}
                />
              </div>
            </aside>

            {/* Main content */}
            <main className="flex-1 min-w-0">
              <ProductGrid
                  products={products}
                  isLoading={isLoading}
                  isError={isError}
                  isFetching={isFetching}
                  onRetry={() => refetch()}
              />

              <Pagination
                  currentPage={currentPage}
                  totalPages={totalPages}
                  totalElements={totalElements}
                  pageSize={filters.size ?? 12}
                  onPageChange={setPage}
              />
            </main>

          </div>
        </div>
      </div>
  );
};