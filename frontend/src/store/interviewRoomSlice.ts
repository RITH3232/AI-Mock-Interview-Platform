import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface RoomState {
  currentQuestionIndex: number;
  timer: number;
  isRecording: boolean;
  transcript: string;
  isEvaluating: boolean;
}

const initialState: RoomState = {
  currentQuestionIndex: 0,
  timer: 0,
  isRecording: false,
  transcript: '',
  isEvaluating: false,
};

const interviewRoomSlice = createSlice({
  name: 'room',
  initialState,
  reducers: {
    nextQuestion: (state) => {
      state.currentQuestionIndex += 1;
      state.timer = 0;
      state.transcript = '';
    },
    setTimer: (state, action: PayloadAction<number>) => {
      state.timer = action.payload;
    },
    setRecording: (state, action: PayloadAction<boolean>) => {
      state.isRecording = action.payload;
    },
    setTranscript: (state, action: PayloadAction<string>) => {
      state.transcript = action.payload;
    },
    setEvaluating: (state, action: PayloadAction<boolean>) => {
      state.isEvaluating = action.payload;
    },
    resetRoom: (state) => {
      state.currentQuestionIndex = 0;
      state.timer = 0;
      state.isRecording = false;
      state.transcript = '';
      state.isEvaluating = false;
    }
  },
});

export const { nextQuestion, setTimer, setRecording, setTranscript, setEvaluating, resetRoom } = interviewRoomSlice.actions;
export default interviewRoomSlice.reducer;
