import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { reviewApi } from '../utils/api';
import { useAuth } from '../hooks/useAuth';

const LANG_COLORS = {
  java: '#f59e0b',
  python: '#3b82f6',
  javascript: '#eab308',
  typescript: '#06b6d4',
  'c++': '#8b5cf6',
  default: '#6366f1',
};

export default function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    reviewApi.dashboard()
      .then(r => setStats(r.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading-spinner" />;

  const langData = stats?.byLanguage
    ? Object.entries(stats.byLanguage).map(([lang, count]) => ({ lang, count }))
    : [];

  return (
    <div>
      <div className="page-header flex justify-between items-center">
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="page-subtitle">Welcome back, {user?.username} 👋</p>
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/review')}>
          + New Review
        </button>
      </div>

      {/* Stats */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-value">{stats?.totalReviews ?? 0}</div>
          <div className="stat-label">Total Reviews</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">
            {stats?.avgQualityScore ? Math.round(stats.avgQualityScore) : '—'}
          </div>
          <div className="stat-label">Avg. Quality Score</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{langData.length}</div>
          <div className="stat-label">Languages Reviewed</div>
        </div>
      </div>

      {/* Charts + Recent */}
      <div className="grid-2">
        {/* Language Breakdown */}
        <div className="card">
          <h3 style={{ fontWeight: 700, marginBottom: '20px', fontSize: '0.9rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)' }}>
            Reviews by Language
          </h3>
          {langData.length === 0 ? (
            <div className="empty-state">
              <div className="empty-state-icon">📊</div>
              <div className="empty-state-text">No data yet</div>
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={langData} barSize={32}>
                <XAxis dataKey="lang" tick={{ fill: 'var(--text-muted)', fontSize: 12 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 12 }} axisLine={false} tickLine={false} />
                <Tooltip
                  contentStyle={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: '8px' }}
                  labelStyle={{ color: 'var(--text-primary)' }}
                />
                <Bar dataKey="count" radius={[6,6,0,0]}>
                  {langData.map(({ lang }) => (
                    <Cell key={lang} fill={LANG_COLORS[lang.toLowerCase()] || LANG_COLORS.default} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Recent Reviews */}
        <div className="card">
          <h3 style={{ fontWeight: 700, marginBottom: '20px', fontSize: '0.9rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)' }}>
            Recent Reviews
          </h3>
          {!stats?.recentReviews?.length ? (
            <div className="empty-state">
              <div className="empty-state-icon">🕒</div>
              <div className="empty-state-text">No reviews yet</div>
              <div className="empty-state-sub">Submit your first code review!</div>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {stats.recentReviews.map((r) => (
                <div
                  key={r.id}
                  className="card card-sm card-hover"
                  style={{ cursor: 'pointer' }}
                  onClick={() => navigate(`/review/${r.id}`)}
                >
                  <div className="flex justify-between items-center">
                    <div>
                      <span className="badge badge-primary" style={{ marginRight: '8px' }}>
                        {r.language}
                      </span>
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                        {r.createdAt}
                      </span>
                    </div>
                    <div style={{
                      fontWeight: 800,
                      fontFamily: 'var(--font-mono)',
                      color: r.qualityScore >= 80 ? 'var(--success)' : r.qualityScore >= 60 ? 'var(--warning)' : 'var(--danger)',
                    }}>
                      {r.qualityScore ?? '…'}/100
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
