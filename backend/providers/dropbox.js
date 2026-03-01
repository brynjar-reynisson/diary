const { Dropbox } = require('dropbox');

function getDropboxAuthUrl() {
  const dbx = new Dropbox({ clientId: process.env.DROPBOX_APP_KEY });
  // Build URL manually for code flow
  const params = new URLSearchParams({
    client_id: process.env.DROPBOX_APP_KEY,
    response_type: 'code',
    redirect_uri: process.env.DROPBOX_REDIRECT_URI,
    token_access_type: 'offline',
  });
  return `https://www.dropbox.com/oauth2/authorize?${params.toString()}`;
}

async function exchangeDropboxCode(code) {
  const params = new URLSearchParams({
    code,
    grant_type: 'authorization_code',
    redirect_uri: process.env.DROPBOX_REDIRECT_URI,
  });

  const basicAuth = Buffer.from(
    `${process.env.DROPBOX_APP_KEY}:${process.env.DROPBOX_APP_SECRET}`,
  ).toString('base64');

  const resp = await fetch('https://api.dropbox.com/oauth2/token', {
    method: 'POST',
    headers: {
      Authorization: `Basic ${basicAuth}`,
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: params.toString(),
  });

  if (!resp.ok) {
    const text = await resp.text();
    throw new Error(`Dropbox token exchange failed: ${text}`);
  }

  return resp.json(); // { access_token, refresh_token, ... }
}

function createClient(tokens) {
  const dbx = new Dropbox({ accessToken: tokens.access_token });

  const DIARY_ROOT = '/diary';

  async function listEntries() {
    const result = {};

    // List year folders under /diary
    let yearRes;
    try {
      yearRes = await dbx.filesListFolder({ path: DIARY_ROOT });
    } catch {
      return result; // diary folder doesn't exist yet
    }

    for (const yearEntry of yearRes.result.entries) {
      if (yearEntry['.tag'] !== 'folder') continue;
      const year = yearEntry.name;
      result[year] = {};

      // List month folders
      const monthRes = await dbx.filesListFolder({ path: `${DIARY_ROOT}/${year}` });
      for (const monthEntry of monthRes.result.entries) {
        if (monthEntry['.tag'] !== 'folder') continue;
        const month = monthEntry.name;
        result[year][month] = [];

        // List entry files
        const fileRes = await dbx.filesListFolder({ path: `${DIARY_ROOT}/${year}/${month}` });
        for (const file of fileRes.result.entries) {
          if (file['.tag'] !== 'file') continue;
          if (!file.name.endsWith('.txt')) continue;
          const day = file.name.replace('entry-', '').split('-')[0];
          result[year][month].push({
            day,
            filename: file.name,
            path: `diary/${year}/${month}/${file.name}`,
          });
        }
        result[year][month].sort((a, b) => a.filename.localeCompare(b.filename));
      }
    }
    return result;
  }

  async function getEntry(year, month, filename) {
    const path = `${DIARY_ROOT}/${year}/${month}/${filename}`;
    const resp = await dbx.filesDownload({ path });
    // filesDownload returns { result: { fileBinary: Buffer } } in Node
    const buf = resp.result.fileBinary;
    return buf.toString('utf-8');
  }

  async function createEntry(year, month, filename, content) {
    const path = `${DIARY_ROOT}/${year}/${month}/${filename}`;
    await dbx.filesUpload({
      path,
      contents: Buffer.from(content, 'utf-8'),
      mode: { '.tag': 'add' },
      autorename: false,
    });
    const day = filename.replace('entry-', '').split('-')[0];
    return {
      day,
      filename,
      path: `diary/${year}/${month}/${filename}`,
    };
  }

  return { listEntries, getEntry, createEntry };
}

module.exports = { getDropboxAuthUrl, exchangeDropboxCode, createClient };
