import { useState, useCallback } from 'react';

export function useIdempotencyKey() {
  const [key, setKey] = useState(() => crypto.randomUUID());

  const resetIdempotencyKey = useCallback(() => {
    setKey(crypto.randomUUID());
  }, []);

  return { idempotencyKey: key, resetIdempotencyKey };
}
