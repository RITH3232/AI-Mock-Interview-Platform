import React, { Component, ErrorInfo, ReactNode } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { ToastProvider } from './hooks/useToast';
import { useTheme } from './hooks/useTheme';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { ResumeUpload } from './pages/ResumeUpload';
import { ResumeInsights } from './pages/ResumeInsights';
import { InterviewSetup } from './pages/InterviewSetup';
import { InterviewRoom } from './pages/InterviewRoom';
import { InterviewReport } from './pages/InterviewReport';
import { CandidateDashboard } from './pages/CandidateDashboard';
import { Leaderboard } from './pages/Leaderboard';
import { AdminDashboard } from './pages/AdminDashboard';

import { Landing } from './pages/Landing';

interface Props { children: ReactNode; }
interface State { hasError: boolean; error: Error | null; }

class ErrorBoundary extends Component<Props, State> {
  public state: State = { hasError: false, error: null };
  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }
  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Uncaught error:', error, errorInfo);
  }
  public render() {
    if (this.state.hasError) {
      return (
        <div className="p-8 text-red-500 bg-red-500/10 min-h-screen">
          <h1 className="text-2xl font-bold mb-4">React Rendering Crash!</h1>
          <pre className="whitespace-pre-wrap">{this.state.error?.stack}</pre>
        </div>
      );
    }
    return this.props.children;
  }
}

const App: React.FC = () => {
  useTheme(); // ensures the theme class is applied to <html> on first mount

  return (
    <Router>
      <ToastProvider>
        <div className="min-h-screen bg-background text-foreground">
          <Navbar />
          <ErrorBoundary>
            <Routes>
              <Route path="/" element={<Landing />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/dashboard" element={<CandidateDashboard />} />
              <Route path="/leaderboard" element={<Leaderboard />} />
              <Route path="/admin" element={<AdminDashboard />} />

              <Route path="/resume/upload" element={<ResumeUpload />} />
              <Route path="/resume/insights" element={<ResumeInsights />} />
              <Route path="/interview/setup" element={<InterviewSetup />} />
              <Route path="/interview/room/:id" element={<InterviewRoom />} />
              <Route path="/interview/report/:id" element={<InterviewReport />} />
            </Routes>
          </ErrorBoundary>
        </div>
      </ToastProvider>
    </Router>
  );
};

export default App;
