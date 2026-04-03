import { useState, useEffect, useRef } from 'react';

interface UseCountdownResult {
  seconds: number;
  isExpired: boolean;
  formattedTime: string;
}

/**
 * Countdown hook that uses remainingSeconds from the server.
 * This avoids timezone issues between server and client.
 */
export const useCountdown = (initialSeconds: number): UseCountdownResult => {
  const [seconds, setSeconds] = useState<number>(Math.max(0, initialSeconds));
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    // Reset when initialSeconds changes (e.g., new reservation)
    setSeconds(Math.max(0, initialSeconds));

    if (initialSeconds <= 0) {
      return;
    }

    intervalRef.current = setInterval(() => {
      setSeconds(prev => {
        if (prev <= 1) {
          if (intervalRef.current) clearInterval(intervalRef.current);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [initialSeconds]);

  const isExpired = seconds === 0;

  const formattedTime = (() => {
    if (isExpired) return '00:00';
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  })();

  return { seconds, isExpired, formattedTime };
};