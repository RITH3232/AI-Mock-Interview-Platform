import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { api } from '../services/api';
import { RootState } from '../store';
import { analyticsApi } from '../services/analyticsApi';
import { setDashboardData, setLoading, setError } from '../store/analyticsSlice';
import { Loader2, Award, TrendingUp, Star, Target, Zap, Clock, FileText, ChevronRight } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Link, useNavigate } from 'react-router-dom';
import { motion, Variants } from 'framer-motion';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { EmptyState } from '../components/ui/EmptyState';

export const CandidateDashboard: React.FC = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { dashboardData, isLoading, error } = useSelector((state: RootState) => state.analytics);
  const { currentResume } = useSelector((state: RootState) => state.resume);

  useEffect(() => {
    const fetchDashboard = async () => {
      dispatch(setLoading(true));
      try {
        const { data } = await analyticsApi.getDashboard();
        dispatch(setDashboardData(data));
      } catch (err: any) {
        dispatch(setError(err.message));
      } finally {
        dispatch(setLoading(false));
      }
    };
    fetchDashboard();
  }, [dispatch]);

  if (isLoading || !dashboardData) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background text-foreground">
        <Loader2 className="w-12 h-12 animate-spin text-primary" />
      </div>
    );
  }

  const { analytics, leaderboard, rank, recentHistory } = dashboardData;

  // Trend data for charts
  const trendData = recentHistory.length > 0 ? recentHistory.map((h: any, i: number) => ({
    name: `Session ${i + 1}`,
    score: h.score
  })).reverse() : [{ name: 'No Data', score: 0 }];

  const containerVariants: Variants = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const itemVariants: Variants = {
    hidden: { opacity: 0, y: 20 },
    show: { opacity: 1, y: 0, transition: { type: "spring", stiffness: 100 } }
  };

  return (
    <div className="min-h-screen bg-background text-foreground p-8 relative overflow-hidden">
      {/* Decorative Blur Backgrounds */}
      <div className="absolute top-[-20%] right-[-10%] w-[600px] h-[600px] bg-primary/10 rounded-full blur-[140px] pointer-events-none" />
      <div className="absolute bottom-[-10%] left-[-10%] w-[500px] h-[500px] bg-primary/5 rounded-full blur-[120px] pointer-events-none" />

      <motion.div 
        variants={containerVariants}
        initial="hidden"
        animate="show"
        className="max-w-7xl mx-auto space-y-10 relative z-10"
      >
        {/* Upper Header */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-border pb-6">
          <div>
            <h1 className="text-4xl font-extrabold tracking-tight">Dashboard</h1>
            <p className="text-sm text-muted-foreground mt-1.5">Monitor your progress, scores, and mock milestones.</p>
          </div>
          <div className="flex items-center gap-3">
            <button 
              onClick={async () => {
                try { await api.post('/auth/logout'); } catch(e) {}
                dispatch({ type: 'auth/logout' });
                navigate('/login');
              }}
              className="px-5 py-2.5 border border-border hover:border-destructive/30 text-sm font-semibold rounded-xl text-muted-foreground hover:text-destructive hover:bg-destructive/5 transition-all"
            >
              Sign Out
            </button>
            <Link 
              to="/interview/setup" 
              className="px-6 py-2.5 bg-primary text-primary-foreground text-sm font-bold rounded-xl hover:bg-primary/95 shadow-md shadow-primary/20 transition-all flex items-center gap-2"
            >
              <span>New Mock Interview</span>
              <ChevronRight className="w-4 h-4" />
            </Link>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {[
            { label: "Average Score", val: `${analytics.averageScore || 0}/100`, icon: Target, desc: "Evaluated accuracy", color: "text-primary bg-primary/10" },
            { label: "Total XP", val: leaderboard.xp || 0, icon: Star, desc: "Gamification progress", color: "text-yellow-500 bg-yellow-500/10" },
            { label: "Global Rank", val: `#${rank || '-'}`, icon: TrendingUp, desc: "Community placement", color: "text-emerald-500 bg-emerald-500/10" },
            { label: "Streak", val: `${analytics.streakInfo?.current || 0} days`, icon: Zap, desc: "Consecutive practice", color: "text-orange-500 bg-orange-500/10" }
          ].map((item, index) => {
            const Icon = item.icon;
            return (
              <motion.div key={index} variants={itemVariants}>
                <Card hover className="p-6">
                  <div className="flex justify-between items-start mb-4">
                    <span className="text-sm font-semibold text-muted-foreground">{item.label}</span>
                    <div className={`p-2 rounded-xl border border-border/50 ${item.color}`}>
                      <Icon className="w-4 h-4" />
                    </div>
                  </div>
                  <div className="text-3xl font-black tracking-tight">{item.val}</div>
                  <p className="text-xs text-muted-foreground mt-1.5">{item.desc}</p>
                </Card>
              </motion.div>
            );
          })}
        </div>

        {/* Primary Content Panels */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          
          {/* Trend Chart */}
          <motion.div 
            variants={itemVariants}
            className="lg:col-span-2 p-6 bg-card border border-border rounded-3xl shadow-sm flex flex-col justify-between"
          >
            <div className="mb-6">
              <h3 className="text-lg font-bold">Performance Progression</h3>
              <p className="text-xs text-muted-foreground mt-1">Average score change across completed mock sessions.</p>
            </div>
            <div className="h-80 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={trendData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#2D3748" vertical={false} />
                  <XAxis dataKey="name" stroke="#718096" fontSize={11} tickLine={false} />
                  <YAxis domain={[0, 100]} stroke="#718096" fontSize={11} tickLine={false} />
                  <Tooltip 
                    contentStyle={{ backgroundColor: '#1E293B', borderColor: '#334155', borderRadius: '12px' }}
                    labelStyle={{ color: '#F8FAFC', fontWeight: 'bold' }}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="score" 
                    stroke="#06B6D4" 
                    strokeWidth={3} 
                    dot={{ r: 5, fill: '#06B6D4', strokeWidth: 0 }} 
                    activeDot={{ r: 7, strokeWidth: 0 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </motion.div>

          {/* Right Side Column */}
          <div className="space-y-8">
            
            {/* Career Readiness Panel */}
            <motion.div 
              variants={itemVariants}
              whileHover={{ y: -4, borderColor: "rgba(6, 182, 212, 0.4)" }}
              className="p-6 bg-card border border-border rounded-3xl shadow-sm relative overflow-hidden"
            >
              <div className="absolute top-0 left-0 w-2.5 h-full bg-gradient-to-b from-primary to-cyan-600" />
              <h3 className="text-xs font-bold uppercase text-primary tracking-wider mb-2 ml-2">Career Readiness</h3>
              <div className="text-3xl font-black mb-2 ml-2">{analytics.careerReadinessLevel || 'Developing'}</div>
              <p className="text-sm text-muted-foreground ml-2 leading-relaxed">
                Calculated dynamically from technical competence, communication styles, and algorithm test accuracy.
              </p>
            </motion.div>

            {/* Resume / ATS Overview */}
            <motion.div 
              variants={itemVariants}
              whileHover={{ y: -4, borderColor: "rgba(6, 182, 212, 0.4)" }}
              className="p-6 bg-card border border-border rounded-3xl shadow-sm flex items-center justify-between"
            >
              <div className="flex items-center gap-4">
                <div className="p-3.5 bg-primary/10 text-primary rounded-2xl border border-primary/20">
                  <FileText className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="font-bold text-foreground">Resume Insights</h3>
                  <p className="text-xs text-muted-foreground mt-0.5">
                    {currentResume ? `Parsed: ${currentResume.originalFileName}` : 'No resume uploaded yet'}
                  </p>
                </div>
              </div>
              <button 
                onClick={() => navigate(currentResume ? `/resume/insights` : '/resume/upload')}
                className="p-2 hover:bg-accent text-primary rounded-xl transition-colors border border-border/50"
              >
                <ChevronRight className="w-5 h-5" />
              </button>
            </motion.div>

            {/* Recent History Panel */}
            <motion.div 
              variants={itemVariants}
              className="p-6 bg-card border border-border rounded-3xl shadow-sm"
            >
              <div className="mb-4">
                <h3 className="text-lg font-bold flex items-center gap-2"><Clock className="w-5 h-5 text-muted-foreground" /> Recent Sessions</h3>
              </div>
              <div className="space-y-3 max-h-[260px] overflow-y-auto pr-1">
                {recentHistory.map((h: any, i: number) => (
                  <div
                    key={i}
                    onClick={() => navigate(`/interview/report/${h._id}`)}
                    className="flex justify-between items-center p-3.5 hover:bg-accent/40 bg-accent/10 border border-border/30 rounded-xl transition-all cursor-pointer group"
                  >
                    <div>
                      <div className="font-bold text-sm text-foreground group-hover:text-primary transition-colors">{h.domain}</div>
                      <div className="text-xs text-muted-foreground mt-0.5">{h.company || 'Technical Practice'} • {new Date(h.startedAt).toLocaleDateString()}</div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Badge variant="primary">{h.score}</Badge>
                      <ChevronRight className="w-4 h-4 text-muted-foreground group-hover:text-primary transition-all" />
                    </div>
                  </div>
                ))}
                {recentHistory.length === 0 && (
                  <EmptyState
                    icon={Clock}
                    title="No mock sessions yet"
                    description="Your completed interviews will show up here."
                    className="py-8"
                  />
                )}
              </div>
            </motion.div>

          </div>
        </div>

      </motion.div>
    </div>
  );
};
