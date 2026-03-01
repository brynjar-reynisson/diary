const express = require('express');
const router = express.Router();
const google = require('../providers/google');
const dropbox = require('../providers/dropbox');

function requireAuth(req, res, next) {
  if (!req.session.provider) {
    return res.status(401).json({ error: 'Not authenticated' });
  }
  next();
}

function getProvider(req) {
  if (req.session.provider === 'google') {
    return google.createClient(req.session.tokens);
  }
  if (req.session.provider === 'dropbox') {
    return dropbox.createClient(req.session.tokens);
  }
  throw new Error('Unknown provider');
}

// GET /api/entries — list all entries grouped by year/month
router.get('/', requireAuth, async (req, res) => {
  try {
    const provider = getProvider(req);
    const entries = await provider.listEntries();
    res.json(entries);
  } catch (err) {
    console.error('listEntries error:', err);
    res.status(500).json({ error: err.message });
  }
});

// GET /api/entries/:year/:month/:filename — get entry content
router.get('/:year/:month/:filename', requireAuth, async (req, res) => {
  try {
    const provider = getProvider(req);
    const { year, month, filename } = req.params;
    const content = await provider.getEntry(year, month, filename);
    res.type('text/plain').send(content);
  } catch (err) {
    console.error('getEntry error:', err);
    res.status(500).json({ error: err.message });
  }
});

// POST /api/entries — create new entry
router.post('/', requireAuth, async (req, res) => {
  try {
    const provider = getProvider(req);
    const { content } = req.body;
    if (typeof content !== 'string') {
      return res.status(400).json({ error: 'content must be a string' });
    }
    const now = new Date();
    const year = String(now.getFullYear());
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const ss = String(now.getSeconds()).padStart(2, '0');
    const filename = `entry-${day}-${hh}:${mm}:${ss}.txt`;

    const entry = await provider.createEntry(year, month, filename, content);
    res.status(201).json(entry);
  } catch (err) {
    console.error('createEntry error:', err);
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
