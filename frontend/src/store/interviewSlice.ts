import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface InterviewState {
  session: any | null;
  isLoading: boolean;
  error: string | null;
}

const initialState: InterviewState = {
  session: null,
  isLoading: false,
  error: null,
};

const interviewSlice = createSlice({
  name: 'interview',
  initialState,
  reducers: {
    setSession: (state, action: PayloadAction<any>) => {
      state.session = action.payload;
      state.error = null;
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

export const { setSession, setLoading, setError } = interviewSlice.actions;
export default interviewSlice.reducer;
