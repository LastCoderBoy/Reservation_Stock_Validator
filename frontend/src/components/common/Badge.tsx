import { forwardRef, type HTMLAttributes } from 'react';

type BadgeVariant = 'success' | 'warning' | 'danger' | 'info' | 'neutral';
type BadgeSize = 'sm' | 'md' | 'lg';

interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  variant?: BadgeVariant;
  size?: BadgeSize;
  icon?: React.ReactNode;
  dot?: boolean;
}

export const Badge = forwardRef<HTMLSpanElement, BadgeProps>(
  ({ variant = 'neutral', size = 'md', icon, dot = false, className = '', children, ...props }, ref) => {
    const baseStyles = 'inline-flex items-center font-semibold rounded-full';

    const variantStyles: Record<BadgeVariant, string> = {
      success: 'bg-success/10 text-success',
      warning: 'bg-urgency-warning/10 text-urgency-warning',
      danger: 'bg-urgency-danger/10 text-urgency-danger',
      info: 'bg-accent-600/10 text-accent-600',
      neutral: 'bg-secondary-100 text-text-secondary',
    };

    const sizeStyles: Record<BadgeSize, string> = {
      sm: 'px-2 py-0.5 text-xs gap-1',
      md: 'px-3 py-1 text-sm gap-1.5',
      lg: 'px-4 py-1.5 text-base gap-2',
    };

    const dotColor: Record<BadgeVariant, string> = {
      success: 'bg-success',
      warning: 'bg-urgency-warning',
      danger: 'bg-urgency-danger',
      info: 'bg-accent-600',
      neutral: 'bg-text-secondary',
    };

    return (
      <span
        ref={ref}
        className={`${baseStyles} ${variantStyles[variant]} ${sizeStyles[size]} ${className}`}
        {...props}
      >
        {dot && <span className={`w-2 h-2 rounded-full ${dotColor[variant]}`}></span>}
        {icon && <span className="inline-flex">{icon}</span>}
        {children}
      </span>
    );
  }
);

Badge.displayName = 'Badge';
