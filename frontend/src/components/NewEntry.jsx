import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
  title: { fontSize: '1.2rem', color: '#555', marginBottom: '1rem' },
  textarea: {
    width: '100%',
    minHeight: '400px',
    padding: '1rem',
    fontSize: '1rem',
    fontFamily: 'Georgia, serif',
    lineHeight: '1.8',
    border: '1px solid #e5dcc8',
    borderRadius: '6px',
    background: '#fff',
    color: '#333',
    resize: 'vertical',
    outline: 'none',
  },
  footer: {
    display: 'flex',
    justifyContent: 'flex-end',
    alignItems: 'center',
    gap: '1rem',
    marginTop: '1rem',
  },
  saveBtn: {
    padding: '0.6rem 1.8rem',
    background: '#5c7a5c',
    color: '#fff',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
    fontSize: '1rem',
  },
  saveBtnDisabled: {
    padding: '0.6rem 1.8rem',
    background: '#aaa',
    color: '#fff',
    border: 'none',
    borderRadius: '5px',
    cursor: 'not-allowed',
    fontSize: '1rem',
  },
  error: { color: '#c00', fontSize: '0.9rem' },
};

export default function NewEntry() {
  const [content, setContent] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  async function handleSave() {
    if (!content.trim()) return;
    setSaving(true);
    setError(null);
    try {
      await api.post('/api/entries', { content });
      navigate('/entries');
    } catch (err) {
      setError(err.response?.data?.error ?? err.message);
      setSaving(false);
    }
  }

  return (
    <div>
      <button style={styles.backBtn} onClick={() => navigate('/entries')}>
        ← Back to entries
      </button>
      <h2 style={styles.title}>New Entry</h2>
      <textarea
        style={styles.textarea}
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder="Write your thoughts…"
        autoFocus
      />
      <div style={styles.footer}>
        {error && <span style={styles.error}>{error}</span>}
        <button
          style={saving ? styles.saveBtnDisabled : styles.saveBtn}
          onClick={handleSave}
          disabled={saving || !content.trim()}
        >
          {saving ? 'Saving…' : 'Save'}
        </button>
      </div>
    </div>
  );
}
