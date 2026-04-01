import { useState, useEffect, useRef } from 'react';

interface UseCountdownResult {
  seconds: number;
  isExpired: boolean;
  formattedTime: string;
}

export const useCountdown = (expiresAt: string | null): UseCountdownResult => {
  const calculateRemaining = (): number => {
    if (!expiresAt) return 0;
    const diff = Math.floor((new Date(expiresAt).getTime() - Date.now()) / 1000);
    return Math.max(0, diff);
  };

  const [seconds, setSeconds] = useState<number>(calculateRemaining);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (!expiresAt) {
      setSeconds(0);
      return;
    }

    setSeconds(calculateRemaining());

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
  }, [expiresAt]);

  const isExpired = seconds === 0;

  const formattedTime = (() => {
    if (isExpired) return '00:00';
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  })();

  return { seconds, isExpired, formattedTime };
};