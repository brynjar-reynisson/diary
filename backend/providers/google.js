const { google } = require('googleapis');
const { Readable } = require('stream');

const SCOPES = ['https://www.googleapis.com/auth/drive.file'];

function createOAuth2Client() {
  return new google.auth.OAuth2(
    process.env.GOOGLE_CLIENT_ID,
    process.env.GOOGLE_CLIENT_SECRET,
    process.env.GOOGLE_REDIRECT_URI,
  );
}

function getGoogleAuthUrl() {
  const auth = createOAuth2Client();
  return auth.generateAuthUrl({
    access_type: 'offline',
    prompt: 'consent',
    scope: SCOPES,
  });
}

async function exchangeGoogleCode(code) {
  const auth = createOAuth2Client();
  const { tokens } = await auth.getToken(code);
  return tokens;
}

// Returns a provider object compatible with routes/entries.js
function createClient(tokens) {
  const auth = createOAuth2Client();
  auth.setCredentials(tokens);
  const drive = google.drive({ version: 'v3', auth });

  async function findOrCreateFolder(name, parentId) {
    const q = parentId
      ? `name='${name}' and mimeType='application/vnd.google-apps.folder' and '${parentId}' in parents and trashed=false`
      : `name='${name}' and mimeType='application/vnd.google-apps.folder' and 'root' in parents and trashed=false`;

    const resp = await drive.files.list({ q, fields: 'files(id, name)' });
    if (resp.data.files.length > 0) return resp.data.files[0].id;

    const created = await drive.files.create({
      requestBody: {
        name,
        mimeType: 'application/vnd.google-apps.folder',
        parents: parentId ? [parentId] : [],
      },
      fields: 'id',
    });
    return created.data.id;
  }

  async function listEntries() {
    // Find diary root folder
    const diaryResp = await drive.files.list({
      q: `name='diary' and mimeType='application/vnd.google-apps.folder' and 'root' in parents and trashed=false`,
      fields: 'files(id)',
    });

    if (diaryResp.data.files.length === 0) return {};
    const diaryId = diaryResp.data.files[0].id;

    // List year folders
    const yearResp = await drive.files.list({
      q: `mimeType='application/vnd.google-apps.folder' and '${diaryId}' in parents and trashed=false`,
      fields: 'files(id, name)',
    });

    const result = {};
    for (const yearFolder of yearResp.data.files) {
      const year = yearFolder.name;
      result[year] = {};

      // List month folders
      const monthResp = await drive.files.list({
        q: `mimeType='application/vnd.google-apps.folder' and '${yearFolder.id}' in parents and trashed=false`,
        fields: 'files(id, name)',
      });

      for (const monthFolder of monthResp.data.files) {
        const month = monthFolder.name;
        result[year][month] = [];

        // List .txt files
        const fileResp = await drive.files.list({
          q: `mimeType='text/plain' and '${monthFolder.id}' in parents and trashed=false`,
          fields: 'files(id, name)',
          orderBy: 'name',
        });

        for (const file of fileResp.data.files) {
          const day = file.name.replace('entry-', '').split('-')[0];
          result[year][month].push({
            day,
            filename: file.name,
            path: `diary/${year}/${month}/${file.name}`,
          });
        }
      }
    }
    return result;
  }

  async function getEntry(year, month, filename) {
    const diaryResp = await drive.files.list({
      q: `name='diary' and mimeType='application/vnd.google-apps.folder' and 'root' in parents and trashed=false`,
      fields: 'files(id)',
    });
    if (diaryResp.data.files.length === 0) throw new Error('No diary folder');
    const diaryId = diaryResp.data.files[0].id;

    const yearResp = await drive.files.list({
      q: `name='${year}' and mimeType='application/vnd.google-apps.folder' and '${diaryId}' in parents and trashed=false`,
      fields: 'files(id)',
    });
    if (yearResp.data.files.length === 0) throw new Error('Year folder not found');
    const yearId = yearResp.data.files[0].id;

    const monthResp = await drive.files.list({
      q: `name='${month}' and mimeType='application/vnd.google-apps.folder' and '${yearId}' in parents and trashed=false`,
      fields: 'files(id)',
    });
    if (monthResp.data.files.length === 0) throw new Error('Month folder not found');
    const monthId = monthResp.data.files[0].id;

    const fileResp = await drive.files.list({
      q: `name='${filename}' and '${monthId}' in parents and trashed=false`,
      fields: 'files(id)',
    });
    if (fileResp.data.files.length === 0) throw new Error('File not found');
    const fileId = fileResp.data.files[0].id;

    const content = await drive.files.get(
      { fileId, alt: 'media' },
      { responseType: 'text' },
    );
    return content.data;
  }

  async function createEntry(year, month, filename, content) {
    const diaryId = await findOrCreateFolder('diary', null);
    const yearId = await findOrCreateFolder(year, diaryId);
    const monthId = await findOrCreateFolder(month, yearId);

    const stream = Readable.from([content]);

    await drive.files.create({
      requestBody: {
        name: filename,
        mimeType: 'text/plain',
        parents: [monthId],
      },
      media: {
        mimeType: 'text/plain',
        body: stream,
      },
      fields: 'id',
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

module.exports = { getGoogleAuthUrl, exchangeGoogleCode, createClient };
