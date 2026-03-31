import { useEffect, useState, useCallback } from 'react';
import { differenceInSeconds } from 'date-fns';

interface CountdownTimerProps {
  expiresAt: string;
  onExpire?: () => void;
}

export const CountdownTimer = ({ expiresAt, onExpire }: CountdownTimerProps) => {
  const [timeLeft, setTimeLeft] = useState<number>(0);
  const [isExpired, setIsExpired] = useState(false);

  const calculateTimeLeft = useCallback(() => {
    const now = new Date();
    const expiry = new Date(expiresAt);
    const seconds = differenceInSeconds(expiry, now);
    return Math.max(0, seconds);
  }, [expiresAt]);

  useEffect(() => {
    // Initial calculation
    const seconds = calculateTimeLeft();
    setTimeLeft(seconds);
    
    if (seconds === 0) {
      setIsExpired(true);
      onExpire?.();
      return;
    }

    // Update every second
    const interval = setInterval(() => {
      const remaining = calculateTimeLeft();
      setTimeLeft(remaining);

      if (remaining === 0 && !isExpired) {
        setIsExpired(true);
        onExpire?.();
        clearInterval(interval);
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [expiresAt, calculateTimeLeft, onExpire, isExpired]);

  const formatTime = (seconds: number): string => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const isUrgent = timeLeft > 0 && timeLeft <= 60; // Less than 1 minute
  const isCritical = timeLeft > 0 && timeLeft <= 30; // Less than 30 seconds

  if (isExpired || timeLeft === 0) {
    return (
      <div>
        <div className="text-sm text-urgency-danger mb-2">⏰ Time Expired!</div>
        <div className="text-4xl font-mono font-bold text-urgency-danger">
          00:00:00
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className={`text-sm mb-2 ${
        isCritical ? 'text-urgency-danger animate-pulse' : 
        isUrgent ? 'text-urgency-danger' : 
        'text-text-secondary'
      }`}>
        {isCritical ? '🚨 Expiring in seconds!' : isUrgent ? '⚠️ Time Running Out!' : 'Time Remaining:'}
      </div>
      <div className={`text-4xl font-mono font-bold ${
        isCritical ? 'text-urgency-danger animate-pulse' : 
        isUrgent ? 'text-urgency-danger' : 
        'text-accent-600'
      }`}>
        {formatTime(timeLeft)}
      </div>
    </div>
  );
};
