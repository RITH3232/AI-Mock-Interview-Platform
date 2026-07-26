import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../store';
import { interviewApi } from '../services/interviewApi';
import { interviewRoomApi } from '../services/interviewRoomApi';
import { nextQuestion, setTimer, setRecording, setTranscript, setEvaluating, resetRoom } from '../store/interviewRoomSlice';
import {
  Mic, Square, Send, Loader2, Clock, CheckCircle2, Circle, ChevronLeft, ChevronRight,
  BookOpen, LogOut, Lock,
} from 'lucide-react';
import { CodeEditor } from '../components/CodeEditor';
import { ProgressBar } from '../components/ui/ProgressBar';
import { Badge } from '../components/ui/Badge';
import { useAutosave, readAutosave, clearAutosave } from '../hooks/useAutosave';
import { useToast } from '../hooks/useToast';

interface SubmittedAnswer {
  format: string;
  content: string;
}

export const InterviewRoom: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { toast } = useToast();

  const { currentQuestionIndex, timer, isRecording, transcript, isEvaluating } = useSelector((state: RootState) => state.room);

  const [sessionData, setSessionData] = useState<any>(null);
  const [questions, setQuestions] = useState<any[]>([]);
  const [textAnswer, setTextAnswer] = useState('');
  const [mediaRecorder, setMediaRecorder] = useState<MediaRecorder | null>(null);
  const [audioChunks, setAudioChunks] = useState<Blob[]>([]);
  const [recognition, setRecognition] = useState<any>(null);

  const [selectedOption, setSelectedOption] = useState('');

  const [submittedAnswers, setSubmittedAnswers] = useState<Record<number, SubmittedAnswer>>({});
  const [viewIndex, setViewIndex] = useState(0);
  const [syncedQuestionIndex, setSyncedQuestionIndex] = useState(0);

  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    dispatch(resetRoom());

    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (SpeechRecognition) {
      const recog = new SpeechRecognition();
      recog.continuous = true;
      recog.interimResults = true;

      recog.onresult = (event: any) => {
        let finalTranscript = '';
        for (let i = event.resultIndex; i < event.results.length; i++) {
          if (event.results[i].isFinal) {
            finalTranscript += event.results[i][0].transcript + ' ';
          }
        }
        if (finalTranscript) {
          dispatch(setTranscript(transcript + finalTranscript));
        }
      };
      setRecognition(recog);
    }

    const initSession = async () => {
      try {
        const { data } = await interviewApi.getSession(id!);
        const session = data.session;

        if (session.status === 'generating') {
          setTimeout(initSession, 3000);
          return;
        }

        if (session.status === 'cancelled') {
          setError(session.errorMessage || 'Failed to generate interview questions. Please try again.');
          return;
        }

        setSessionData(session);
        setQuestions(session.generatedQuestions);

        if (session.status === 'ready') {
          await interviewRoomApi.startSession(id!);
        }
      } catch (err: any) {
        console.error(err);
        setError(err.response?.data?.message || 'Failed to connect to the interview room.');
      }
    };
    initSession();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, dispatch]);

  useEffect(() => {
    const interval = setInterval(() => dispatch(setTimer(timer + 1)), 1000);
    return () => clearInterval(interval);
  }, [timer, dispatch]);

  const currentQ = questions[currentQuestionIndex];
  const viewQ = questions[viewIndex];
  const isViewingPast = viewIndex !== currentQuestionIndex;

  const autosaveKey = id ? `interviewiq:autosave:${id}:${currentQuestionIndex}` : null;
  useAutosave(autosaveKey, textAnswer);

  // Keep the view snapped to the active question whenever it advances (e.g. after Submit & Next),
  // without re-running on every viewIndex change caused by manual Previous/history navigation.
  if (currentQuestionIndex !== syncedQuestionIndex) {
    setSyncedQuestionIndex(currentQuestionIndex);
    setViewIndex(currentQuestionIndex);
  }

  useEffect(() => {
    if (currentQ) {
      setSelectedOption('');
      setTextAnswer(readAutosave(id ? `interviewiq:autosave:${id}:${currentQuestionIndex}` : null));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentQuestionIndex, currentQ]);

  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const recorder = new MediaRecorder(stream);
      const chunks: Blob[] = [];

      recorder.ondataavailable = (e) => chunks.push(e.data);
      recorder.onstop = () => {
        setAudioChunks(chunks);
        stream.getTracks().forEach((t) => t.stop());
      };

      recorder.start();
      setMediaRecorder(recorder);
      dispatch(setRecording(true));

      if (recognition) recognition.start();
    } catch (err) {
      console.error('Microphone access denied', err);
      toast('Microphone access was denied.', 'error');
    }
  };

  const stopRecording = () => {
    if (mediaRecorder && mediaRecorder.state !== 'inactive') {
      mediaRecorder.stop();
      dispatch(setRecording(false));
      if (recognition) recognition.stop();
    }
  };

  const recordSubmitted = (index: number, format: string, content: string) => {
    setSubmittedAnswers((prev) => ({ ...prev, [index]: { format, content } }));
  };

  const advanceOrFinish = async () => {
    if (currentQuestionIndex < questions.length - 1) {
      dispatch(nextQuestion());
      dispatch(setEvaluating(false));
    } else {
      await interviewRoomApi.generateReport(id!);
      navigate(`/interview/report/${id}`);
    }
  };

  const submitAnswer = async () => {
    dispatch(setEvaluating(true));
    stopRecording();

    try {
      const formData = new FormData();
      formData.append('sessionId', id!);
      formData.append('questionId', questions[currentQuestionIndex]._id);
      formData.append('duration', timer.toString());

      let recordedContent = '';
      if (currentQ?.questionFormat === 'mcq') {
        formData.append('answerType', 'mcq');
        formData.append('answerText', selectedOption);
        recordedContent = selectedOption;
      } else if (textAnswer) {
        formData.append('answerType', 'text');
        formData.append('answerText', textAnswer);
        recordedContent = textAnswer;
      } else {
        formData.append('answerType', 'voice');
        formData.append('transcript', transcript);
        recordedContent = transcript;
        if (audioChunks.length > 0) {
          const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
          formData.append('audio', audioBlob, 'answer.webm');
        }
      }

      await interviewRoomApi.submitAnswer(formData);
      recordSubmitted(currentQuestionIndex, currentQ?.questionFormat || 'text', recordedContent);
      clearAutosave(autosaveKey);

      setTextAnswer('');
      dispatch(setTranscript(''));
      setAudioChunks([]);

      await advanceOrFinish();
    } catch (err) {
      console.error(err);
      toast('Failed to submit your answer. Please try again.', 'error');
      dispatch(setEvaluating(false));
    }
  };

  const submitCodeAnswer = async (code: string, execOutput: string, passed: boolean) => {
    const finalAnswerText = `Code:\n${code}\n\nExecution Output:\n${execOutput}\n\nPassed Test Cases: ${passed}`;

    dispatch(setEvaluating(true));
    stopRecording();

    try {
      const formData = new FormData();
      formData.append('sessionId', id!);
      formData.append('questionId', questions[currentQuestionIndex]._id);
      formData.append('duration', timer.toString());
      formData.append('answerType', 'code');
      formData.append('answerText', finalAnswerText);

      await interviewRoomApi.submitAnswer(formData);
      recordSubmitted(currentQuestionIndex, 'code', finalAnswerText);
      clearAutosave(autosaveKey);

      await advanceOrFinish();
    } catch (err) {
      console.error(err);
      toast('Failed to submit your answer. Please try again.', 'error');
      dispatch(setEvaluating(false));
    }
  };

  const goPrevious = () => {
    if (viewIndex > 0) setViewIndex(viewIndex - 1);
  };

  const returnToCurrent = () => setViewIndex(currentQuestionIndex);

  if (error) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-background text-foreground p-6">
        <div className="bg-destructive/10 border border-destructive/20 text-destructive p-6 rounded-2xl max-w-md text-center">
          <h2 className="text-xl font-bold mb-2">Something went wrong</h2>
          <p className="mb-4">{error}</p>
          <button
            onClick={() => navigate('/dashboard')}
            className="px-4 py-2 bg-primary text-primary-foreground rounded-xl font-medium hover:bg-primary/90 transition-colors"
          >
            Return to Dashboard
          </button>
        </div>
      </div>
    );
  }

  if (!sessionData || questions.length === 0 || !currentQ) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-background text-foreground">
        <Loader2 className="w-12 h-12 animate-spin text-primary mb-4" />
        <p className="text-muted-foreground font-medium animate-pulse">
          {!sessionData || questions.length === 0 ? 'Building your personalized interview…' : 'Loading question…'}
        </p>
      </div>
    );
  }

  const answeredCount = Object.keys(submittedAnswers).length;
  const progressPct = (answeredCount / questions.length) * 100;
  const minutes = Math.floor(timer / 60).toString().padStart(2, '0');
  const seconds = (timer % 60).toString().padStart(2, '0');

  return (
    <div className="flex flex-col lg:flex-row h-screen bg-background text-foreground overflow-hidden">
      {/* LEFT: Progress rail */}
      <div className="w-full lg:w-80 shrink-0 border-b lg:border-b-0 lg:border-r border-border bg-card flex flex-col max-h-[40vh] lg:max-h-none">
        <div className="p-5 border-b border-border">
          <div className="flex items-center justify-between mb-1">
            <span className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
              {sessionData.role || sessionData.domain}
            </span>
            <Badge variant="primary">{sessionData.interviewType}</Badge>
          </div>
          <ProgressBar value={progressPct} className="mt-3" />
          <p className="text-xs text-muted-foreground mt-2">{answeredCount} of {questions.length} answered</p>
        </div>
        <div className="flex-1 overflow-y-auto scrollbar-thin p-3 space-y-1.5">
          {questions.map((q, i) => {
            const answered = submittedAnswers[i] !== undefined;
            const isCurrent = i === currentQuestionIndex;
            const isUpcoming = i > currentQuestionIndex;
            const isSelected = i === viewIndex;
            return (
              <button
                key={q._id || i}
                type="button"
                disabled={isUpcoming}
                onClick={() => !isUpcoming && setViewIndex(i)}
                className={`w-full flex items-center gap-3 p-3 rounded-xl text-left transition-all ${
                  isSelected ? 'bg-primary/10 border border-primary/30' : 'hover:bg-accent/50 border border-transparent'
                } ${isUpcoming ? 'opacity-50 cursor-not-allowed' : ''}`}
              >
                {answered ? (
                  <CheckCircle2 className="w-5 h-5 text-emerald-500 shrink-0" />
                ) : isCurrent ? (
                  <Circle className="w-5 h-5 text-primary shrink-0 fill-primary/20" />
                ) : (
                  <Lock className="w-4 h-4 text-muted-foreground shrink-0" />
                )}
                <div className="min-w-0">
                  <p className="text-xs font-bold text-muted-foreground">Question {i + 1}</p>
                  <p className="text-sm truncate font-medium">{q.category || q.domain || 'Question'}</p>
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* RIGHT: Workspace */}
      <div className="flex-1 flex flex-col p-4 sm:p-8 bg-background relative overflow-y-auto">
        <div className="flex justify-between items-center mb-6 gap-3">
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2 bg-card px-4 py-2 rounded-full border border-border shadow-sm">
              <Clock className="w-4 h-4 text-primary" />
              <span className="font-mono text-base font-medium">{minutes}:{seconds}</span>
            </div>
            <span className="hidden sm:inline text-sm text-muted-foreground font-medium">
              Question {viewIndex + 1} of {questions.length}
            </span>
          </div>
          <button
            onClick={() => navigate('/dashboard')}
            className="flex items-center gap-1.5 text-sm font-medium text-muted-foreground hover:text-destructive transition-colors"
          >
            <LogOut className="w-4 h-4" />
            <span className="hidden sm:inline">End Interview</span>
          </button>
        </div>

        <div className="mb-6">
          <div className="flex items-center gap-2 mb-3">
            <Badge variant="primary">{viewQ.domain}</Badge>
            <Badge variant="outline">{viewQ.difficulty}</Badge>
            {viewQ.category && <Badge variant="default">{viewQ.category}</Badge>}
          </div>
          <h1 className="text-2xl sm:text-3xl font-bold leading-tight">{viewQ.question}</h1>
          {viewQ.expectedTopics?.length > 0 && (
            <div className="flex items-center flex-wrap gap-2 mt-4">
              <BookOpen className="w-4 h-4 text-muted-foreground" />
              {viewQ.expectedTopics.map((t: string, i: number) => (
                <span key={i} className="px-3 py-1 bg-accent/50 text-xs font-medium rounded-full border border-border/50">{t}</span>
              ))}
            </div>
          )}
        </div>

        {isViewingPast ? (
          <div className="flex-1 flex flex-col bg-card border border-border rounded-3xl p-6 shadow-sm">
            <Badge variant="success" className="w-fit mb-4">Already submitted</Badge>
            <div className="flex-1 rounded-2xl bg-accent/30 border border-border/50 p-5 overflow-auto">
              {submittedAnswers[viewIndex]?.format === 'code' ? (
                <pre className="text-sm font-mono whitespace-pre-wrap">{submittedAnswers[viewIndex]?.content}</pre>
              ) : (
                <p className="text-sm leading-relaxed whitespace-pre-wrap">
                  {submittedAnswers[viewIndex]?.content || 'No response was recorded for this question.'}
                </p>
              )}
            </div>
            <button
              onClick={returnToCurrent}
              className="mt-6 self-end flex items-center gap-2 px-6 py-3 bg-primary text-primary-foreground rounded-xl font-medium hover:bg-primary/90 transition-all"
            >
              <span>Back to current question</span>
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        ) : (
          <div className="flex-1 flex flex-col bg-card border border-border rounded-3xl p-6 shadow-sm overflow-hidden relative">
            <div className="flex items-center justify-between mb-4">
              <button
                onClick={goPrevious}
                disabled={currentQuestionIndex === 0 || isEvaluating}
                className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-sm font-medium text-muted-foreground hover:bg-accent transition-all disabled:opacity-40"
              >
                <ChevronLeft className="w-4 h-4" />
                <span>Previous</span>
              </button>
              {currentQ?.questionFormat === 'code' && (
                <span className="text-xs text-muted-foreground">Use the editor's Submit Code button to continue</span>
              )}
            </div>
            {currentQ?.questionFormat === 'code' ? (
              <div className="flex-1 flex flex-col min-h-[400px]">
                <CodeEditor
                  starterCode={currentQ.starterCode}
                  expectedOutput={currentQ.testCases?.[0]?.expectedOutput}
                  onSubmitAnswer={submitCodeAnswer}
                />
              </div>
            ) : currentQ?.questionFormat === 'mcq' ? (
              <div className="flex-1 flex flex-col space-y-3">
                {currentQ.options?.map((opt: string, i: number) => (
                  <label
                    key={i}
                    className={`flex items-center p-4 rounded-xl border cursor-pointer transition-colors ${
                      selectedOption === opt ? 'bg-primary/10 border-primary' : 'bg-transparent border-border hover:border-primary/50'
                    }`}
                  >
                    <input
                      type="radio"
                      name="mcq-option"
                      value={opt}
                      checked={selectedOption === opt}
                      onChange={() => setSelectedOption(opt)}
                      className="w-4 h-4 text-primary bg-background border-border focus:ring-primary"
                    />
                    <span className="ml-3 text-lg">{opt}</span>
                  </label>
                ))}
              </div>
            ) : (
              <>
                <textarea
                  className="flex-1 min-h-[240px] bg-transparent resize-none outline-none text-lg leading-relaxed placeholder:text-muted-foreground/50"
                  placeholder={isRecording ? 'Listening and transcribing…' : 'Type your answer here, or click the mic to speak…'}
                  value={isRecording ? transcript : textAnswer}
                  onChange={(e) => !isRecording && setTextAnswer(e.target.value)}
                  disabled={isRecording || isEvaluating}
                />

                <div className="mt-6 flex items-center justify-between pt-6 border-t border-border">
                  <div className="flex items-center gap-4">
                    {!isRecording ? (
                      <button
                        onClick={startRecording}
                        disabled={isEvaluating}
                        className="flex items-center gap-2 px-6 py-3 bg-primary text-primary-foreground rounded-xl font-medium shadow-md shadow-primary/20 hover:bg-primary/90 transition-all disabled:opacity-50"
                      >
                        <Mic className="w-5 h-5" />
                        <span>Record Answer</span>
                      </button>
                    ) : (
                      <button
                        onClick={stopRecording}
                        className="flex items-center gap-2 px-6 py-3 bg-destructive text-destructive-foreground rounded-xl font-medium shadow-md shadow-destructive/20 hover:bg-destructive/90 transition-all animate-pulse"
                      >
                        <Square className="w-5 h-5" />
                        <span>Stop Recording</span>
                      </button>
                    )}
                    {!isRecording && textAnswer && (
                      <span className="text-xs text-muted-foreground">Draft auto-saved</span>
                    )}
                  </div>
                </div>
              </>
            )}

            {currentQ?.questionFormat !== 'code' && (
              <div className="flex items-center justify-end mt-6 pt-6 border-t border-border">
                <button
                  onClick={submitAnswer}
                  disabled={
                    isEvaluating ||
                    (currentQ?.questionFormat === 'mcq' ? !selectedOption : !textAnswer && !transcript && audioChunks.length === 0)
                  }
                  className="flex items-center gap-2 px-8 py-3 bg-gradient-to-r from-primary to-cyan-500 text-primary-foreground rounded-xl font-bold shadow-lg shadow-primary/20 hover:shadow-xl transition-all disabled:opacity-50 disabled:shadow-none"
                >
                  {isEvaluating ? <Loader2 className="w-5 h-5 animate-spin" /> : <Send className="w-5 h-5" />}
                  <span>{currentQuestionIndex === questions.length - 1 ? 'Finish Interview' : 'Submit & Next'}</span>
                </button>
              </div>
            )}

            {isEvaluating && (
              <div className="absolute inset-0 bg-background/80 backdrop-blur-sm flex flex-col items-center justify-center z-10 rounded-3xl">
                <Loader2 className="w-12 h-12 text-primary animate-spin mb-4" />
                <p className="text-lg font-bold">AI is evaluating your response…</p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
