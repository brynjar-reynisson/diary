import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api.js';

const styles = {
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '1.5rem',
  },
  h2: { fontSize: '1.3rem', color: '#444' },
  newBtn: {
    padding: '0.5rem 1.2rem',
    background: '#5c7a5c',
    color: '#fff',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
    fontSize: '0.9rem',
  },
  year: {
    marginBottom: '1rem',
  },
  yearTitle: {
    fontSize: '1.1rem',
    fontWeight: 'bold',
    color: '#666',
    cursor: 'pointer',
    userSelect: 'none',
    padding: '0.3rem 0',
    borderBottom: '1px solid #ddd',
    marginBottom: '0.5rem',
  },
  month: {
    marginLeft: '1rem',
    marginBottom: '0.5rem',
  },
  monthTitle: {
    fontSize: '0.95rem',
    color: '#888',
    cursor: 'pointer',
    userSelect: 'none',
    padding: '0.2rem 0',
  },
  entryLink: {
    display: 'block',
    marginLeft: '1.5rem',
    padding: '0.25rem 0',
    color: '#5c7a5c',
    textDecoration: 'none',
    fontSize: '0.9rem',
  },
  empty: { color: '#aaa', fontStyle: 'italic', marginTop: '2rem', textAlign: 'center' },
  error: { color: '#c00', marginTop: '1rem' },
};

const MONTH_NAMES = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

function monthName(m) {
  const idx = parseInt(m, 10) - 1;
  return MONTH_NAMES[idx] ?? m;
}

export default function EntryList() {
  const [entries, setEntries] = useState(null);
  const [error, setError] = useState(null);
  const [collapsed, setCollapsed] = useState({});
  const navigate = useNavigate();

  useEffect(() => {
    api.get('/api/entries')
      .then(({ data }) => setEntries(data))
      .catch((err) => setError(err.message));
  }, []);

  function toggle(key) {
    setCollapsed((c) => ({ ...c, [key]: !c[key] }));
  }

  if (error) return <p style={styles.error}>Error: {error}</p>;
  if (!entries) return <p style={{ color: '#888' }}>Loading entries…</p>;

  const years = useMemo(() => Object.keys(entries).sort((a, b) => b - a), [entries]);

  return (
    <div>
      <div style={styles.header}>
        <h2 style={styles.h2}>My Entries</h2>
        <button style={styles.newBtn} onClick={() => navigate('/new')}>+ New Entry</button>
      </div>

      {years.length === 0 && (
        <p style={styles.empty}>No entries yet. Write your first one!</p>
      )}

      {years.map((year) => (
        <div key={year} style={styles.year}>
          <div style={styles.yearTitle} onClick={() => toggle(`y-${year}`)} role="button" tabIndex={0}>
            {collapsed[`y-${year}`] ? '▶' : '▼'} {year}
          </div>
          {!collapsed[`y-${year}`] && Object.keys(entries[year]).sort((a, b) => b - a).map((month) => (
            <div key={month} style={styles.month}>
              <div style={styles.monthTitle} onClick={() => toggle(`m-${year}-${month}`)} role="button" tabIndex={0}>
                {collapsed[`m-${year}-${month}`] ? '▶' : '▼'} {monthName(month)}
              </div>
              {!collapsed[`m-${year}-${month}`] && entries[year][month].map((entry) => (
                <Link
                  key={entry.filename}
                  to={`/entries/${year}/${month}/${entry.filename}`}
                  style={styles.entryLink}
                >
                  {entry.filename.replace('entry-', '').replace('.txt', '')}
                </Link>
              ))}
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}
