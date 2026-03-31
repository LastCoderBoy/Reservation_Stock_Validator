import { forwardRef, type HTMLAttributes } from 'react';

type CardVariant = 'default' | 'elevated' | 'bordered';

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  variant?: CardVariant;
  hover?: boolean;
}

const Card = forwardRef<HTMLDivElement, CardProps>(
  ({ variant = 'default', hover = false, className = '', children, ...props }, ref) => {
    const baseStyles = 'bg-white rounded-lg transition-all';

    const variantStyles: Record<CardVariant, string> = {
      default: 'border border-secondary-100',
      elevated: 'shadow-md',
      bordered: 'border-2 border-secondary-200',
    };

    const hoverStyles = hover ? 'hover:shadow-lg hover:border-accent-600 cursor-pointer' : '';

    return (
      <div
        ref={ref}
        className={`${baseStyles} ${variantStyles[variant]} ${hoverStyles} ${className}`}
        {...props}
      >
        {children}
      </div>
    );
  }
);

Card.displayName = 'Card';

// Card Header
interface CardHeaderProps extends HTMLAttributes<HTMLDivElement> {
  title?: string;
  subtitle?: string;
}

const CardHeader = forwardRef<HTMLDivElement, CardHeaderProps>(
  ({ title, subtitle, className = '', children, ...props }, ref) => {
    return (
      <div ref={ref} className={`p-6 border-b border-secondary-100 ${className}`} {...props}>
        {title && <h3 className="text-lg font-semibold text-text-primary">{title}</h3>}
        {subtitle && <p className="text-sm text-text-secondary mt-1">{subtitle}</p>}
        {children}
      </div>
    );
  }
);

CardHeader.displayName = 'CardHeader';

// Card Body
const CardBody = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className = '', children, ...props }, ref) => {
    return (
      <div ref={ref} className={`p-6 ${className}`} {...props}>
        {children}
      </div>
    );
  }
);

CardBody.displayName = 'CardBody';

// Card Footer
const CardFooter = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className = '', children, ...props }, ref) => {
    return (
      <div ref={ref} className={`p-6 border-t border-secondary-100 ${className}`} {...props}>
        {children}
      </div>
    );
  }
);

CardFooter.displayName = 'CardFooter';

export { Card, CardHeader, CardBody, CardFooter };
