import type { ButtonHTMLAttributes } from 'react';

type Variant = 'primary' | 'secondary' | 'danger' | 'ghost';
type Size = 'sm' | 'md' | 'lg';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: Variant;
    size?: Size;
    loading?: boolean;
}

const variantClasses: Record<Variant, string> = {
    primary:   'bg-indigo-600 text-white hover:bg-indigo-700 disabled:bg-indigo-300',
    secondary: 'bg-gray-100 text-gray-800 hover:bg-gray-200 disabled:bg-gray-50 disabled:text-gray-400',
    danger:    'bg-red-600 text-white hover:bg-red-700 disabled:bg-red-300',
    ghost:     'bg-transparent text-indigo-600 hover:bg-indigo-50 disabled:text-gray-300',
};

const sizeClasses: Record<Size, string> = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-3 text-base',
};

export const Button = ({
                           variant = 'primary',
                           size = 'md',
                           loading = false,
                           disabled,
                           children,
                           className = '',
                           ...props
                       }: ButtonProps) => {
    return (
        <button
            disabled={disabled || loading}
            className={[
                'inline-flex items-center justify-center gap-2 font-medium rounded-lg',
                'transition-colors duration-150 cursor-pointer',
                'focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2',
                'disabled:cursor-not-allowed',
                variantClasses[variant],
                sizeClasses[size],
                className,
            ].join(' ')}
            {...props}
        >
            {loading && (
                <span className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin" />
            )}
            {children}
        </button>
    );
};