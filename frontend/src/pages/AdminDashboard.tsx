import React, { useEffect, useState } from 'react';
import { analyticsApi } from '../services/analyticsApi';
import { Loader2, Users, Activity, BrainCircuit, FileText } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export const AdminDashboard: React.FC = () => {
  const [health, setHealth] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchHealth = async () => {
      try {
        const { data } = await analyticsApi.getPlatformHealth();
        setHealth(data);
      } catch (err: any) {
        setError(err.response?.data?.message || err.message);
      } finally {
        setLoading(false);
      }
    };
    fetchHealth();
  }, []);

  if (loading) return <div className="min-h-screen flex items-center justify-center"><Loader2 className="w-12 h-12 animate-spin text-primary" /></div>;
  
  if (error) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center p-8 text-center">
         <div className="p-4 bg-destructive/10 text-destructive rounded-xl border border-destructive/20 font-medium">
           {error}
         </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background text-foreground p-8">
      <div className="max-w-7xl mx-auto space-y-8">
        
        <div className="mb-8 border-b border-border pb-6">
          <h1 className="text-3xl font-bold tracking-tight">Platform Administration</h1>
          <p className="text-muted-foreground mt-1">Real-time health and usage metrics.</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
           <div className="p-6 bg-card border border-border rounded-2xl shadow-sm">
             <div className="flex justify-between items-start mb-4">
               <span className="text-muted-foreground font-medium text-sm">Total Users</span>
               <div className="p-2 bg-blue-500/10 rounded-lg"><Users className="w-4 h-4 text-blue-500" /></div>
             </div>
             <div className="text-4xl font-bold">{health.totalUsers}</div>
             <div className="text-xs text-muted-foreground mt-2">{health.activeUsers} Active</div>
           </div>

           <div className="p-6 bg-card border border-border rounded-2xl shadow-sm">
             <div className="flex justify-between items-start mb-4">
               <span className="text-muted-foreground font-medium text-sm">Interviews</span>
               <div className="p-2 bg-emerald-500/10 rounded-lg"><Activity className="w-4 h-4 text-emerald-500" /></div>
             </div>
             <div className="text-4xl font-bold">{health.totalInterviews}</div>
             <div className="text-xs text-muted-foreground mt-2">{health.completedInterviews} Completed</div>
           </div>

           <div className="p-6 bg-card border border-border rounded-2xl shadow-sm">
             <div className="flex justify-between items-start mb-4">
               <span className="text-muted-foreground font-medium text-sm">Generated Reports</span>
               <div className="p-2 bg-purple-500/10 rounded-lg"><FileText className="w-4 h-4 text-purple-500" /></div>
             </div>
             <div className="text-4xl font-bold">{health.totalReports}</div>
           </div>

           <div className="p-6 bg-card border border-border rounded-2xl shadow-sm">
             <div className="flex justify-between items-start mb-4">
               <span className="text-muted-foreground font-medium text-sm">Est. OpenAI Cost</span>
               <div className="p-2 bg-orange-500/10 rounded-lg"><BrainCircuit className="w-4 h-4 text-orange-500" /></div>
             </div>
             <div className="text-4xl font-bold">${health.openAiUsageEst.toFixed(2)}</div>
             <div className="text-xs text-muted-foreground mt-2">{health.totalQuestions} Questions Gen</div>
           </div>
        </div>

        <div className="p-6 bg-card border border-border rounded-3xl shadow-sm">
          <h3 className="text-lg font-bold mb-6">Daily Activity (Last 7 Days)</h3>
          <div className="h-80 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={health.dailyActivity}>
                <CartesianGrid strokeDasharray="3 3" stroke="#333" />
                <XAxis dataKey="name" stroke="#888" />
                <YAxis stroke="#888" />
                <Tooltip cursor={{fill: 'rgba(255,255,255,0.05)'}} contentStyle={{ backgroundColor: '#111', borderColor: '#333' }} />
                <Bar dataKey="interviews" fill="#8b5cf6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

      </div>
    </div>
  );
};
