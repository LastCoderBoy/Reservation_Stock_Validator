import { useCountdown } from '../../hooks/useCountdown';

interface CountdownTimerProps {
    remainingSeconds: number;
    onExpired?: () => void;
}

export const CountdownTimer = ({ remainingSeconds, onExpired }: CountdownTimerProps) => {
    const { formattedTime, isExpired, seconds } = useCountdown(remainingSeconds);

    // Notify parent when timer hits zero
    if (isExpired && onExpired) {
        onExpired();
    }

    const isUrgent = seconds > 0 && seconds <= 60;

    return (
        <div className={`flex items-center gap-2 px-4 py-3 rounded-xl text-sm font-medium
      ${isExpired
            ? 'bg-red-50 text-red-700 border border-red-100'
            : isUrgent
                ? 'bg-amber-50 text-amber-700 border border-amber-100'
                : 'bg-indigo-50 text-indigo-700 border border-indigo-100'
        }`}
        >
            {/* Clock icon */}
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" className="flex-shrink-0">
                <circle cx="8" cy="8" r="7" stroke="currentColor" strokeWidth="1.5"/>
                <path d="M8 4.5V8L10.5 10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>

            {isExpired ? (
                <span>Reservation expired</span>
            ) : (
                <span>
          Reserved — expires in{' '}
                    <span className={`font-bold tabular-nums ${isUrgent ? 'text-amber-800' : ''}`}>
            {formattedTime}
          </span>
        </span>
            )}

            {/* Pulse dot when urgent */}
            {isUrgent && !isExpired && (
                <span className="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse ml-auto" />
            )}
        </div>
    );
};