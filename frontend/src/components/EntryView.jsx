import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api.js';

const styles = {
  backBtn: {
    background: 'none',
    border: 'none',
    color: '#5c7a5c',
    cursor: 'pointer',
    fontSize: '0.9rem',
    marginBottom: '1.5rem',
    padding: 0,
    textDecoration: 'underline',
  },
  title: {
    fontSize: '1rem',
    color: '#888',
    marginBottom: '1rem',
    fontStyle: 'italic',
  },
  content: {
    background: '#fff',
    border: '1px solid #e5dcc8',
    borderRadius: '6px',
    padding: '1.5rem',
    whiteSpace: 'pre-wrap',
    lineHeight: '1.8',
    fontSize: '1rem',
    minHeight: '200px',
    color: '#333',
    fontFamily: 'Georgia, serif',
  },
  error: { color: '#c00' },
};

export default function EntryView() {
  const { year, month, filename } = useParams();
  const navigate = useNavigate();
  const [content, setContent] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.get(`/api/entries/${year}/${month}/${filename}`)
      .then(({ data }) => setContent(data))
      .catch((err) => setError(err.message));
  }, [year, month, filename]);

  if (error) return <p style={styles.error}>Error: {error}</p>;

  const displayName = filename.replace('entry-', '').replace('.txt', '');

  return (
    <div>
      <button style={styles.backBtn} onClick={() => navigate('/entries')}>
        ← Back to entries
      </button>
      <p style={styles.title}>
        {year} / {month} / {displayName}
      </p>
      {content === null
        ? <p style={{ color: '#888' }}>Loading…</p>
        : <div style={styles.content}>{content}</div>
      }
    </div>
  );
}
