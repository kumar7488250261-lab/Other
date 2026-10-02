// Welcome Splash & Portal Landing Screen
import { store } from '../store.js';

export function renderWelcomeView() {
  const container = document.createElement('div');
  container.className = 'fade-in';
  container.style.cssText = `
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    align-items: center;
    padding: 36px 20px;
    background: #060D18;
    text-align: center;
  `;

  const config = store.getAppConfig();

  container.innerHTML = `
    <div></div>

    <div style="display: flex; flex-direction: column; align-items: center; max-width: 480px; width: 100%;">
      <!-- Official Circular Kharsia Lobby Emblem -->
      <img src="./icons/logo.png" alt="Kharsia Lobby Emblem" width="128" height="128" class="kharsia-logo" style="margin-bottom: 20px;" />

      <h2 style="font-size: 22px; font-weight: 700; color: #ffffff; margin-bottom: 4px;">Welcome to</h2>
      <h1 style="font-size: 32px; font-weight: 800; color: #F59E0B; margin-bottom: 8px;">Kharsia Lobby</h1>

      <p style="font-size: 13px; font-weight: 700; color: #94A3B8; letter-spacing: 1.5px; margin-bottom: 4px; text-transform: uppercase;">
        SOUTH EAST CENTRAL RAILWAY (SECR)
      </p>
      <p style="font-size: 12px; font-weight: 600; color: #64748B; letter-spacing: 1.2px; margin-bottom: 30px; text-transform: uppercase;">
        BILASPUR DIVISION • OPERATIONAL PORTAL
      </p>

      <!-- Action Buttons Group -->
      <div style="display: flex; flex-direction: column; gap: 12px; width: 100%; max-width: 320px;">
        <a href="#/login" class="btn btn-primary" style="padding: 14px 24px; font-size: 15px; font-weight: 700; display: inline-flex; align-items: center; justify-content: center; gap: 8px; border-radius: 8px;">
          <span>Launch Web Portal</span>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="5" y1="12" x2="19" y2="12"></line>
            <polyline points="12 5 19 12 12 19"></polyline>
          </svg>
        </a>

        <!-- Download Android App Button (Prominent) -->
        <a href="${config.latestApkUrl}" target="_blank" class="btn btn-outline" style="padding: 12px 20px; font-size: 13.5px; font-weight: 600; display: inline-flex; align-items: center; justify-content: center; gap: 8px; border-radius: 8px; color: #10B981; border-color: #10B981;">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="7 10 12 15 17 10"></polyline>
            <line x1="12" y1="15" x2="12" y2="3"></line>
          </svg>
          <span>Download Android App (v${config.latestVersion})</span>
        </a>
      </div>
    </div>

    <!-- Division Footer Label -->
    <div style="font-size: 11px; color: #64748B; letter-spacing: 1.2px; font-weight: 500;">
      KHS • BILASPUR DIVISION • SECR • INDIAN RAILWAYS
    </div>
  `;

  return container;
}
