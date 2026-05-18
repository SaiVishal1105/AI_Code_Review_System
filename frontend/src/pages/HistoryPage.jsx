import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { reviewApi } from '../utils/api';
import toast from 'react-hot-toast';

function scoreColor(score) {
  if (score == null) return 'var(--text-muted)';
  if (score >= 80) return 'var(--success)';
  if (score >= 60) return 'var(--warning)';
  return 'var(--danger)';
}

export default function HistoryPage() {
  const navigate = useNavigate();
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);

  const loadReviews = (p = 0) => {
    setLoading(true);
    reviewApi.getHistory(p, 15)
      .then(r => {
        setReviews(r.data);
        setPage(p);
      })
      .catch(() => toast.error('Failed to load history'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadReviews(0); }, []);

  const handleDelete = async (e, id) => {
    e.stopPropagation();
    if (!window.confirm('Delete this review?')) return;
    await reviewApi.delete(id);
    toast.success('Deleted');
    loadReviews(page);
  };

  return (
    <div>
      <div className="page-header flex justify-between items-center">
        <div>
          <h1 className="page-title">Review History</h1>
          <p className="page-subtitle">All your past code reviews</p>
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/review')}>
          + New Review
        </button>
      </div>

      <div className="card" style={{ padding: 0 }}>
        {loading ? (
          <div className="loading-spinner" />
        ) : reviews.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">📋</div>
            <div className="empty-state-text">No reviews yet</div>
            <div className="empty-state-sub">Submit your first code review to get started</div>
            <button className="btn btn-primary" style={{ marginTop: '16px' }} onClick={() => navigate('/review')}>
              Start a Review
            </button>
          </div>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>#</th>
                <th>Language</th>
                <th>Quality</th>
                <th>Status</th>
                <th>Date</th>
                <th>Preview</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {reviews.map((r) => (
                <tr
                  key={r.id}
                  onClick={() => navigate(`/review/${r.id}`)}
                  style={{ cursor: 'pointer' }}
                >
                  <td className="text-mono" style={{ color: 'var(--text-muted)' }}>#{r.id}</td>
                  <td><span className="badge badge-primary">{r.language}</span></td>
                  <td>
                    <span style={{ fontWeight: 800, fontFamily: 'var(--font-mono)', color: scoreColor(r.qualityScore) }}>
                      {r.qualityScore != null ? `${r.qualityScore}/100` : '—'}
                    </span>
                  </td>
                  <td>
                    <span className={`badge ${r.status === 'COMPLETED' ? 'badge-success' : r.status === 'FAILED' ? 'badge-danger' : 'badge-warning'}`}>
                      {r.status}
                    </span>
                  </td>
                  <td style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>{r.createdAt}</td>
                  <td style={{ maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontFamily: 'var(--font-mono)', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                    {r.codePreview}
                  </td>
                  <td>
                    <button
                      className="btn btn-danger btn-sm"
                      onClick={(e) => handleDelete(e, r.id)}
                    >
                      🗑
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {reviews.length === 15 && (
        <div className="flex gap-2 mt-4" style={{ justifyContent: 'center' }}>
          <button className="btn btn-outline" onClick={() => loadReviews(page - 1)} disabled={page === 0}>
            ← Previous
          </button>
          <button className="btn btn-outline" onClick={() => loadReviews(page + 1)}>
            Next →
          </button>
        </div>
      )}
    </div>
  );
}
