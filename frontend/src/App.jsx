import React, { useEffect, useState } from 'react';
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  useNavigate,
} from 'react-router-dom';
import api from './api.js';
import AuthSelector from './components/AuthSelector.jsx';
import EntryList from './components/EntryList.jsx';
import EntryView from './components/EntryView.jsx';
import NewEntry from './components/NewEntry.jsx';

const styles = {
  nav: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '2rem',
    paddingBottom: '1rem',
    borderBottom: '1px solid #ccc',
  },
  h1: { fontSize: '1.5rem', fontStyle: 'italic', color: '#555' },
  logoutBtn: {
    background: 'none',
    border: '1px solid #aaa',
    borderRadius: '4px',
    padding: '0.3rem 0.8rem',
    cursor: 'pointer',
    fontSize: '0.85rem',
    color: '#666',
  },
};

function Nav({ provider, onLogout }) {
  return (
    <nav style={styles.nav}>
      <h1 style={styles.h1}>My Diary</h1>
      {provider && (
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <span style={{ fontSize: '0.8rem', color: '#888' }}>
            via {provider}
          </span>
          <button style={styles.logoutBtn} onClick={onLogout}>
            Logout
          </button>
        </div>
      )}
    </nav>
  );
}

function AppInner() {
  const [authState, setAuthState] = useState({ loading: true, authenticated: false, provider: null });
  const navigate = useNavigate();

  useEffect(() => {
    api.get('/api/me').then(({ data }) => {
      setAuthState({ loading: false, ...data });
    }).catch(() => {
      setAuthState({ loading: false, authenticated: false, provider: null });
    });
  }, []);

  async function handleLogout() {
    await api.delete('/api/auth/logout');
    setAuthState({ loading: false, authenticated: false, provider: null });
    navigate('/');
  }

  if (authState.loading) {
    return <p style={{ color: '#888' }}>Loading…</p>;
  }

  return (
    <>
      <Nav provider={authState.provider} onLogout={handleLogout} />
      <Routes>
        <Route
          path="/"
          element={
            authState.authenticated
              ? <Navigate to="/entries" replace />
              : <AuthSelector />
          }
        />
        <Route
          path="/entries"
          element={
            authState.authenticated
              ? <EntryList />
              : <Navigate to="/" replace />
          }
        />
        <Route
          path="/entries/:year/:month/:filename"
          element={
            authState.authenticated
              ? <EntryView />
              : <Navigate to="/" replace />
          }
        />
        <Route
          path="/new"
          element={
            authState.authenticated
              ? <NewEntry />
              : <Navigate to="/" replace />
          }
        />
      </Routes>
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppInner />
    </BrowserRouter>
  );
}
