import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError, messageOf } from '../api/http';

export function useRemote<T>(fetcher: () => Promise<T>, deps: unknown[]) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [errorStatus, setErrorStatus] = useState<number | null>(null);
  const sequence = useRef(0);
  // Caller supplies stable primitive dependencies.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  const reload = useCallback(async () => {
    const current = ++sequence.current;
    setLoading(true); setError(''); setErrorStatus(null);
    try { const next = await fetcher(); if(current === sequence.current)setData(next); return next; }
    catch (error) { if(current === sequence.current) {setError(messageOf(error)); setErrorStatus(error instanceof ApiError ? error.status : null);} return null; }
    finally { if(current === sequence.current)setLoading(false); }
  }, deps);
  useEffect(() => { void reload(); }, [reload]);
  return {data, loading, error, errorStatus, reload, setData};
}
export function useAction() {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  async function run<T>(action: () => Promise<T>, successMessage = ''): Promise<T | null> {
    if (busy) return null;
    setBusy(true); setError(''); setSuccess('');
    try { const value = await action(); setSuccess(successMessage); return value; }
    catch (error) { setError(messageOf(error)); return null; }
    finally { setBusy(false); }
  }
  return {busy, error, success, setError, setSuccess, run};
}
