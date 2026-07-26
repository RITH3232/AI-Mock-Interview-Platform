import { useCallback, useEffect, useState } from 'react';

export type Theme = 'light' | 'dark';

const STORAGE_KEY = 'interviewiq-theme';

function getInitialTheme(): Theme {
  const stored = localStorage.getItem(STORAGE_KEY) as Theme | null;
  if (stored === 'light' || stored === 'dark') return stored;
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function applyTheme(theme: Theme) {
  document.documentElement.classList.toggle('dark', theme === 'dark');
}

let listeners: Array<(t: Theme) => void> = [];
let currentTheme: Theme | null = null;

export function useTheme() {
  const [theme, setThemeState] = useState<Theme>(() => {
    if (currentTheme === null) {
      currentTheme = getInitialTheme();
      applyTheme(currentTheme);
    }
    return currentTheme;
  });

  useEffect(() => {
    listeners.push(setThemeState);
    return () => {
      listeners = listeners.filter((l) => l !== setThemeState);
    };
  }, []);

  const setTheme = useCallback((next: Theme) => {
    currentTheme = next;
    localStorage.setItem(STORAGE_KEY, next);
    applyTheme(next);
    listeners.forEach((l) => l(next));
  }, []);

  const toggleTheme = useCallback(() => {
    setTheme(currentTheme === 'dark' ? 'light' : 'dark');
  }, [setTheme]);

  return { theme, setTheme, toggleTheme };
}
