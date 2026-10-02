# Kharsia Lobby (संयुक्त चालक एवं परिचालक लॉबी खरसिया)

**South East Central Railway (SECR) • Bilaspur Division**  
*Combined Crew Management & Operational Portal*

---

## 📌 Overview

**Kharsia Lobby** is an integrated railway operational management system developed for the Bilaspur Division, South East Central Railway (SECR). The repository contains two synchronized client applications sharing the identical design system, dataset, terminology, and operational workflow:

1. **Native Android Application** (`/app`): Built with modern Kotlin, Jetpack Compose, and Material Design 3.
2. **Responsive Web Application & PWA** (`/web`): Built with Vite, modern Vanilla JavaScript, CSS custom properties, and service worker caching for seamless cross-platform browser support on Mobile (Android Chrome, iOS Safari), Tablets, and Desktop computers.

Both versions require **zero paid hosting** and are fully automated via **GitHub Actions**.

---

## 🚆 Operational Modules

Both the Android app and the Web version provide complete implementations of:

| Feature / Screen | Description & Functionality |
| :--- | :--- |
| **Splash & Landing** | High-contrast SECR emblem branding, bilingual Hindi/English headers, and official lobby imagery. |
| **Authentication** | Secure crew and staff login with username/crew ID and password. |
| **Staff Directory** | Complete Call Book with contact details across **all 14 division lobbies** (Kharsia, Bilaspur, Raigarh, Champa, Korba, Raipur, etc.), live search, lobby filtering, and direct click-to-call. |
| **PR Remark** | Periodical Rest (PR) management: crew sign-off time capture, auto-fetching staff details from the Crew Master database, request status tracking, and Lobby In-Charge PIN authorization (`1234`). |
| **Store Register** | Equipment Fast Issue & Fast Return management (Walkie-Talkies, Torches, Breathalyzers, PUGs, Tool Kits, Detonators) with crew ID auto-fetch, train details, and supervisor logs. |
| **Long Hour Update** | Real-time train duty duration tracker with color-coded warning levels (Normal, 9h+ Long Hour, 11h+ Urgent Relief), 7-position selector, and station relief entry. |
| **Roaster & TLC Update** | Shift-wise duty roster (06–14, 14–22, 22–06) capturing TFR, LH, DI, WD crews, CMS operator, Lobby CLI, Sander boy, and Bilaspur TLC ML/LH controllers. |
| **Jeep Movement** | First-In-First-Out (FIFO) availability tracking for lobby vehicles (89, 89-II, 91, 22, 31, 79, Breakdown), outward and return journeys, and 7-slot crew assignment. |

---

## 📱 Native Android App (`/app`)

### Build Prerequisites
- Android Studio Ladybug / Meerkat or newer
- JDK 17+
- Android SDK 35

### Running & Building Debug APK
```bash
# Clean and assemble debug APK
gradle :app:assembleDebug

# Run unit tests
gradle :app:testDebugUnitTest
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🌐 Web Application (`/web`)

The web version is located in the `/web` subdirectory. It is designed as a standalone, lightweight Progressive Web App (PWA).

### Directory Structure
```text
web/
├── public/
│   ├── data/                 # Static JSON datasets (kharsia_directory, kharsia_crew_master)
│   ├── icons/                # PWA icons (favicon, 192x192, 512x512, logo.png)
│   ├── images/               # Building backdrop assets
│   ├── manifest.json         # PWA Manifest configuration
│   └── service-worker.js     # Offline caching service worker
├── src/
│   ├── css/
│   │   └── styles.css        # Material 3 Railway dark design system
│   └── js/
│       ├── app.js            # Hash router & application bootstrap
│       ├── store.js          # Central state store (mirrors Android Room & SharedPreferences)
│       ├── components/       # Reusable components (Header, Toast, AdminPinModal)
│       └── views/            # Screen views (Welcome, Landing, Login, Menu, Modules)
├── index.html                # Web entry point
├── package.json              # NPM dependencies & build scripts
└── vite.config.js            # GitHub Pages deployment configuration (base: './')
```

### Local Web Development
To run the web app locally on your machine:
```bash
# Navigate to the web directory
cd web

# Install dependencies (only required once)
npm install

# Start local development server
npm run dev
```
Open your browser at `http://localhost:3000` (or the URL displayed in your terminal).

### Production Build
```bash
cd web
npm run build
```
The optimized production files will be output to `web/dist/`.

---

## 🚀 GitHub Actions CI/CD & Automated Companion Website

The repository includes automated GitHub Actions workflows:

### Automated APK Build, Release & Companion Web Portal (`.github/workflows/release-and-deploy.yml`)
- **Automated Trigger:** Runs on every push to the `main` branch or manual `workflow_dispatch`.
- **Builds APK:** Automatically sets up JDK 17, Android SDK, and builds the Android debug APK using `./gradlew :app:assembleDebug`.
- **GitHub Release:** Generates/updates a GitHub Release tagged with the app's version and attaches the `.apk` file for direct phone downloads.
- **Syncs Datasets:** Automatically copies JSON datasets (`app/src/main/assets/*.json`) into `docs/data/` and updates `docs/data/version.json`.
- **Deploys Companion Website:** Publishes the `docs/` companion portal directly to **GitHub Pages**.

### 🌐 Live URLs
* **GitHub Releases (APK Downloads):**  
  [https://github.com/abhishekused/Newapk/releases](https://github.com/abhishekused/Newapk/releases)
* **GitHub Pages Companion Portal:**  
  [https://abhishekused.github.io/Newapk/](https://abhishekused.github.io/Newapk/)

#### ⚙️ Enabling GitHub Pages in Your Repository (One-time Setup)
GitHub requires Pages to be enabled once manually in your repository settings:
1. Open your repository on GitHub: `https://github.com/abhishekused/Newapk`
2. Go to **Settings** → **Pages** (in the left sidebar under *Code and automation*).
3. Under **Build and deployment** → **Source**, select **GitHub Actions**.
4. That's it! Future pushes to `main` will automatically build, release, and deploy the website.

---

## 🔐 Security & Administration
- **Lobby In-Charge PIN:** Access to administrative actions (PR status review, Roaster editing, Store log oversight, Long Hour relief confirmation) is secured with a 4-digit In-Charge PIN.
  - **Default PIN:** `1234`
- **Data Privacy:** Local storage is sandboxed within the browser (`localStorage`). No private API keys or personal credentials are exposed in client-side code.

<!-- Build Sync: 2026-09-29 -->

