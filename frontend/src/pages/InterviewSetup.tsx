import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { motion } from 'framer-motion';
import {
  Code2, Users, Heart, Network, Shuffle, Loader2, Sparkles, ChevronRight,
  Briefcase, FileText, ArrowRight, X
} from 'lucide-react';
import { RootState } from '../store';
import { interviewApi } from '../services/interviewApi';
import { setSession, setLoading, setError } from '../store/interviewSlice';
import { useToast } from '../hooks/useToast';
import { Card } from '../components/ui/Card';
import { Chip } from '../components/ui/Chip';
import { Select } from '../components/ui/Select';
import {
  ROLES, EXPERIENCE_LEVELS, DIFFICULTIES, INTERVIEW_TYPES, QUESTION_COUNTS,
  SKILLS, COMPANY_TYPES, TARGET_COMPANIES,
} from '../utils/constants';

const INTERVIEW_TYPE_META: Record<string, { icon: React.ElementType; desc: string }> = {
  Technical: { icon: Code2, desc: 'Hands-on and theory questions from your skills.' },
  HR: { icon: Users, desc: 'Soft-skills & situational questions.' },
  Behavioral: { icon: Heart, desc: 'Real-world scenarios and conduct.' },
  'System Design': { icon: Network, desc: 'Architecture, scale, and trade-offs.' },
  Mixed: { icon: Shuffle, desc: 'A balanced blend of all of the above.' },
};

const SectionCard: React.FC<{ title: string; subtitle?: string; required?: boolean; children: React.ReactNode }> = ({
  title, subtitle, required, children,
}) => (
  <Card className="p-6 sm:p-7">
    <div className="mb-5">
      <h2 className="text-lg font-bold flex items-center gap-2">
        {title}
        {required && <span className="text-primary text-sm">*</span>}
      </h2>
      {subtitle && <p className="text-sm text-muted-foreground mt-1">{subtitle}</p>}
    </div>
    {children}
  </Card>
);

const SegmentedControl: React.FC<{
  options: readonly (string | number)[];
  value: string | number;
  onChange: (v: any) => void;
}> = ({ options, value, onChange }) => (
  <div className="flex flex-wrap gap-2">
    {options.map((opt) => (
      <button
        key={opt}
        type="button"
        onClick={() => onChange(opt)}
        className={`px-4 py-2.5 rounded-xl text-sm font-semibold border transition-all ${
          value === opt
            ? 'bg-primary text-primary-foreground border-primary shadow-sm shadow-primary/25'
            : 'bg-card text-muted-foreground border-border hover:border-primary/50 hover:text-foreground'
        }`}
      >
        {opt}
      </button>
    ))}
  </div>
);

export const InterviewSetup: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { toast } = useToast();
  const { isLoading, error } = useSelector((state: RootState) => state.interview);
  const { currentResume } = useSelector((state: RootState) => state.resume);

  const isResumeMode = new URLSearchParams(window.location.search).get('type') === 'resume';

  // ---- Resume-based (focused) flow ----
  const [resumeDifficulty, setResumeDifficulty] = useState('Medium');
  const [resumeExperience, setResumeExperience] = useState('Fresher');
  const [resumeCount, setResumeCount] = useState<number>(10);
  const [resumeSubmitting, setResumeSubmitting] = useState(false);

  const handleGenerateFromResume = async () => {
    if (!currentResume?._id) return;
    setResumeSubmitting(true);
    dispatch(setError(''));
    try {
      const response = await interviewApi.generateResumeQuestions({
        resumeId: currentResume._id,
        difficulty: resumeDifficulty,
        experienceLevel: resumeExperience,
        count: resumeCount,
      });
      dispatch(setSession(response.data.session));
      navigate(`/interview/room/${response.data.session._id}`);
    } catch (err: any) {
      const message = err.response?.data?.message || err.message || 'Failed to generate interview. Please try again.';
      toast(message, 'error');
      dispatch(setError(message));
    } finally {
      setResumeSubmitting(false);
    }
  };

  // ---- Personalized setup flow ----
  const [role, setRole] = useState('');
  const [customRole, setCustomRole] = useState('');
  const [experienceLevel, setExperienceLevel] = useState('');
  const [difficulty, setDifficulty] = useState('');
  const [interviewType, setInterviewType] = useState('');
  const [count, setCount] = useState<number>(10);
  const [skills, setSkills] = useState<string[]>([]);
  const [companyType, setCompanyType] = useState('');
  const [targetCompany, setTargetCompany] = useState('');
  const [customCompany, setCustomCompany] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const effectiveRole = role === 'Custom' ? customRole.trim() : role;
  const effectiveCompany = targetCompany === 'Custom' ? customCompany.trim() : targetCompany;

  const toggleSkill = (skill: string) => {
    setSkills((prev) => (prev.includes(skill) ? prev.filter((s) => s !== skill) : [...prev, skill]));
  };

  const isValid = useMemo(() => {
    return !!effectiveRole && !!experienceLevel && !!difficulty && !!interviewType && skills.length > 0;
  }, [effectiveRole, experienceLevel, difficulty, interviewType, skills]);

  const summary = useMemo(() => {
    if (!isValid) return 'Fill in the required fields to generate your interview.';
    const bits = [
      `${count}-question ${difficulty.toLowerCase()} ${interviewType} interview`,
      `for a ${effectiveRole}`,
      `(${experienceLevel})`,
      skills.length ? `covering ${skills.slice(0, 3).join(', ')}${skills.length > 3 ? ` +${skills.length - 3} more` : ''}` : '',
      effectiveCompany ? `styled after ${effectiveCompany}` : companyType ? `styled for a ${companyType.toLowerCase()}` : '',
    ].filter(Boolean);
    return bits.join(' ');
  }, [isValid, count, difficulty, interviewType, effectiveRole, experienceLevel, skills, effectiveCompany, companyType]);

  const handleGenerate = async () => {
    if (!isValid || submitting) return;
    setSubmitting(true);
    dispatch(setLoading(true));
    dispatch(setError(''));
    try {
      const response = await interviewApi.generateQuestions({
        role: effectiveRole,
        difficulty,
        experienceLevel,
        type: interviewType,
        count,
        skills,
        companyType: companyType || undefined,
        targetCompany: effectiveCompany || undefined,
      });
      dispatch(setSession(response.data.session));
      navigate(`/interview/room/${response.data.session._id}`);
    } catch (err: any) {
      const message = err.response?.data?.message || err.message || 'Failed to generate interview. Please try again.';
      toast(message, 'error');
      dispatch(setError(message));
    } finally {
      dispatch(setLoading(false));
      setSubmitting(false);
    }
  };

  const busy = submitting || isLoading;

  return (
    <div className="min-h-screen bg-background text-foreground pb-32">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 pt-10 pb-6">
        <div className="flex items-center gap-2 text-xs font-semibold text-primary uppercase tracking-wider mb-2">
          <Sparkles className="w-3.5 h-3.5" />
          <span>New Mock Interview</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">Configure your interview</h1>
        <p className="text-muted-foreground mt-2">
          Every field below shapes the questions you'll get — edit anything before you generate.
        </p>
      </div>

      <div className="max-w-4xl mx-auto px-4 sm:px-6 space-y-6">
        {error && (
          <div className="p-4 bg-destructive/10 text-destructive border border-destructive/20 rounded-xl text-sm">
            {error}
          </div>
        )}

        {/* Resume-based focused card */}
        {isResumeMode && (
          <Card className="p-6 sm:p-7 border-primary/30 bg-gradient-to-br from-primary/5 to-transparent relative">
            <div className="flex items-start justify-between gap-4 mb-5">
              <div className="flex items-center gap-3">
                <div className="p-3 bg-primary/10 text-primary rounded-2xl border border-primary/20">
                  <FileText className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-lg font-bold">Resume-based interview</h2>
                  <p className="text-sm text-muted-foreground mt-0.5">
                    {currentResume ? `Using: ${currentResume.originalFileName}` : 'No resume found — upload one first.'}
                  </p>
                </div>
              </div>
              <button onClick={() => navigate('/interview/setup')} className="text-muted-foreground hover:text-foreground">
                <X className="w-4 h-4" />
              </button>
            </div>

            {currentResume ? (
              <div className="space-y-5">
                <div>
                  <p className="text-sm font-semibold mb-2">Difficulty</p>
                  <SegmentedControl options={DIFFICULTIES} value={resumeDifficulty} onChange={setResumeDifficulty} />
                </div>
                <div>
                  <p className="text-sm font-semibold mb-2">Experience Level</p>
                  <SegmentedControl options={EXPERIENCE_LEVELS} value={resumeExperience} onChange={setResumeExperience} />
                </div>
                <div>
                  <p className="text-sm font-semibold mb-2">Question Count</p>
                  <SegmentedControl options={QUESTION_COUNTS} value={resumeCount} onChange={setResumeCount} />
                </div>
                <button
                  onClick={handleGenerateFromResume}
                  disabled={resumeSubmitting}
                  className="w-full flex items-center justify-center gap-2 px-6 py-3.5 bg-gradient-to-r from-primary to-cyan-500 text-primary-foreground font-bold rounded-xl shadow-lg shadow-primary/25 hover:shadow-xl transition-all disabled:opacity-50"
                >
                  {resumeSubmitting ? <Loader2 className="w-5 h-5 animate-spin" /> : <ArrowRight className="w-5 h-5" />}
                  <span>Generate from Resume</span>
                </button>
              </div>
            ) : (
              <button
                onClick={() => navigate('/resume/upload?redirect=interview')}
                className="w-full flex items-center justify-center gap-2 px-6 py-3.5 bg-primary text-primary-foreground font-bold rounded-xl shadow-md transition-all"
              >
                <Briefcase className="w-5 h-5" />
                <span>Upload Resume</span>
              </button>
            )}
          </Card>
        )}

        {/* Role */}
        <SectionCard title="Role" subtitle="What position are you interviewing for?" required>
          <Select value={role} onChange={(e) => setRole(e.target.value)}>
            <option value="" disabled>Select a role…</option>
            {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
            <option value="Custom">Custom…</option>
          </Select>
          {role === 'Custom' && (
            <input
              type="text"
              value={customRole}
              onChange={(e) => setCustomRole(e.target.value)}
              placeholder="e.g. Site Reliability Engineer"
              className="mt-3 w-full rounded-xl border border-input bg-card px-4 py-3 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
          )}
        </SectionCard>

        {/* Experience & Difficulty */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <SectionCard title="Experience Level" required>
            <SegmentedControl options={EXPERIENCE_LEVELS} value={experienceLevel} onChange={setExperienceLevel} />
          </SectionCard>
          <SectionCard title="Difficulty" required>
            <SegmentedControl options={DIFFICULTIES} value={difficulty} onChange={setDifficulty} />
          </SectionCard>
        </div>

        {/* Interview Type */}
        <SectionCard title="Interview Type" required>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
            {INTERVIEW_TYPES.map((type) => {
              const meta = INTERVIEW_TYPE_META[type];
              const Icon = meta.icon;
              const selected = interviewType === type;
              return (
                <button
                  key={type}
                  type="button"
                  onClick={() => setInterviewType(type)}
                  className={`p-4 rounded-2xl border text-left transition-all flex flex-col gap-2 ${
                    selected ? 'border-primary bg-primary/10' : 'border-border hover:border-primary/50 bg-card'
                  }`}
                >
                  <Icon className={`w-5 h-5 ${selected ? 'text-primary' : 'text-muted-foreground'}`} />
                  <span className="font-semibold text-sm">{type}</span>
                  <span className="text-xs text-muted-foreground leading-snug">{meta.desc}</span>
                </button>
              );
            })}
          </div>
        </SectionCard>

        {/* Question Count */}
        <SectionCard title="Question Count">
          <SegmentedControl options={QUESTION_COUNTS} value={count} onChange={setCount} />
        </SectionCard>

        {/* Skills */}
        <SectionCard title="Skills" subtitle="Select every skill you want questions about." required>
          <div className="flex flex-wrap gap-2">
            {SKILLS.map((skill) => (
              <Chip key={skill} label={skill} selected={skills.includes(skill)} onClick={() => toggleSkill(skill)} />
            ))}
          </div>
        </SectionCard>

        {/* Company Type */}
        <SectionCard title="Company Type" subtitle="Optional — flavors the interview style.">
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {COMPANY_TYPES.map((type) => (
              <button
                key={type}
                type="button"
                onClick={() => setCompanyType(companyType === type ? '' : type)}
                className={`p-4 rounded-2xl border text-sm font-semibold transition-all ${
                  companyType === type ? 'border-primary bg-primary/10 text-primary' : 'border-border hover:border-primary/50 bg-card text-muted-foreground'
                }`}
              >
                {type}
              </button>
            ))}
          </div>
        </SectionCard>

        {/* Target Company */}
        <SectionCard title="Target Company" subtitle="Optional — generates questions in their interview style.">
          <Select value={targetCompany} onChange={(e) => setTargetCompany(e.target.value)}>
            <option value="">No specific company</option>
            {TARGET_COMPANIES.map((c) => <option key={c} value={c}>{c}</option>)}
            <option value="Custom">Custom…</option>
          </Select>
          {targetCompany === 'Custom' && (
            <input
              type="text"
              value={customCompany}
              onChange={(e) => setCustomCompany(e.target.value)}
              placeholder="e.g. Zoho"
              className="mt-3 w-full rounded-xl border border-input bg-card px-4 py-3 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
          )}
        </SectionCard>
      </div>

      {/* Sticky CTA bar */}
      <motion.div
        initial={{ y: 80, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 0.2 }}
        className="fixed bottom-0 left-0 right-0 z-40 border-t border-border bg-card/90 backdrop-blur-xl"
      >
        <div className="max-w-4xl mx-auto px-4 sm:px-6 py-4 flex flex-col sm:flex-row items-center gap-3 sm:gap-6">
          <p className="text-xs sm:text-sm text-muted-foreground flex-1 text-center sm:text-left line-clamp-2">
            {summary}
          </p>
          <button
            onClick={handleGenerate}
            disabled={!isValid || busy}
            className="w-full sm:w-auto shrink-0 flex items-center justify-center gap-2 px-8 py-3.5 bg-gradient-to-r from-primary to-cyan-500 text-primary-foreground font-bold rounded-xl shadow-lg shadow-primary/25 hover:shadow-xl transition-all disabled:opacity-40 disabled:shadow-none"
          >
            {busy ? (
              <><Loader2 className="w-5 h-5 animate-spin" /> Generating…</>
            ) : (
              <>Generate Interview <ChevronRight className="w-5 h-5" /></>
            )}
          </button>
        </div>
      </motion.div>

      {busy && (
        <div className="fixed inset-0 bg-background/70 backdrop-blur-sm z-50 flex flex-col items-center justify-center">
          <Loader2 className="w-12 h-12 text-primary animate-spin mb-4" />
          <p className="text-lg font-bold">Building your personalized interview…</p>
          <p className="text-sm text-muted-foreground mt-1">This usually takes a few seconds.</p>
        </div>
      )}
    </div>
  );
};
