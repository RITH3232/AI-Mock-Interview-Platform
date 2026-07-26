import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { logout } from '../store/authSlice';
import { api } from '../services/api';
import { LogOut, Home, User, BarChart, Sparkles, FileText, ChevronDown } from 'lucide-react';
import { ThemeToggle } from './ui/ThemeToggle';

export const Navbar: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const location = useLocation();
  const { user } = useSelector((state: any) => state.auth);
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!menuOpen) return;
    const handleClick = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) setMenuOpen(false);
    };
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setMenuOpen(false);
    };
    document.addEventListener('mousedown', handleClick);
    document.addEventListener('keydown', handleKey);
    return () => {
      document.removeEventListener('mousedown', handleClick);
      document.removeEventListener('keydown', handleKey);
    };
  }, [menuOpen]);

  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (err) {
      console.error('Logout failed:', err);
    } finally {
      dispatch(logout());
      navigate('/login');
    }
  };

  // Landing has its own bespoke header; auth pages and the interview room are full-bleed
  if (['/', '/login', '/register'].includes(location.pathname) || location.pathname.startsWith('/interview/room')) {
    return null;
  }

  return (
    <nav className="w-full bg-card/80 backdrop-blur-xl border-b border-border py-3.5 px-4 sm:px-6 flex items-center justify-between sticky top-0 z-50">
      <div className="flex items-center gap-6">
        <div
          className="flex items-center gap-2 cursor-pointer"
          onClick={() => navigate('/dashboard')}
        >
          <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-primary to-cyan-500 flex items-center justify-center shadow-sm shadow-primary/25">
            <Sparkles className="w-3.5 h-3.5 text-white" />
          </div>
          <h1 className="text-lg font-extrabold tracking-tight text-foreground">InterviewIQ</h1>
        </div>
        <div className="hidden md:flex gap-1">
          <button onClick={() => navigate('/dashboard')} className={`flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium transition-colors ${location.pathname === '/dashboard' ? 'bg-primary/10 text-primary' : 'text-muted-foreground hover:bg-accent'}`}>
            <Home className="w-4 h-4" /> <span>Dashboard</span>
          </button>
          <button onClick={() => navigate('/leaderboard')} className={`flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium transition-colors ${location.pathname === '/leaderboard' ? 'bg-primary/10 text-primary' : 'text-muted-foreground hover:bg-accent'}`}>
            <BarChart className="w-4 h-4" /> <span>Leaderboard</span>
          </button>
        </div>
      </div>
      <div className="flex items-center gap-3">
        <ThemeToggle />
        {user && (
          <div className="relative" ref={menuRef}>
            <button
              onClick={() => setMenuOpen((v) => !v)}
              aria-expanded={menuOpen}
              aria-haspopup="menu"
              className="flex items-center gap-2 pl-2 pr-1 py-1.5 rounded-xl text-sm font-medium text-foreground hover:bg-accent transition-colors border border-transparent hover:border-border"
            >
              <div className="w-6 h-6 rounded-full bg-primary/10 text-primary flex items-center justify-center border border-primary/20">
                <User className="w-3.5 h-3.5" />
              </div>
              <span className="hidden sm:inline max-w-[10rem] truncate">{user.email}</span>
              <ChevronDown className={`w-3.5 h-3.5 text-muted-foreground transition-transform ${menuOpen ? 'rotate-180' : ''}`} />
            </button>

            {menuOpen && (
              <div
                role="menu"
                className="absolute right-0 mt-2 w-64 rounded-2xl border border-border bg-card shadow-xl overflow-hidden z-50"
              >
                <div className="px-4 py-3 border-b border-border">
                  <p className="text-sm font-semibold text-foreground truncate">{user.email}</p>
                  <p className="text-xs text-muted-foreground capitalize mt-0.5">{user.role}</p>
                </div>
                <div className="p-1.5">
                  <button
                    onClick={() => { setMenuOpen(false); navigate('/dashboard'); }}
                    className="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium text-foreground hover:bg-accent transition-colors text-left"
                  >
                    <Home className="w-4 h-4 text-muted-foreground" /> Dashboard
                  </button>
                  <button
                    onClick={() => { setMenuOpen(false); navigate('/resume/insights'); }}
                    className="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium text-foreground hover:bg-accent transition-colors text-left"
                  >
                    <FileText className="w-4 h-4 text-muted-foreground" /> Resume Insights
                  </button>
                  <button
                    onClick={() => { setMenuOpen(false); handleLogout(); }}
                    className="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium text-destructive hover:bg-destructive/10 transition-colors text-left"
                  >
                    <LogOut className="w-4 h-4" /> Sign out
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
        {!user && (
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 px-3.5 py-2 text-sm font-medium text-muted-foreground hover:text-destructive hover:bg-destructive/10 rounded-xl transition-colors border border-transparent hover:border-destructive/20"
          >
            <LogOut className="w-4 h-4" />
            <span className="hidden sm:inline">Logout</span>
          </button>
        )}
      </div>
    </nav>
  );
};
