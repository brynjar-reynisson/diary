const path = require('path');
const NODE_ENV = process.env.NODE_ENV || 'development';
require('dotenv').config({ path: path.join(__dirname, `.env.${NODE_ENV}`) });
const fs = require('fs');
const http = require('http');
const express = require('express');
const session = require('express-session');
const cors = require('cors');

const authRoutes = require('./routes/auth');
const entriesRoutes = require('./routes/entries');

const app = express();
const PORT = process.env.PORT || 3001;
const APP_URL = process.env.APP_URL || `http://localhost:${PORT}`;

// ── Middleware ────────────────────────────────────────────────────────────────
app.use(cors({
  origin: APP_URL,
  credentials: true,
}));

app.use(express.json());

app.use(session({
  secret: process.env.SESSION_SECRET || 'dev-secret-change-me',
  resave: false,
  saveUninitialized: false,
  cookie: {
    secure: APP_URL.startsWith('https'),
    httpOnly: true,
    maxAge: 7 * 24 * 60 * 60 * 1000, // 7 days
  },
}));

// ── API routes ────────────────────────────────────────────────────────────────
app.get('/api/me', (req, res) => {
  if (req.session.provider) {
    res.json({ authenticated: true, provider: req.session.provider });
  } else {
    res.json({ authenticated: false });
  }
});

app.use('/api/auth', authRoutes);
app.use('/api/entries', entriesRoutes);

// ── Serve built React frontend ────────────────────────────────────────────────
const FRONTEND_DIST = path.join(__dirname, '..', 'frontend', 'dist');
if (fs.existsSync(FRONTEND_DIST)) {
  app.use(express.static(FRONTEND_DIST));
  // SPA fallback — let React Router handle all non-API routes
  app.get('*', (req, res) => {
    res.sendFile(path.join(FRONTEND_DIST, 'index.html'));
  });
} else {
  app.get('/', (req, res) => res.json({ status: 'API running. Build the frontend first.' }));
}

// ── Start HTTP server ─────────────────────────────────────────────────────────
http.createServer(app).listen(PORT, () => {
  console.log(`Diary app [${NODE_ENV}] running at ${APP_URL}`);
});
