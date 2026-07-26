import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../store';
import { analyticsApi } from '../services/analyticsApi';
import { setLeaderboardData, setLoading } from '../store/analyticsSlice';
import { Loader2, Trophy, Medal, Star } from 'lucide-react';

export const Leaderboard: React.FC = () => {
  const dispatch = useDispatch();
  const { leaderboardData, isLoading } = useSelector((state: RootState) => state.analytics);

  useEffect(() => {
    const fetchLeaderboard = async () => {
      dispatch(setLoading(true));
      try {
        const { data } = await analyticsApi.getLeaderboard();
        dispatch(setLeaderboardData(data));
      } catch (err) {
        console.error(err);
      } finally {
        dispatch(setLoading(false));
      }
    };
    fetchLeaderboard();
  }, [dispatch]);

  if (isLoading) {
    return <div className="min-h-screen flex items-center justify-center"><Loader2 className="w-12 h-12 animate-spin text-primary" /></div>;
  }

  return (
    <div className="min-h-screen bg-background text-foreground p-8">
      <div className="max-w-4xl mx-auto space-y-8">
        
        <div className="text-center space-y-4 mb-12">
          <div className="inline-flex items-center justify-center p-4 bg-yellow-500/10 rounded-full mb-4">
             <Trophy className="w-12 h-12 text-yellow-500" />
          </div>
          <h1 className="text-4xl font-extrabold tracking-tight">Global Leaderboard</h1>
          <p className="text-xl text-muted-foreground">Compete, practice, and climb the ranks.</p>
        </div>

        <div className="bg-card border border-border rounded-3xl overflow-hidden shadow-sm">
          <div className="grid grid-cols-12 gap-4 p-4 border-b border-border bg-accent/50 text-sm font-semibold text-muted-foreground uppercase tracking-wider">
            <div className="col-span-2 text-center">Rank</div>
            <div className="col-span-5">Candidate</div>
            <div className="col-span-2 text-right">Interviews</div>
            <div className="col-span-3 text-right">Total XP</div>
          </div>
          
          <div className="divide-y divide-border">
            {leaderboardData.map((user: any, index: number) => (
              <div key={user._id} className="grid grid-cols-12 gap-4 p-4 items-center hover:bg-accent/30 transition-colors">
                <div className="col-span-2 flex justify-center">
                  {index === 0 ? <Medal className="w-8 h-8 text-yellow-500" /> :
                   index === 1 ? <Medal className="w-8 h-8 text-gray-400" /> :
                   index === 2 ? <Medal className="w-8 h-8 text-amber-700" /> :
                   <span className="font-bold text-lg text-muted-foreground">#{index + 1}</span>}
                </div>
                <div className="col-span-5 flex items-center space-x-3">
                  <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center font-bold text-primary">
                    {user.userId?.firstName?.charAt(0) || 'A'}
                  </div>
                  <div>
                    <div className="font-semibold">{user.userId?.firstName} {user.userId?.lastName}</div>
                    <div className="text-xs text-muted-foreground">Avg Score: {user.averageScore}</div>
                  </div>
                </div>
                <div className="col-span-2 text-right font-medium text-muted-foreground">
                  {user.totalInterviews}
                </div>
                <div className="col-span-3 flex justify-end items-center text-right font-bold text-lg">
                  {user.xp} <Star className="w-4 h-4 ml-1 text-yellow-500" />
                </div>
              </div>
            ))}
          </div>
        </div>

      </div>
    </div>
  );
};
