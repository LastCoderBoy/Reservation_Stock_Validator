// ─── Badge ───────────────────────────────────────────────────────
type BadgeVariant = 'green' | 'red' | 'yellow' | 'gray' | 'indigo';

interface BadgeProps {
    variant?: BadgeVariant;
    children: React.ReactNode;
}

const badgeClasses: Record<BadgeVariant, string> = {
    green:  'bg-green-100 text-green-800',
    red:    'bg-red-100 text-red-800',
    yellow: 'bg-yellow-100 text-yellow-800',
    gray:   'bg-gray-100 text-gray-700',
    indigo: 'bg-indigo-100 text-indigo-800',
};

export const Badge = ({ variant = 'gray', children }: BadgeProps) => (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${badgeClasses[variant]}`}>
    {children}
  </span>
);

// ─── Spinner ─────────────────────────────────────────────────────
interface SpinnerProps {
    size?: 'sm' | 'md' | 'lg';
    className?: string;
}

const spinnerSize = { sm: 'w-4 h-4', md: 'w-8 h-8', lg: 'w-12 h-12' };

export const Spinner = ({ size = 'md', className = '' }: SpinnerProps) => (
    <div className={`${spinnerSize[size]} border-4 border-gray-200 border-t-indigo-600 rounded-full animate-spin ${className}`} />
);

// ─── ErrorMessage ─────────────────────────────────────────────────
interface ErrorMessageProps {
    message: string;
    onRetry?: () => void;
}

export const ErrorMessage = ({ message, onRetry }: ErrorMessageProps) => (
    <div className="flex flex-col items-center justify-center py-12 gap-3">
        <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center">
            <span className="text-red-600 text-xl font-bold">!</span>
        </div>
        <p className="text-gray-600 text-sm">{message}</p>
        {onRetry && (
            <button
                onClick={onRetry}
                className="text-indigo-600 text-sm underline hover:text-indigo-800"
            >
                Try again
            </button>
        )}
    </div>
);

// ─── Pagination ───────────────────────────────────────────────────
interface PaginationProps {
    currentPage: number;      // 0-indexed
    totalPages: number;
    totalElements: number;
    pageSize: number;
    onPageChange: (page: number) => void;
}

export const Pagination = ({
                               currentPage,
                               totalPages,
                               totalElements,
                               pageSize,
                               onPageChange,
                           }: PaginationProps) => {
    if (totalPages <= 1) return null;

    const from = currentPage * pageSize + 1;
    const to = Math.min((currentPage + 1) * pageSize, totalElements);

    const pages: (number | '...')[] = [];
    if (totalPages <= 7) {
        for (let i = 0; i < totalPages; i++) pages.push(i);
    } else {
        pages.push(0);
        if (currentPage > 2) pages.push('...');
        for (let i = Math.max(1, currentPage - 1); i <= Math.min(totalPages - 2, currentPage + 1); i++) {
            pages.push(i);
        }
        if (currentPage < totalPages - 3) pages.push('...');
        pages.push(totalPages - 1);
    }

    return (
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3 mt-6">
            <p className="text-sm text-gray-500">
                Showing <span className="font-medium">{from}–{to}</span> of{' '}
                <span className="font-medium">{totalElements}</span> products
            </p>
            <div className="flex items-center gap-1">
                <button
                    onClick={() => onPageChange(currentPage - 1)}
                    disabled={currentPage === 0}
                    className="px-3 py-1.5 text-sm rounded-lg border border-gray-200 text-gray-600
                     hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                >
                    Prev
                </button>

                {pages.map((page, i) =>
                    page === '...' ? (
                        <span key={`ellipsis-${i}`} className="px-2 text-gray-400 text-sm">…</span>
                    ) : (
                        <button
                            key={page}
                            onClick={() => onPageChange(page as number)}
                            className={[
                                'px-3 py-1.5 text-sm rounded-lg border transition-colors',
                                page === currentPage
                                    ? 'bg-indigo-600 text-white border-indigo-600'
                                    : 'border-gray-200 text-gray-600 hover:bg-gray-50',
                            ].join(' ')}
                        >
                            {(page as number) + 1}
                        </button>
                    )
                )}

                <button
                    onClick={() => onPageChange(currentPage + 1)}
                    disabled={currentPage >= totalPages - 1}
                    className="px-3 py-1.5 text-sm rounded-lg border border-gray-200 text-gray-600
                     hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                >
                    Next
                </button>
            </div>
        </div>
    );
};