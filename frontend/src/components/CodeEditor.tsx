import React, { useState } from 'react';
import Editor from '@monaco-editor/react';
import { Play, Send, Loader2, Code2, AlertCircle } from 'lucide-react';

interface CodeEditorProps {
  starterCode?: string;
  expectedOutput?: string;
  testCases?: any[];
  onSubmitAnswer: (code: string, executionOutput: string, passed: boolean) => void;
}

// Executed via Judge0 CE's free public instance (ce.judge0.com) — no API key required.
// language_id values come from GET https://ce.judge0.com/languages
const LANGUAGES = [
  { id: 'javascript', name: 'JavaScript (Node.js)', version: '18.15.0', judgeId: 93, defaultCode: 'function solve(input) {\n  return input;\n}\n\nconsole.log(solve("test"));' },
  { id: 'python', name: 'Python 3', version: '3.11.2', judgeId: 92, defaultCode: 'def solve(input):\n  return input\n\nprint(solve("test"))' },
  { id: 'java', name: 'Java', version: '17.0.6', judgeId: 91, defaultCode: 'public class Main {\n  public static void main(String[] args) {\n    System.out.println("test");\n  }\n}' },
  { id: 'cpp', name: 'C++', version: '14.1.0', judgeId: 105, defaultCode: '#include <iostream>\nusing namespace std;\n\nint main() {\n  cout << "test" << endl;\n  return 0;\n}' }
];

export const CodeEditor: React.FC<CodeEditorProps> = ({ starterCode, expectedOutput, testCases, onSubmitAnswer }) => {
  const [language, setLanguage] = useState(LANGUAGES[0]);
  const [code, setCode] = useState(starterCode || LANGUAGES[0].defaultCode);
  const [output, setOutput] = useState<string>('');
  const [isRunning, setIsRunning] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleLanguageChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const lang = LANGUAGES.find(l => l.id === e.target.value) || LANGUAGES[0];
    setLanguage(lang);
    setCode(starterCode || lang.defaultCode);
  };

  const executeCode = async () => {
    setIsRunning(true);
    setOutput('Executing code...');
    try {
      const response = await fetch('https://ce.judge0.com/submissions?base64_encoded=false&wait=true', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          source_code: code,
          language_id: language.judgeId,
          stdin: '',
        })
      });

      const data = await response.json();
      const combined = [data.compile_output, data.stdout, data.stderr]
        .filter((part): part is string => !!part && part.trim().length > 0)
        .map((part) => part.trim())
        .join('\n\n');

      setOutput(combined || data.status?.description || 'Execution completed with no output.');
      return data;
    } catch (err: any) {
      setOutput(`Failed to execute code: ${err.message}`);
      return null;
    } finally {
      setIsRunning(false);
    }
  };

  const handleSubmit = async () => {
    setIsSubmitting(true);
    const result = await executeCode();

    // Evaluate correctness based on expected output (basic matching)
    let passed = false;
    let execOutput = output;

    if (result) {
      execOutput = [result.compile_output, result.stdout, result.stderr]
        .filter((part: string | null) => !!part && part.trim().length > 0)
        .map((part: string) => part.trim())
        .join('\n\n') || (result.status?.description ?? '');

      const stdout = (result.stdout || '').trim();
      if (expectedOutput && stdout.includes(expectedOutput.trim())) {
        passed = true;
      } else if (result.status?.id === 3) {
        // Judge0 status 3 = "Accepted" — ran to completion with no compile/runtime error
        passed = true;
      }
    }

    onSubmitAnswer(code, execOutput, passed);
    setIsSubmitting(false);
  };

  return (
    <div className="flex flex-col h-[500px] bg-[#1e1e1e] rounded-xl overflow-hidden border border-border shadow-2xl relative">
      <div className="flex items-center justify-between px-4 py-3 bg-[#2d2d2d] border-b border-white/10">
        <div className="flex items-center space-x-3">
          <Code2 className="w-5 h-5 text-blue-400" />
          <select 
            value={language.id} 
            onChange={handleLanguageChange}
            className="bg-[#1e1e1e] text-white border border-white/20 rounded px-2 py-1 text-sm focus:outline-none focus:border-blue-500"
          >
            {LANGUAGES.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
          </select>
        </div>
        <div className="flex items-center space-x-2">
          <button 
            onClick={executeCode} 
            disabled={isRunning || isSubmitting}
            className="flex items-center px-3 py-1.5 bg-white/10 hover:bg-white/20 text-white text-sm rounded transition-colors disabled:opacity-50"
          >
            {isRunning ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : <Play className="w-4 h-4 mr-2" />}
            Run
          </button>
          <button 
            onClick={handleSubmit} 
            disabled={isRunning || isSubmitting}
            className="flex items-center px-3 py-1.5 bg-blue-600 hover:bg-blue-500 text-white text-sm rounded transition-colors disabled:opacity-50 font-medium"
          >
            {isSubmitting ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : <Send className="w-4 h-4 mr-2" />}
            Submit Code
          </button>
        </div>
      </div>
      
      <div className="flex-1 grid grid-rows-3 lg:grid-rows-1 lg:grid-cols-3">
        <div className="row-span-2 lg:col-span-2 border-b lg:border-b-0 lg:border-r border-white/10 relative">
          <Editor
            height="100%"
            language={language.id === 'cpp' ? 'cpp' : language.id}
            theme="vs-dark"
            value={code}
            onChange={(val) => setCode(val || '')}
            options={{
              minimap: { enabled: false },
              fontSize: 14,
              padding: { top: 16 },
              scrollBeyondLastLine: false,
              smoothScrolling: true
            }}
          />
        </div>
        
        <div className="row-span-1 lg:col-span-1 bg-[#1e1e1e] flex flex-col">
          <div className="px-4 py-2 bg-[#2d2d2d] border-b border-white/10 text-xs font-semibold text-gray-400 uppercase tracking-wider">
            Execution Output
          </div>
          <div className="p-4 flex-1 overflow-auto font-mono text-sm text-gray-300 whitespace-pre-wrap">
            {output || <span className="text-gray-600 italic">Run your code to see output...</span>}
          </div>
        </div>
      </div>
    </div>
  );
};
