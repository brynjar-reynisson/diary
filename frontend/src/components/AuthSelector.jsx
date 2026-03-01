import React from 'react';

const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: '1.5rem',
    marginTop: '4rem',
  },
  title: { fontSize: '1.8rem', color: '#444', fontStyle: 'italic' },
  subtitle: { color: '#888', fontSize: '0.95rem' },
  btnGoogle: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
    padding: '0.8rem 2rem',
    fontSize: '1rem',
    border: '1px solid #ddd',
    borderRadius: '6px',
    cursor: 'pointer',
    background: '#fff',
    color: '#333',
    width: '260px',
    justifyContent: 'center',
    boxShadow: '0 1px 3px rgba(0,0,0,.1)',
  },
  btnDropbox: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
    padding: '0.8rem 2rem',
    fontSize: '1rem',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    background: '#0061fe',
    color: '#fff',
    width: '260px',
    justifyContent: 'center',
    boxShadow: '0 1px 3px rgba(0,0,0,.2)',
  },
};

export default function AuthSelector() {
  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Welcome to your Diary</h2>
      <p style={styles.subtitle}>Choose where to store your entries:</p>
      <a href="/api/auth/google" style={{ textDecoration: 'none' }}>
        <button style={styles.btnGoogle}>
          <svg width="18" height="18" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
            <g fill="none" fillRule="evenodd">
              <path d="M17.64 9.205c0-.639-.057-1.252-.164-1.841H9v3.481h4.844a4.14 4.14 0 01-1.796 2.716v2.259h2.908c1.702-1.567 2.684-3.875 2.684-6.615z" fill="#4285F4"/>
              <path d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 009 18z" fill="#34A853"/>
              <path d="M3.964 10.71A5.41 5.41 0 013.682 9c0-.593.102-1.17.282-1.71V4.958H.957A8.996 8.996 0 000 9c0 1.452.348 2.827.957 4.042l3.007-2.332z" fill="#FBBC05"/>
              <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 00.957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z" fill="#EA4335"/>
            </g>
          </svg>
          Connect Google Drive
        </button>
      </a>
      <a href="/api/auth/dropbox" style={{ textDecoration: 'none' }}>
        <button style={styles.btnDropbox}>
          <svg width="18" height="18" viewBox="0 0 528 528" xmlns="http://www.w3.org/2000/svg" fill="#fff">
            <path d="M162 0L0 105 162 210 324 105zM486 0L324 105l162 105 162-105zM0 315l162 105 162-105-162-105zM486 210L324 315l162 105 162-105zM162 444l162 84 162-84-162-105z"/>
          </svg>
          Connect Dropbox
        </button>
      </a>
    </div>
  );
}
