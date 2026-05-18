import React, { useState } from 'react';
import Editor from '@monaco-editor/react';
import { reviewApi } from '../utils/api';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

const LANGUAGES = [
  { value: 'java',       label: '☕ Java' },
  { value: 'python',     label: '🐍 Python' },
  { value: 'javascript', label: '🟡 JavaScript' },
  { value: 'typescript', label: '🔷 TypeScript' },
  { value: 'c++',        label: '⚙️  C++' },
  { value: 'go',         label: '🐹 Go' },
  { value: 'rust',       label: '🦀 Rust' },
  { value: 'kotlin',     label: '🎯 Kotlin' },
];

const SAMPLE_CODE = {
  java: `public class Calculator {
    public int divide(int a, int b) {
        return a / b;  // Potential division by zero!
    }
    
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        int result = calc.divide(10, 0);
        System.out.println(result);
    }
}`,
  python: `def calculate_average(numbers):
    total = 0
    for num in numbers:
        total = total + num
    return total / len(numbers)  # No check for empty list!

data = [1, 2, 3, 4, 5]
print(calculate_average(data))`,
  javascript: `function fetchUserData(userId) {
    fetch('/api/users/' + userId)
        .then(response => response.json())
        .then(data => {
            document.getElementById('user').innerHTML = data.name;
        });
    // No error handling, potential XSS vulnerability
}`,
};

export default function ReviewPage() {
  const navigate = useNavigate();
  const [language, setLanguage] = useState('java');
  const [code, setCode] = useState(SAMPLE_CODE.java);
  const [loading, setLoading] = useState(false);

  const handleLangChange = (e) => {
    const lang = e.target.value;
    setLanguage(lang);
    if (SAMPLE_CODE[lang]) setCode(SAMPLE_CODE[lang]);
  };

  const handleSubmit = async () => {
    if (!code.trim()) { toast.error('Please enter some code'); return; }
    setLoading(true);
    const toastId = toast.loading('🤖 AI is reviewing your code...');
    try {
      const res = await reviewApi.submit({ language, code });
      toast.success('Review complete!', { id: toastId });
      navigate(`/review/${res.data.id}`);
    } catch (err) {
      toast.error(err.response?.data?.error || 'Review failed', { id: toastId });
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">New Code Review</h1>
        <p className="page-subtitle">Paste your code and let AI find bugs, security issues, and improvements</p>
      </div>

      <div className="card" style={{ marginBottom: '20px' }}>
        <div className="flex items-center justify-between" style={{ marginBottom: '20px' }}>
          <div className="form-group" style={{ minWidth: '200px' }}>
            <label className="label">Language</label>
            <select className="select" value={language} onChange={handleLangChange}>
              {LANGUAGES.map(({ value, label }) => (
                <option key={value} value={value}>{label}</option>
              ))}
            </select>
          </div>

          <div className="flex gap-2">
            <button
              className="btn btn-outline btn-sm"
              onClick={() => setCode(SAMPLE_CODE[language] || '')}
            >
              Load Sample
            </button>
            <button
              className="btn btn-outline btn-sm"
              onClick={() => setCode('')}
            >
              Clear
            </button>
          </div>
        </div>

        {/* Monaco Editor */}
        <div style={{
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius)',
          overflow: 'hidden',
          minHeight: '400px',
        }}>
          <Editor
            height="400px"
            language={language === 'c++' ? 'cpp' : language}
            value={code}
            onChange={(val) => setCode(val || '')}
            theme="vs-dark"
            options={{
              fontSize: 14,
              fontFamily: "'JetBrains Mono', monospace",
              minimap: { enabled: false },
              padding: { top: 16, bottom: 16 },
              lineNumbers: 'on',
              renderLineHighlight: 'gutter',
              scrollBeyondLastLine: false,
              wordWrap: 'on',
            }}
          />
        </div>

        <div style={{ marginTop: '16px', display: 'flex', gap: '12px', alignItems: 'center' }}>
          <button
            className="btn btn-primary btn-lg"
            onClick={handleSubmit}
            disabled={loading}
          >
            {loading
              ? <><span className="spinner-sm" /> Analyzing with AI...</>
              : '🔍 Review Code'}
          </button>
          <span className="text-muted">
            {code.length.toLocaleString()} characters
          </span>
        </div>
      </div>

      {/* Tips */}
      <div className="card card-sm">
        <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600, marginBottom: '8px' }}>
          💡 Tips for better reviews
        </p>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '8px', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
          <div>✅ Include complete functions</div>
          <div>✅ Add context with comments</div>
          <div>✅ Use realistic variable names</div>
        </div>
      </div>
    </div>
  );
}
