import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { motion } from 'framer-motion';
import {
  Sparkles, Code2, FileText, Users, BarChart3, ArrowRight, CheckCircle,
  Mic, LayoutGrid, Star
} from 'lucide-react';
import { RootState } from '../store';
import { ThemeToggle } from '../components/ui/ThemeToggle';

const FEATURES = [
  {
    icon: LayoutGrid,
    title: 'Fully Personalized Setup',
    desc: 'Pick your role, skills, difficulty, interview type, and even a target company — every question is generated around that exact combination.',
  },
  {
    icon: Code2,
    title: 'Technical, HR & System Design',
    desc: 'Practice DSA, backend, frontend, behavioral, or architecture rounds with formats that match reality: MCQ, code, or open-ended.',
  },
  {
    icon: FileText,
    title: 'Resume-Aware Interviews',
    desc: 'Upload your resume to generate questions grounded in your actual projects, stack, and experience — not generic templates.',
  },
  {
    icon: BarChart3,
    title: 'Real Analytics, Not Guesses',
    desc: 'Every answer is scored on technical accuracy, communication, and confidence, with a full report and downloadable PDF.',
  },
];

const STEPS = [
  { icon: LayoutGrid, title: 'Configure', desc: 'Choose role, skills, difficulty, and interview type in one screen.' },
  { icon: Mic, title: 'Practice', desc: 'Answer by voice, text, or code with a real-time timer and progress tracker.' },
  { icon: BarChart3, title: 'Improve', desc: 'Get a scored report with strengths, gaps, and a personalized roadmap.' },
];

const STATS = [
  { value: '16+', label: 'Skill domains' },
  { value: '5', label: 'Interview types' },
  { value: '4', label: 'Difficulty tiers' },
  { value: '100%', label: 'Personalized' },
];

export const Landing: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useSelector((state: RootState) => state.auth);

  const handleStart = () => navigate(isAuthenticated ? '/interview/setup' : '/register');

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col relative overflow-hidden">
      {/* Background decoration */}
      <div className="absolute top-[-15%] left-[8%] w-[550px] h-[550px] bg-primary/15 rounded-full blur-[130px] pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[600px] h-[600px] bg-cyan-500/10 rounded-full blur-[140px] pointer-events-none" />
      <div
        className="absolute inset-0 opacity-[0.03] pointer-events-none"
        style={{
          backgroundImage:
            'linear-gradient(hsl(var(--foreground)) 1px, transparent 1px), linear-gradient(90deg, hsl(var(--foreground)) 1px, transparent 1px)',
          backgroundSize: '56px 56px',
        }}
      />

      {/* Navbar */}
      <header className="w-full max-w-7xl mx-auto px-6 py-6 flex items-center justify-between z-10 sticky top-0">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-cyan-500 flex items-center justify-center shadow-md shadow-primary/25">
            <Sparkles className="w-4 h-4 text-white" />
          </div>
          <span className="text-xl font-extrabold tracking-tight">InterviewIQ</span>
        </div>
        <div className="flex items-center gap-3">
          <ThemeToggle />
          {isAuthenticated ? (
            <button
              onClick={() => navigate('/dashboard')}
              className="px-4 py-2 bg-primary text-primary-foreground text-sm font-semibold rounded-xl hover:bg-primary/90 shadow-sm transition-all"
            >
              Dashboard
            </button>
          ) : (
            <>
              <button
                onClick={() => navigate('/login')}
                className="hidden sm:block text-sm font-semibold text-muted-foreground hover:text-foreground transition-colors px-3"
              >
                Sign In
              </button>
              <button
                onClick={() => navigate('/register')}
                className="px-4 py-2 bg-primary text-primary-foreground text-sm font-semibold rounded-xl hover:bg-primary/90 shadow-sm transition-all"
              >
                Get Started
              </button>
            </>
          )}
        </div>
      </header>

      {/* Hero */}
      <section className="flex-1 flex flex-col items-center justify-center text-center px-6 py-16 sm:py-24 z-10 max-w-5xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="space-y-6"
        >
          <span className="inline-flex items-center gap-2 px-3 py-1 bg-primary/10 border border-primary/20 text-primary rounded-full text-xs font-semibold uppercase tracking-wider">
            <CheckCircle className="w-3.5 h-3.5" />
            <span>Personalized AI Interview Prep</span>
          </span>

          <h1 className="text-4xl sm:text-6xl md:text-7xl font-black tracking-tight leading-[1.08] text-foreground">
            Interview prep that actually
            <br className="hidden sm:block" />
            <span className="bg-gradient-to-r from-primary via-cyan-400 to-primary bg-clip-text text-transparent bg-[length:200%_auto] animate-[gradient_6s_linear_infinite]">
              {' '}knows your role
            </span>
          </h1>

          <p className="max-w-2xl mx-auto text-base sm:text-xl text-muted-foreground font-medium leading-relaxed">
            Pick your role, skills, difficulty, and target company. InterviewIQ generates a fully
            personalized mock interview and scores your answers on technical depth, communication, and confidence.
          </p>

          <div className="pt-4 flex flex-col sm:flex-row items-center justify-center gap-4">
            <button
              onClick={handleStart}
              className="w-full sm:w-auto px-8 py-4 bg-gradient-to-r from-primary to-cyan-500 text-primary-foreground font-bold rounded-2xl shadow-lg shadow-primary/25 hover:shadow-xl hover:brightness-105 transition-all flex items-center justify-center gap-2 group"
            >
              <span>Start Interview</span>
              <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </button>
            <button
              onClick={() => navigate(isAuthenticated ? '/dashboard' : '/login')}
              className="w-full sm:w-auto px-8 py-4 bg-card border border-border hover:border-primary/50 text-foreground font-bold rounded-2xl hover:bg-accent/30 transition-all"
            >
              {isAuthenticated ? 'View Dashboard' : 'I already have an account'}
            </button>
          </div>

          {/* Stat strip */}
          <div className="pt-10 grid grid-cols-2 sm:grid-cols-4 gap-6 sm:gap-10 max-w-2xl mx-auto">
            {STATS.map((s, i) => (
              <div key={i} className="flex flex-col items-center">
                <span className="text-2xl sm:text-3xl font-black text-foreground">{s.value}</span>
                <span className="text-xs text-muted-foreground font-medium mt-1">{s.label}</span>
              </div>
            ))}
          </div>
        </motion.div>
      </section>

      {/* Feature Grid */}
      <section className="w-full max-w-7xl mx-auto px-6 py-16 sm:py-20 z-10 border-t border-border/50">
        <div className="text-center mb-12 space-y-3">
          <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight">Everything you need to walk in ready</h2>
          <p className="text-muted-foreground max-w-xl mx-auto">
            Built to feel like a real interview loop, not a quiz app.
          </p>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {FEATURES.map((feat, i) => {
            const Icon = feat.icon;
            return (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 40 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: i * 0.08 }}
                whileHover={{ y: -6 }}
                className="p-6 bg-card border border-border rounded-3xl space-y-4 shadow-sm hover:shadow-lg hover:border-primary/40 transition-all flex flex-col justify-between"
              >
                <div className="space-y-4">
                  <div className="p-3.5 bg-primary/10 text-primary w-fit rounded-2xl border border-primary/20">
                    <Icon className="w-6 h-6" />
                  </div>
                  <h3 className="text-lg font-bold tracking-tight text-foreground">{feat.title}</h3>
                  <p className="text-sm text-muted-foreground leading-relaxed">{feat.desc}</p>
                </div>
              </motion.div>
            );
          })}
        </div>
      </section>

      {/* How it works */}
      <section className="w-full max-w-5xl mx-auto px-6 py-16 sm:py-20 z-10 border-t border-border/50">
        <div className="text-center mb-12 space-y-3">
          <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight">Three steps. Full loop.</h2>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-8">
          {STEPS.map((step, i) => {
            const Icon = step.icon;
            return (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: i * 0.1 }}
                className="relative flex flex-col items-center text-center gap-3"
              >
                <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-primary/20 to-cyan-500/20 border border-primary/20 flex items-center justify-center text-primary">
                  <Icon className="w-6 h-6" />
                </div>
                <span className="text-xs font-bold text-primary uppercase tracking-wider">Step {i + 1}</span>
                <h3 className="font-bold text-lg">{step.title}</h3>
                <p className="text-sm text-muted-foreground max-w-xs">{step.desc}</p>
              </motion.div>
            );
          })}
        </div>
      </section>

      {/* CTA band */}
      <section className="w-full max-w-5xl mx-auto px-6 py-4 pb-20 z-10">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          className="relative overflow-hidden rounded-3xl border border-primary/20 bg-gradient-to-br from-primary/10 via-card to-cyan-500/5 p-10 sm:p-14 text-center space-y-5"
        >
          <div className="flex items-center justify-center gap-1 text-amber-500">
            {Array.from({ length: 5 }).map((_, i) => <Star key={i} className="w-4 h-4 fill-current" />)}
          </div>
          <h2 className="text-2xl sm:text-3xl font-extrabold tracking-tight">Ready to see where you stand?</h2>
          <p className="text-muted-foreground max-w-lg mx-auto">
            Set up your first personalized mock interview in under a minute.
          </p>
          <button
            onClick={handleStart}
            className="inline-flex items-center gap-2 px-8 py-4 bg-gradient-to-r from-primary to-cyan-500 text-primary-foreground font-bold rounded-2xl shadow-lg shadow-primary/25 hover:shadow-xl hover:brightness-105 transition-all group"
          >
            <Users className="w-5 h-5" />
            <span>Start Interview</span>
            <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
          </button>
        </motion.div>
      </section>

      {/* Footer */}
      <footer className="w-full border-t border-border/50 py-8 text-center text-xs text-muted-foreground z-10">
        <p>© 2026 InterviewIQ. Built for engineers who want to walk in ready.</p>
      </footer>

      <style>{`
        @keyframes gradient {
          to { background-position: 200% center; }
        }
      `}</style>
    </div>
  );
};
