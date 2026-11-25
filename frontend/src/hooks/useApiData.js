import { useCallback, useEffect, useState } from 'react';

export default function useApiData(loader, token, onExpired) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [revision, setRevision] = useState(0);
  const reload = useCallback(() => setRevision((value) => value + 1), []);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError('');
    loader(token, controller.signal)
      .then((result) => { if (!controller.signal.aborted) setData(result); })
      .catch((failure) => {
        if (controller.signal.aborted) return;
        if (failure.status === 401) onExpired();
        else setError(failure.message);
      })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [loader, token, onExpired, revision]);

  return { data, setData, loading, error, reload };
}
