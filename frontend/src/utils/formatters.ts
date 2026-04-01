export const formatPrice = (price: number): string => {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
    }).format(price);
};

export const formatDate = (dateStr: string): string => {
    return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    }).format(new Date(dateStr));
};

export const formatCountdown = (seconds: number): string => {
    if (seconds <= 0) return '00:00';
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
};

export const formatStock = (available: number, total: number): string => {
    return `${available} / ${total} available`;
};

export const stockPercentage = (available: number, total: number): number => {
    if (total === 0) return 0;
    return Math.round((available / total) * 100);
};