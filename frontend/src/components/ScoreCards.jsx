import React from 'react';

function getScoreClass(score) {
  if (score == null) return '';
  if (score >= 80) return 'score-good';
  if (score >= 60) return 'score-warn';
  return 'score-bad';
}

export default function ScoreCards({ review }) {
  const scores = [
    { label: 'Quality',      value: review.qualityScore },
    { label: 'Readability',  value: review.readabilityScore },
    { label: 'Security',     value: review.securityScore },
    { label: 'Performance',  value: review.performanceScore },
  ];

  return (
    <div className="score-grid">
      {scores.map(({ label, value }) => (
        <div className="score-card" key={label}>
          <div className={`score-value ${getScoreClass(value)}`}>
            {value ?? '—'}
          </div>
          <div className="score-label">{label}</div>
          {value != null && (
            <div style={{ marginTop: '8px', height: '4px', borderRadius: '999px', background: 'var(--border)' }}>
              <div style={{
                height: '100%',
                width: `${value}%`,
                borderRadius: '999px',
                background: value >= 80 ? 'var(--success)' : value >= 60 ? 'var(--warning)' : 'var(--danger)',
                transition: 'width 0.8s ease',
              }} />
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
