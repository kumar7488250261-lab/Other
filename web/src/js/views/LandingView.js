// Step 2: Lobby Landing Screen
export function renderLandingView() {
  const container = document.createElement('div');
  container.className = 'fade-in';
  container.style.cssText = `
    min-height: 100vh;
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  `;

  container.innerHTML = `
    <!-- Real Kharsia Lobby Building Background Backdrop -->
    <div class="building-backdrop">
      <div class="building-overlay"></div>
    </div>

    <div class="backdrop-content" style="display: flex; flex-direction: column; justify-content: space-between; min-height: 100vh; padding: 24px 20px;">
      <!-- Top Section: Emblem + Division Capsule Tag -->
      <div style="display: flex; flex-direction: column; align-items: center; padding-top: 16px;">
        <img src="./icons/logo.png" alt="Kharsia Lobby Emblem" width="80" height="80" class="kharsia-logo" style="margin-bottom: 12px;" />
        <div style="background: rgba(12, 36, 59, 0.94); border: 1px solid #1E4D7A; border-radius: var(--radius-pill); padding: 6px 16px;">
          <span style="color: #64B5F6; font-size: 11px; font-weight: 800; letter-spacing: 1.2px;">INDIAN RAILWAYS • SECR BILASPUR</span>
        </div>
      </div>

      <!-- Central Hindi/English Card -->
      <div class="card card-gold-border" style="max-width: 480px; width: 100%; margin: 24px auto; text-align: center; padding: 28px 24px;">
        <p style="color: #F1B748; font-size: 17px; font-weight: 700; margin-bottom: 4px;">
          संयुक्त चालक एवं परिचालक लॉबी
        </p>
        <h1 style="color: #FFFFFF; font-size: 32px; font-weight: 800; margin-bottom: 4px; letter-spacing: 0.5px;">
          खरसिया
        </h1>
        <p style="color: #64B5F6; font-size: 14px; font-weight: 700; letter-spacing: 0.8px; margin-bottom: 2px;">
          COMBINED CREW LOBBY KHARSIA
        </p>
        <p style="color: #90A4AE; font-size: 12px; font-weight: 600; letter-spacing: 0.5px; margin-bottom: 16px;">
          SECR BILASPUR DIVISION
        </p>

        <p style="color: #B0BEC5; font-size: 13px; line-height: 1.4; margin-bottom: 24px;">
          Operational call book, store equipment register, periodical rest & crew duty monitoring.
        </p>

        <div style="display: flex; flex-direction: column; gap: 12px;">
          <!-- Primary Blue Login Button -->
          <a href="#/login" class="btn btn-primary btn-full" style="font-size: 16px; letter-spacing: 0.5px;">
            <span>LOGIN</span>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="5" y1="12" x2="19" y2="12"></line>
              <polyline points="12 5 19 12 12 19"></polyline>
            </svg>
          </a>

          <!-- Guest Staff Directory Access -->
          <a href="#/directory" class="btn btn-secondary btn-full">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
            <span>Guest Staff Directory</span>
          </a>
        </div>
      </div>

      <!-- Bottom Railway Footer -->
      <div style="text-align: center; font-size: 11px; color: #546E7A; letter-spacing: 1.2px; font-weight: 600; padding-bottom: 8px;">
        SEC RAILWAY • KHS LOBBY • GOVT OF INDIA
      </div>
    </div>
  `;

  return container;
}
