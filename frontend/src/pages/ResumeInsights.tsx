import React, { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { RootState } from '../store';
import { resumeApi } from '../services/resumeApi';
import { setReport, setAnalyzing, setError } from '../store/resumeSlice';
import { Loader2, CheckCircle, XCircle, AlertTriangle, Briefcase, Code, FileText, ArrowRight } from 'lucide-react';
import { 
  RadialBarChart, RadialBar, ResponsiveContainer, PolarAngleAxis 
} from 'recharts';

const ScoreRing = ({ score, label, color }: { score: number, label: string, color: string }) => {
  const data = [{ name: label, value: score, fill: color }];
  return (
    <div className="flex flex-col items-center justify-center p-6 bg-card border border-border rounded-2xl shadow-sm">
      <div className="h-24 w-24 relative">
        <ResponsiveContainer width="100%" height="100%">
          <RadialBarChart cx="50%" cy="50%" innerRadius="80%" outerRadius="100%" barSize={8} data={data} startAngle={90} endAngle={-270}>
            <PolarAngleAxis type="number" domain={[0, 100]} angleAxisId={0} tick={false} />
            <RadialBar background dataKey="value" cornerRadius={10} />
          </RadialBarChart>
        </ResponsiveContainer>
        <div className="absolute inset-0 flex items-center justify-center flex-col">
          <span className="text-2xl font-bold">{score}</span>
        </div>
      </div>
      <span className="mt-4 text-xs font-medium text-muted-foreground text-center">{label}</span>
    </div>
  );
};

export const ResumeInsights: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();
  const { currentResume, currentReport, isAnalyzing, error } = useSelector((state: RootState) => state.resume);

  useEffect(() => {
    let intervalId: ReturnType<typeof setTimeout>;

    const checkStatus = async () => {
      if (!currentResume?._id) return;
      try {
        const { data } = await resumeApi.getResumeStatus(currentResume._id);
        if (data.status === 'completed') {
          const reportRes = await resumeApi.getResumeReport(currentResume._id);
          dispatch(setReport(reportRes.data.report));
          dispatch(setAnalyzing(false));
          clearInterval(intervalId);
        } else if (data.status === 'failed') {
          dispatch(setError('Analysis failed on the server.'));
          clearInterval(intervalId);
        }
      } catch (err: any) {
        dispatch(setError(err.message));
        clearInterval(intervalId);
      }
    };

    if (isAnalyzing || (currentResume && !currentReport)) {
      dispatch(setAnalyzing(true));
      checkStatus(); // initial check
      intervalId = setInterval(checkStatus, 3000); // poll every 3s
    }

    return () => clearInterval(intervalId);
  }, [currentResume, isAnalyzing, dispatch, currentReport]);

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-background text-destructive p-4 text-center">
        <AlertTriangle className="w-16 h-16 mb-4" />
        <h2 className="text-2xl font-bold">Error</h2>
        <p className="mt-2 text-muted-foreground">{error}</p>
      </div>
    );
  }

  if (isAnalyzing || !currentReport) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-background text-foreground space-y-6">
        <div className="relative flex items-center justify-center">
           <div className="absolute animate-ping w-16 h-16 rounded-full bg-primary/20"></div>
           <Loader2 className="w-12 h-12 text-primary animate-spin relative z-10" />
        </div>
        <div className="text-center space-y-2">
          <h2 className="text-2xl font-bold">AI is analyzing your resume</h2>
          <p className="text-muted-foreground">Extracting skills, computing ATS score, and generating insights...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background text-foreground p-8">
      <div className="max-w-6xl mx-auto space-y-8">
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div>
            <h1 className="text-4xl font-extrabold tracking-tight">AI Resume Analysis</h1>
            <p className="text-muted-foreground mt-2 text-lg">Review your ATS score, extracted projects, and feedback before diving into the interview.</p>
          </div>
          <button 
            onClick={() => navigate('/interview/setup?type=resume')}
            className="px-8 py-4 bg-primary text-primary-foreground font-bold rounded-xl hover:bg-primary/90 transition-all shadow-xl shadow-primary/20 flex items-center shrink-0 group"
          >
            Start Resume Interview <ArrowRight className="ml-2 w-5 h-5 group-hover:translate-x-1 transition-transform" />
          </button>
        </div>

        {/* Top Summary & ATS Score */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-1 p-6 bg-card border border-border rounded-2xl shadow-sm flex flex-col items-center text-center justify-center">
            <div className="h-40 w-40 relative mb-4">
              <ResponsiveContainer width="100%" height="100%">
                <RadialBarChart cx="50%" cy="50%" innerRadius="80%" outerRadius="100%" barSize={12} data={[{ name: 'ATS', value: currentReport.atsScore, fill: '#10b981' }]} startAngle={90} endAngle={-270}>
                  <PolarAngleAxis type="number" domain={[0, 100]} angleAxisId={0} tick={false} />
                  <RadialBar background dataKey="value" cornerRadius={10} />
                </RadialBarChart>
              </ResponsiveContainer>
              <div className="absolute inset-0 flex items-center justify-center flex-col">
                <span className="text-5xl font-black">{currentReport.atsScore}</span>
                <span className="text-xs text-muted-foreground mt-1">/ 100</span>
              </div>
            </div>
            <h2 className="text-xl font-bold">Overall ATS Score</h2>
            <p className="text-sm text-muted-foreground mt-2">Based on industry standards for Software Engineers.</p>
          </div>
          
          <div className="lg:col-span-2 space-y-6">
            <div className="p-6 bg-card border border-border rounded-2xl shadow-sm h-full">
              <h3 className="text-xl font-semibold flex items-center mb-4"><FileText className="w-5 h-5 text-primary mr-2"/> Resume Summary</h3>
              <p className="text-muted-foreground leading-relaxed">
                {currentReport.resumeSummary || "This candidate has a strong technical background. Experience includes various projects demonstrating problem-solving and full-stack capabilities."}
              </p>
            </div>
          </div>
        </div>

        {/* Score Breakdown */}
        <div>
          <h3 className="text-xl font-bold mb-4">ATS Score Breakdown</h3>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
            <ScoreRing score={currentReport.technicalSkillScore || currentReport.resumeScore} label="Skills Match" color="#3b82f6" />
            <ScoreRing score={currentReport.projectQualityScore || currentReport.resumeScore} label="Project Quality" color="#8b5cf6" />
            <ScoreRing score={currentReport.experienceScore || currentReport.resumeScore} label="Experience" color="#f59e0b" />
            <ScoreRing score={currentReport.formattingScore || currentReport.resumeScore} label="Structure" color="#ec4899" />
            <ScoreRing score={currentReport.keywordMatchScore || currentReport.resumeScore} label="Keywords" color="#06b6d4" />
          </div>
        </div>

        {/* Extracted Data */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="p-6 bg-card border border-border rounded-2xl shadow-sm space-y-6">
            <h3 className="text-xl font-semibold flex items-center"><Code className="w-5 h-5 text-primary mr-2"/> Extracted Skills</h3>
            <div className="flex flex-wrap gap-2">
              {currentReport.extractedSkills.map((s: string, i: number) => (
                <span key={i} className="px-3 py-1 bg-primary/10 text-primary text-sm font-medium rounded-lg border border-primary/20">{s}</span>
              ))}
            </div>
            {currentReport.extractedSkills.length === 0 && <p className="text-sm text-muted-foreground">No explicit skills detected.</p>}
          </div>

          <div className="p-6 bg-card border border-border rounded-2xl shadow-sm space-y-6">
            <h3 className="text-xl font-semibold flex items-center"><Briefcase className="w-5 h-5 text-primary mr-2"/> Project Analysis</h3>
            <div className="space-y-4">
              {currentReport.extractedProjects && currentReport.extractedProjects.length > 0 ? (
                currentReport.extractedProjects.map((p: any, i: number) => (
                  <div key={i} className="p-4 bg-accent/30 rounded-xl border border-border">
                    <div className="flex justify-between items-start mb-2">
                      <h4 className="font-bold">{typeof p === 'string' ? p : p.name || 'Unnamed Project'}</h4>
                      {p.readinessScore && (
                        <span className="text-xs px-2 py-1 bg-blue-500/10 text-blue-500 rounded-full font-medium">
                          Readiness: {p.readinessScore}/100
                        </span>
                      )}
                    </div>
                    {p.complexity && <p className="text-xs text-muted-foreground mb-2">Complexity: <span className="font-medium text-foreground">{p.complexity}</span></p>}
                    {p.technologies && Array.isArray(p.technologies) && (
                      <div className="flex flex-wrap gap-1 mt-2">
                        {p.technologies.map((t: string, j: number) => (
                          <span key={j} className="text-[10px] px-2 py-0.5 bg-background rounded border border-border text-muted-foreground">{t}</span>
                        ))}
                      </div>
                    )}
                  </div>
                ))
              ) : (
                <p className="text-sm text-muted-foreground">No projects detected.</p>
              )}
            </div>
          </div>
        </div>

        {/* Feedback Section */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="space-y-6">
            <div className="p-6 bg-card border border-border rounded-2xl shadow-sm">
              <h3 className="text-lg font-semibold flex items-center mb-4"><CheckCircle className="w-5 h-5 text-emerald-500 mr-2"/> Strengths</h3>
              <ul className="space-y-3">
                {currentReport.strengths.map((s: string, i: number) => (
                  <li key={i} className="flex items-start text-sm"><span className="text-emerald-500 mr-2">•</span> {s}</li>
                ))}
              </ul>
            </div>
          </div>

          <div className="space-y-6">
            <div className="p-6 bg-card border border-border rounded-2xl shadow-sm">
              <h3 className="text-lg font-semibold flex items-center mb-4"><AlertTriangle className="w-5 h-5 text-amber-500 mr-2"/> Improvement Suggestions</h3>
              <ul className="space-y-4">
                {currentReport.recommendations.map((r: string, i: number) => (
                  <li key={i} className="flex items-start p-3 rounded-lg bg-amber-500/5 text-sm border border-amber-500/20 text-amber-600 dark:text-amber-400">
                    <span className="font-bold mr-3">{i+1}.</span> {r}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};
