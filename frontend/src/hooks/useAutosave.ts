import { useEffect } from 'react';

export function useAutosave(key: string | null, value: string, delay = 500) {
  useEffect(() => {
    if (!key) return;
    const handle = setTimeout(() => {
      try {
        if (value) localStorage.setItem(key, value);
        else localStorage.removeItem(key);
      } catch {
        // localStorage unavailable — autosave is best-effort only
      }
    }, delay);
    return () => clearTimeout(handle);
  }, [key, value, delay]);
}

export function readAutosave(key: string | null): string {
  if (!key) return '';
  try {
    return localStorage.getItem(key) || '';
  } catch {
    return '';
  }
}

export function clearAutosave(key: string | null) {
  if (!key) return;
  try {
    localStorage.removeItem(key);
  } catch {
    // ignore
  }
}
