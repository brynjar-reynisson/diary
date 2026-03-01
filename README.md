# Diary App

A personal diary application that stores entries in cloud storage (Google Drive or Dropbox). Write and read your diary from a web browser or Android device.

## File Format

Entries are stored as plain `.txt` files using this path convention:

```
<root>/diary/<year>/<month>/entry-<DD>-<HH:MM:SS>.txt
```

Example: `diary/2024/01/entry-15-09:30:00.txt`

## Supported Providers

- **Google Drive** — OAuth 2.0 via Google Sign-In
- **Dropbox** — OAuth 2.0 via Dropbox SDK

## Components

| Component | Tech | Description |
|-----------|------|-------------|
| `backend/` | Node.js + Express | REST API, OAuth handling, Drive/Dropbox proxying |
| `frontend/` | React + Vite | Web UI for writing and reading entries |
| `android/` | Kotlin + Jetpack Compose | Native Android app using provider SDKs directly |

---

## Backend Setup

### Prerequisites
- Node.js 18+
- A Google Cloud project with Drive API enabled
- A Dropbox app

### Google OAuth Setup
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a project and enable **Google Drive API**
3. Create OAuth 2.0 credentials (Web application)
4. Set Authorized redirect URI: `http://localhost:3001/api/auth/google/callback`
5. Copy Client ID and Client Secret

### Dropbox OAuth Setup
1. Go to [Dropbox App Console](https://www.dropbox.com/developers/apps)
2. Create an app with **Full Dropbox** or **App Folder** access
3. Add redirect URI: `http://localhost:3001/api/auth/dropbox/callback`
4. Copy App Key and App Secret

### Configuration

```bash
cd backend
cp .env.example .env
# Edit .env with your credentials
```

`.env` fields:
```
SESSION_SECRET=<random secret>
GOOGLE_CLIENT_ID=<your google client id>
GOOGLE_CLIENT_SECRET=<your google client secret>
GOOGLE_REDIRECT_URI=http://localhost:3001/api/auth/google/callback
DROPBOX_APP_KEY=<your dropbox app key>
DROPBOX_APP_SECRET=<your dropbox app secret>
DROPBOX_REDIRECT_URI=http://localhost:3001/api/auth/dropbox/callback
```

### Run

```bash
cd backend
npm install
npm start
# Server runs on http://localhost:3001
```

---

## Frontend Setup

### Prerequisites
- Node.js 18+
- Backend running on port 3001

### Configuration

```bash
cd frontend
cp .env.example .env
# Default: VITE_API_URL=http://localhost:3001
```

### Run

```bash
cd frontend
npm install
npm run dev
# Dev server on http://localhost:5173
```

---

## Android App Setup

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 26+
- A Google Cloud project with Drive API + Android OAuth client
- A Dropbox app with deep link redirect URI

### Google Sign-In Setup
1. In Google Cloud Console, add an **Android** OAuth client
2. Use your app's package name: `com.diary`
3. Provide your debug keystore SHA-1:
   ```bash
   keytool -exportcert -keystore ~/.android/debug.keystore -list -v
   ```
4. Download `google-services.json` and place it at `android/app/google-services.json`

### Dropbox Deep Link Setup
1. In Dropbox App Console, add redirect URI: `diaryapp://dropbox-auth`
2. The Android app handles this via an intent filter in `AndroidManifest.xml`

### Build & Run

1. Open the `android/` directory in Android Studio
2. Wait for Gradle sync to complete
3. Select a device or emulator (API 26+)
4. Click **Run**

---

## API Reference

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/me` | Auth status and active provider |
| GET | `/api/auth/google` | Start Google OAuth flow |
| GET | `/api/auth/google/callback` | Google OAuth callback |
| GET | `/api/auth/dropbox` | Start Dropbox OAuth flow |
| GET | `/api/auth/dropbox/callback` | Dropbox OAuth callback |
| DELETE | `/api/auth/logout` | Clear session |
| GET | `/api/entries` | List all entries grouped by year/month |
| GET | `/api/entries/:year/:month/:filename` | Get entry text content |
| POST | `/api/entries` | Create new entry (`{ content }`) |

---

## License

MIT
