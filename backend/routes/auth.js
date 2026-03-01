const express = require('express');
const router = express.Router();
const { getGoogleAuthUrl, exchangeGoogleCode } = require('../providers/google');
const { getDropboxAuthUrl, exchangeDropboxCode } = require('../providers/dropbox');

// ── Google ──────────────────────────────────────────────────────────────────

router.get('/google', (req, res) => {
  const url = getGoogleAuthUrl();
  res.redirect(url);
});

router.get('/google/callback', async (req, res) => {
  const { code, error } = req.query;
  if (error || !code) {
    return res.redirect(`${process.env.APP_URL || 'http://localhost:3001'}/?error=google_denied`);
  }
  try {
    const tokens = await exchangeGoogleCode(code);
    req.session.provider = 'google';
    req.session.tokens = tokens;
    res.redirect(`${process.env.APP_URL || 'http://localhost:3001'}/entries`);
  } catch (err) {
    console.error('Google callback error:', err);
    res.redirect(`${process.env.APP_URL || 'http://localhost:3001'}/?error=google_failed`);
  }
});

// ── Dropbox ─────────────────────────────────────────────────────────────────

router.get('/dropbox', (req, res) => {
  const url = getDropboxAuthUrl();
  res.redirect(url);
});

router.get('/dropbox/callback', async (req, res) => {
  const { code, error } = req.query;
  if (error || !code) {
    return res.redirect(`${process.env.APP_URL || 'http://localhost:3001'}/?error=dropbox_denied`);
  }
  try {
    const tokens = await exchangeDropboxCode(code);
    req.session.provider = 'dropbox';
    req.session.tokens = tokens;
    res.redirect(`${process.env.APP_URL || 'http://localhost:3001'}/entries`);
  } catch (err) {
    console.error('Dropbox callback error:', err);
    res.redirect(`${process.env.APP_URL || 'http://localhost:3001'}/?error=dropbox_failed`);
  }
});

// ── Logout ───────────────────────────────────────────────────────────────────

router.delete('/logout', (req, res) => {
  req.session.destroy(() => {
    res.json({ ok: true });
  });
});

module.exports = router;
