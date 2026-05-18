import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import { reviewApi } from '../utils/api';
import ScoreCards from '../components/ScoreCards';
import toast from 'react-hot-toast';

export default function ReviewDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [review, setReview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('analysis');
  const [refactoring, setRefactoring] = useState(false);
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    reviewApi.getOne(id)
      .then(r => setReview(r.data))
      .catch(() => { toast.error('Review not found'); navigate('/history'); })
      .finally(() => setLoading(false));
  }, [id, navigate]);

  const handleRefactor = async () => {
    setRefactoring(true);
    const tid = toast.loading('🤖 AI is refactoring your code...');
    try {
      const res = await reviewApi.refactor(id);
      setReview(prev => ({ ...prev, refactoredCode: res.data.refactoredCode }));
      setActiveTab('refactored');
      toast.success('Refactoring complete!', { id: tid });
    } catch (err) {
      toast.error('Refactor failed', { id: tid });
    } finally {
      setRefactoring(false);
    }
  };

  const handleDownloadPdf = async () => {
    setDownloading(true);
    try {
      const res = await reviewApi.downloadPdf(id);
      const url = URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
      const a = document.createElement('a');
      a.href = url;
      a.download = `code-review-${id}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
      toast.success('PDF downloaded!');
    } catch {
      toast.error('PDF download failed');
    } finally {
      setDownloading(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Delete this review?')) return;
    await reviewApi.delete(id);
    toast.success('Review deleted');
    navigate('/history');
  };

  if (loading) return <div className="loading-spinner" />;
  if (!review)  return null;

  const lang = review.language === 'c++' ? 'cpp' : review.language;

  return (
    <div>
      {/* Header */}
      <div className="page-header flex justify-between items-center">
        <div>
          <div className="flex items-center gap-2" style={{ marginBottom: '6px' }}>
            <button className="btn btn-outline btn-sm" onClick={() => navigate(-1)}>← Back</button>
            <span className="badge badge-primary">{review.language}</span>
            <span className="text-muted" style={{ fontSize: '0.8rem' }}>{review.createdAt}</span>
          </div>
          <h1 className="page-title">Code Review #{review.id}</h1>
          {review.summary && (
            <p className="page-subtitle" style={{ maxWidth: '600px', marginTop: '6px' }}>
              {review.summary}
            </p>
          )}
        </div>
        <div className="flex gap-2">
          <button className="btn btn-outline btn-sm" onClick={handleRefactor} disabled={refactoring}>
            {refactoring ? <><span className="spinner-sm" /> Refactoring...</> : '✨ AI Refactor'}
          </button>
          <button className="btn btn-outline btn-sm" onClick={handleDownloadPdf} disabled={downloading}>
            {downloading ? <span className="spinner-sm" /> : '📄'} PDF
          </button>
          <button className="btn btn-danger btn-sm" onClick={handleDelete}>🗑</button>
        </div>
      </div>

      {/* Scores */}
      <div className="card" style={{ marginBottom: '20px' }}>
        <ScoreCards review={review} />
      </div>

      {/* Tabs */}
      <div className="tabs">
        {[
          { key: 'analysis', label: '🔍 Analysis' },
          { key: 'code', label: '📝 Original Code' },
          ...(review.refactoredCode ? [{ key: 'refactored', label: '✨ Refactored' }] : []),
        ].map(({ key, label }) => (
          <button
            key={key}
            className={`tab ${activeTab === key ? 'active' : ''}`}
            onClick={() => setActiveTab(key)}
          >
            {label}
          </button>
        ))}
      </div>

      {/* Analysis Tab */}
      {activeTab === 'analysis' && (
        <div className="grid-2">
          <div>
            <ReviewSection title="🐛 Issues" items={review.issues} type="issue" emptyMsg="No critical issues found!" />
            <ReviewSection title="🔒 Security" items={review.securityWarnings} type="security" emptyMsg="No security concerns found." />
          </div>
          <div>
            <ReviewSection title="💡 Suggestions" items={review.suggestions} type="suggest" emptyMsg="No suggestions." />
            <ReviewSection title="⚡ Performance" items={review.performanceNotes} type="perf" emptyMsg="No performance notes." />
          </div>
        </div>
      )}

      {/* Code Tabs */}
      {(activeTab === 'code' || activeTab === 'refactored') && (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <Editor
            height="600px"
            language={lang}
            value={activeTab === 'code' ? review.originalCode : review.refactoredCode}
            theme="vs-dark"
            options={{
              readOnly: true,
              fontSize: 14,
              fontFamily: "'JetBrains Mono', monospace",
              minimap: { enabled: true },
              padding: { top: 16 },
              scrollBeyondLastLine: false,
              wordWrap: 'on',
            }}
          />
        </div>
      )}
    </div>
  );
}

function ReviewSection({ title, items, type, emptyMsg }) {
  return (
    <div className="review-section">
      <div className="review-section-title">
        <span>{title}</span>
        {items?.length > 0 && (
          <span style={{
            background: 'var(--border)',
            borderRadius: '999px',
            padding: '1px 8px',
            fontSize: '0.7rem',
            fontWeight: 700,
          }}>{items.length}</span>
        )}
      </div>
      <div className="review-items">
        {!items?.length
          ? <div className="review-item" style={{ borderLeftColor: 'var(--success)', color: 'var(--success)' }}>{emptyMsg}</div>
          : items.map((item, i) => (
              <div key={i} className={`review-item ${type}`}>{item}</div>
            ))
        }
      </div>
    </div>
  );
}
