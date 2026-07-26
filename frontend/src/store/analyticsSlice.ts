import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface AnalyticsState {
  dashboardData: any | null;
  leaderboardData: any[];
  isLoading: boolean;
  error: string | null;
}

const initialState: AnalyticsState = {
  dashboardData: null,
  leaderboardData: [],
  isLoading: false,
  error: null,
};

const analyticsSlice = createSlice({
  name: 'analytics',
  initialState,
  reducers: {
    setDashboardData: (state, action: PayloadAction<any>) => {
      state.dashboardData = action.payload;
      state.error = null;
    },
    setLeaderboardData: (state, action: PayloadAction<any[]>) => {
      state.leaderboardData = action.payload;
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },
    setError: (state, action: PayloadAction<string>) => {
      state.error = action.payload;
      state.isLoading = false;
    },
  },
});

export const { setDashboardData, setLeaderboardData, setLoading, setError } = analyticsSlice.actions;
export default analyticsSlice.reducer;
