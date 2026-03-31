import { forwardRef, type HTMLAttributes } from 'react';

type SpinnerSize = 'sm' | 'md' | 'lg' | 'xl';
type SpinnerColor = 'primary' | 'white' | 'accent';

interface SpinnerProps extends HTMLAttributes<HTMLDivElement> {
  size?: SpinnerSize;
  color?: SpinnerColor;
  label?: string;
}

export const Spinner = forwardRef<HTMLDivElement, SpinnerProps>(
  ({ size = 'md', color = 'accent', label, className = '', ...props }, ref) => {
    const sizeStyles: Record<SpinnerSize, string> = {
      sm: 'h-4 w-4 border-2',
      md: 'h-8 w-8 border-2',
      lg: 'h-12 w-12 border-3',
      xl: 'h-16 w-16 border-4',
    };

    const colorStyles: Record<SpinnerColor, string> = {
      primary: 'border-text-primary border-b-transparent',
      white: 'border-white border-b-transparent',
      accent: 'border-accent-600 border-b-transparent',
    };

    return (
      <div ref={ref} className={`flex flex-col items-center justify-center gap-3 ${className}`} {...props}>
        <div
          className={`animate-spin rounded-full ${sizeStyles[size]} ${colorStyles[color]}`}
          role="status"
          aria-label={label || 'Loading'}
        ></div>
        {label && <span className="text-sm text-text-secondary">{label}</span>}
      </div>
    );
  }
);

Spinner.displayName = 'Spinner';

// Full page spinner
export const PageSpinner = ({ label = 'Loading...' }: { label?: string }) => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-primary-50">
      <Spinner size="lg" label={label} />
    </div>
  );
};
