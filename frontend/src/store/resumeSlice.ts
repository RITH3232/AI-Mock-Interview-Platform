import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface ResumeState {
  currentResume: any | null;
  currentReport: any | null;
  isLoading: boolean;
  isAnalyzing: boolean;
  error: string | null;
}

const initialState: ResumeState = {
  currentResume: null,
  currentReport: null,
  isLoading: false,
  isAnalyzing: false,
  error: null,
};

const resumeSlice = createSlice({
  name: 'resume',
  initialState,
  reducers: {
    setResume: (state, action: PayloadAction<any>) => {
      state.currentResume = action.payload;
      state.error = null;
    },
    setReport: (state, action: PayloadAction<any>) => {
      state.currentReport = action.payload;
    },
    setAnalyzing: (state, action: PayloadAction<boolean>) => {
      state.isAnalyzing = action.payload;
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },
    setError: (state, action: PayloadAction<string>) => {
      state.error = action.payload;
      state.isLoading = false;
      state.isAnalyzing = false;
    },
  },
});

export const { setResume, setReport, setAnalyzing, setLoading, setError } = resumeSlice.actions;
export default resumeSlice.reducer;
