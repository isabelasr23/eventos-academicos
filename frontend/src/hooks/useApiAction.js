import { useEffect, useRef, useState } from 'react';

export default function useApiAction(onExpired) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const controllerRef = useRef(null);
  useEffect(() => () => controllerRef.current?.abort(), []);

  function clear() { setError(''); setMessage(''); }

  async function run(action, successMessage, onSuccess) {
    if (controllerRef.current && !controllerRef.current.signal.aborted) return;
    const controller = new AbortController();
    controllerRef.current = controller;
    clear();
    setBusy(true);
    try {
      const result = await action(controller.signal);
      if (!controller.signal.aborted) {
        setMessage(successMessage);
        onSuccess?.(result);
      }
    } catch (failure) {
      if (controller.signal.aborted) return;
      if (failure.status === 401) onExpired();
      else setError(failure.message);
    } finally {
      if (!controller.signal.aborted) setBusy(false);
      if (controllerRef.current === controller) controllerRef.current = null;
    }
  }

  return { busy, error, message, run, clear };
}
