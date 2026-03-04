# Diary App — Claude Code Guide

## Project Overview

A personal diary app that stores entries as plain `.txt` files in the user's cloud storage.
Three independent components share this repository:

| Component | Stack | Entry point |
|-----------|-------|-------------|
| `backend/` | Node.js + Express | `backend/server.js` |
| `frontend/` | React + Vite | `frontend/src/index.jsx` |
| `android/` | Kotlin + Jetpack Compose | `android/app/src/main/java/com/diary/` |

The Android app talks directly to Google Drive and Dropbox — it does **not** use the backend.
The backend is only used by the web frontend.

---

## Diary File Convention

```
diary/<year>/<month>/entry-<DD>-<HH:MM:SS>.txt
```

Example: `diary/2026/03/entry-04-09:30:00.txt`

---

## Running the Web App

The backend serves the built React frontend as static files. There is no separate frontend server in normal use.

**Start (dev):**
```bash
cd backend && npm start
# Loads .env.development, runs on http://localhost:3001
```

**Rebuild frontend** (required after frontend code changes):
```bash
cd frontend && npm run build
# Output goes to frontend/dist/ which the backend serves
```

**Restart server:**
```bash
pkill -f "node server.js"
cd backend && npm start
```

**Other environments:**
```bash
npm run start:cert   # loads .env.cert
npm run start:prod   # loads .env.production
```

---

## Environment Files

Each environment has its own `.env.*` file in `backend/`. These are gitignored.
Templates (committed) live alongside them as `.env.*.example`.

| File | Used by |
|------|---------|
| `backend/.env.development` | `npm start` / `npm run dev` |
| `backend/.env.cert` | `npm run start:cert` |
| `backend/.env.production` | `npm run start:prod` |

Key variable: `APP_URL` — drives CORS origin, session cookie security, and post-OAuth redirects.
`GOOGLE_REDIRECT_URI` and `DROPBOX_REDIRECT_URI` must be registered in the respective OAuth consoles and must match the values in `.env.*`.

---

## Repository Rules

- **Never commit** `.env.*` files (real ones are gitignored; only `.env.*.example` files are committed)
- **Never commit** `backend/certs/` or `*.pem` files
- **Never commit** `android/app/google-services.json` (gitignored; contains OAuth credentials)
- `frontend/dist/` and `node_modules/` are gitignored

---

## Android

**Open in Android Studio:** File → Open → select the `android/` folder (not repo root).

**Credentials required before building:**
- `android/app/google-services.json` — download from Firebase Console (project: `micro-rigging-118017`)
- Dropbox app key/secret — currently hardcoded as placeholders in `ProviderScreen.kt`; replace before building

**Debug keystore:** `~/.android/debug.keystore`
SHA-1: `A6:16:D6:FC:D2:AE:91:C4:6D:26:9D:7C:FC:E0:0D:B0:F5:15:98:67`
This SHA-1 is registered in Google Cloud Console for package `com.diary`.

**Min Android version:** API 26 (Android 8.0)

---

## Common Tasks

**Commit and push:**
Always check `git status` first to avoid committing secrets or build artifacts.

**Add a new API endpoint:**
1. Add route in `backend/routes/entries.js` or a new route file
2. Mount it in `backend/server.js`
3. Add corresponding call in `frontend/src/api.js`

**Add a new screen (Android):**
1. Create screen file in `android/app/src/main/java/com/diary/ui/screen/`
2. Add route in `DiaryNavHost.kt`
