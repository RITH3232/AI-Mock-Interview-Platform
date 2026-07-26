import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { interviewRoomApi } from '../services/interviewRoomApi';
import {
  Loader2, Download, Award, Target, MessageSquare, Zap, CheckCircle2, AlertCircle,
  Code, Lightbulb, RotateCcw, Sparkles,
} from 'lucide-react';
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer } from 'recharts';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { useToast } from '../hooks/useToast';

export const InterviewReport: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [report, setReport] = useState<any>(null);
  const [answers, setAnswers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    const fetchRep = async () => {
      try {
        const { data } = await interviewRoomApi.fetchReport(id!);
        setReport(data.report);
        setAnswers(data.answers || []);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchRep();
  }, [id]);

  const handleExport = async () => {
    setExporting(true);
    try {
      await interviewRoomApi.downloadPDF(id!);
    } catch (err) {
      console.error(err);
      toast('Failed to export PDF. Please try again.', 'error');
    } finally {
      setExporting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <Loader2 className="w-12 h-12 animate-spin text-primary" />
      </div>
    );
  }

  if (!report) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center text-destructive bg-background">
        <AlertCircle className="w-16 h-16 mb-4" />
        <h2 className="text-xl font-bold">Report not found.</h2>
      </div>
    );
  }

  const radarData = [
    { subject: 'Technical', A: report.technicalScore, fullMark: 100 },
    { subject: 'Communication', A: report.communicationScore, fullMark: 100 },
    { subject: 'Confidence', A: report.confidenceScore, fullMark: 100 },
    { subject: 'Problem Solving', A: report.problemSolvingScore, fullMark: 100 },
  ];

  return (
    <div className="min-h-screen bg-background text-foreground p-4 sm:p-8">
      <div className="max-w-6xl mx-auto space-y-8">

        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 text-xs font-semibold text-primary uppercase tracking-wider mb-2">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Interview complete</span>
            </div>
            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">Performance Report</h1>
            <p className="text-muted-foreground mt-2">AI-generated analysis of your mock interview session.</p>
          </div>
          <div className="flex flex-wrap items-center gap-3">
            <button
              onClick={() => navigate('/interview/setup')}
              className="flex items-center gap-2 px-5 py-3 bg-card border border-border rounded-xl font-medium hover:border-primary/50 hover:bg-accent/30 transition-all"
            >
              <RotateCcw className="w-4 h-4" />
              <span>New Interview</span>
            </button>
            <button
              onClick={handleExport}
              disabled={exporting}
              className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-primary to-cyan-500 text-primary-foreground font-medium rounded-xl hover:shadow-lg hover:shadow-primary/25 transition-all disabled:opacity-60"
            >
              {exporting ? <Loader2 className="w-5 h-5 animate-spin" /> : <Download className="w-5 h-5" />}
              <span>Export PDF</span>
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

          {/* Main Score & Radar Chart */}
          <div className="col-span-1 space-y-8">
            <Card className="p-8 text-center relative overflow-hidden">
              <div className="absolute top-0 left-0 w-full h-2 bg-gradient-to-r from-primary to-cyan-500" />
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground mb-4">Overall Score</h3>
              <div className="text-7xl font-black text-transparent bg-clip-text bg-gradient-to-br from-primary to-cyan-500">
                {report.overallScore}
              </div>
              <p className="mt-4 text-sm text-muted-foreground font-medium">Out of 100</p>
              {report.careerReadinessLevel && (
                <Badge variant="primary" className="mt-4">{report.careerReadinessLevel}</Badge>
              )}
            </Card>

            <Card className="p-6 h-80">
              <ResponsiveContainer width="100%" height="100%">
                <RadarChart cx="50%" cy="50%" outerRadius="70%" data={radarData}>
                  <PolarGrid stroke="hsl(var(--border))" />
                  <PolarAngleAxis dataKey="subject" tick={{ fill: 'hsl(var(--muted-foreground))', fontSize: 12 }} />
                  <PolarRadiusAxis angle={30} domain={[0, 100]} tick={false} axisLine={false} />
                  <Radar name="Candidate" dataKey="A" stroke="hsl(var(--primary))" fill="hsl(var(--primary))" fillOpacity={0.4} />
                </RadarChart>
              </ResponsiveContainer>
            </Card>
          </div>

          {/* Breakdown & Analysis */}
          <div className="col-span-2 space-y-8">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {[
                { label: 'Technical', score: report.technicalScore, icon: Code },
                { label: 'Communication', score: report.communicationScore, icon: MessageSquare },
                { label: 'Confidence', score: report.confidenceScore, icon: Zap },
                { label: 'Problem Solving', score: report.problemSolvingScore, icon: Target },
              ].map((m, i) => (
                <Card key={i} className="p-5">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-3">
                      <div className="p-2.5 bg-accent rounded-xl text-primary"><m.icon className="w-4 h-4" /></div>
                      <span className="font-semibold text-sm">{m.label}</span>
                    </div>
                    <span className="text-xl font-bold">{Math.round(m.score || 0)}</span>
                  </div>
                  <ProgressBar value={m.score || 0} />
                </Card>
              ))}
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
              <Card className="p-6">
                <h3 className="text-lg font-bold flex items-center mb-4 text-emerald-600 dark:text-emerald-400">
                  <CheckCircle2 className="w-5 h-5 mr-2" /> Strengths
                </h3>
                <ul className="space-y-3">
                  {report.strengths?.map((s: string, i: number) => (
                    <li key={i} className="flex items-start text-sm"><span className="text-emerald-500 mr-2">•</span> {s}</li>
                  ))}
                  {(!report.strengths || report.strengths.length === 0) && (
                    <p className="text-sm text-muted-foreground">No standout strengths were flagged this round.</p>
                  )}
                </ul>
              </Card>
              <Card className="p-6">
                <h3 className="text-lg font-bold flex items-center mb-4 text-destructive">
                  <AlertCircle className="w-5 h-5 mr-2" /> Weaknesses
                </h3>
                <ul className="space-y-3">
                  {report.weaknesses?.map((s: string, i: number) => (
                    <li key={i} className="flex items-start text-sm"><span className="text-destructive mr-2">•</span> {s}</li>
                  ))}
                  {(!report.weaknesses || report.weaknesses.length === 0) && (
                    <p className="text-sm text-muted-foreground">No significant weaknesses were flagged this round.</p>
                  )}
                </ul>
              </Card>
            </div>

            {report.recommendations?.length > 0 && (
              <Card className="p-6">
                <h3 className="text-lg font-bold flex items-center mb-4">
                  <Lightbulb className="w-5 h-5 text-amber-500 mr-2" /> Suggested Improvements
                </h3>
                <ul className="space-y-3">
                  {report.recommendations.map((s: string, i: number) => (
                    <li key={i} className="flex items-start text-sm p-3 rounded-xl bg-amber-500/5 border border-amber-500/10">
                      <span className="text-amber-500 mr-2">→</span> {s}
                    </li>
                  ))}
                </ul>
              </Card>
            )}

            <Card className="p-6">
              <h3 className="text-lg font-bold flex items-center mb-4">
                <Award className="w-5 h-5 text-primary mr-2" /> Recommended Topics &amp; Roadmap
              </h3>
              <ul className="space-y-4">
                {report.learningRoadmap?.map((s: string, i: number) => (
                  <li key={i} className="flex p-4 rounded-xl bg-accent/50 text-sm border border-border/50">
                    <span className="font-bold text-primary mr-4">{i + 1}.</span> {s}
                  </li>
                ))}
              </ul>
            </Card>

            <Card className="p-6">
              <h3 className="text-lg font-bold flex items-center mb-4">
                <MessageSquare className="w-5 h-5 text-primary mr-2" /> Question-wise Transcript
              </h3>
              <div className="space-y-4 max-h-96 overflow-y-auto scrollbar-thin pr-2">
                {answers && answers.length > 0 ? (
                  answers.map((ans: any, idx: number) => {
                    const questionText = ans.questionId?.question || 'Question';
                    const answerVal = ans.answerType === 'voice' ? ans.transcript : ans.answerText;
                    const isCode = ans.answerType === 'code';

                    return (
                      <React.Fragment key={ans._id || idx}>
                        <div className="bg-accent/30 p-4 rounded-xl border border-border/50">
                          <p className="text-xs text-muted-foreground font-bold uppercase mb-1">Question {idx + 1}</p>
                          <p className="text-sm">{questionText}</p>
                        </div>
                        <div className="bg-primary/10 p-4 rounded-xl border border-primary/20 ml-4 sm:ml-8">
                          <p className="text-xs text-primary font-bold uppercase mb-1">Your Answer</p>
                          {isCode ? (
                            <pre className="text-sm font-mono mt-2 bg-black/80 text-emerald-400 p-3 rounded-lg overflow-x-auto">
                              {answerVal || 'No code provided.'}
                            </pre>
                          ) : (
                            <p className="text-sm">{answerVal || 'No response provided.'}</p>
                          )}
                        </div>
                      </React.Fragment>
                    );
                  })
                ) : (
                  <p className="text-sm text-muted-foreground">No transcript available for this session.</p>
                )}
              </div>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};
