import { api } from './api';

export const analyticsApi = {
  getDashboard: async () => {
    const response = await api.get('/analytics/dashboard');
    return response.data;
  },
  getLeaderboard: async () => {
    const response = await api.get('/analytics/leaderboard');
    return response.data;
  },
  getAchievements: async () => {
    const response = await api.get('/analytics/achievements');
    return response.data;
  },
  getPlatformHealth: async () => {
    const response = await api.get('/admin/analytics');
    return response.data;
  }
};
