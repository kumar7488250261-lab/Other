// Top Navigation Bar Component with Responsive Nav & Live Sync Indicator
import { store } from '../store.js';

export function renderHeader({ title = 'Kharsia Lobby', subtitle = 'SECR Bilaspur Division', showBack = false, onBack = null } = {}) {
  const auth = store.getAuth();
  const isAdmin = auth.user?.role === 'ADMIN' || store.isAdminSessionActive();
  const isApproved = auth.isLoggedIn && auth.user?.status === 'APPROVED';

  const backBtnHtml = showBack 
    ? `<button class="btn-icon" id="topbar-back-btn" aria-label="Go Back">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="19" y1="12" x2="5" y2="12"></line>
          <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
      </button>`
    : `<img src="./icons/logo.png" alt="Kharsia Lobby Logo" width="34" height="34" class="kharsia-logo" />`;

  const syncPillHtml = `
    <span class="sync-indicator ${store.syncStatus.toLowerCase()}" id="header-sync-pill" title="Real-time Firebase Firestore Sync Status">
      <span class="dot"></span>
      <span class="text">${store.syncStatus}</span>
    </span>
  `;

  const userHtml = auth.isLoggedIn
    ? `
      <div style="display:flex; align-items:center; gap:8px;">
        <a href="#/profile" class="user-badge" style="text-decoration:none; cursor:pointer;" title="View Profile">
          ${auth.user?.crewId || 'CREW'} <span class="role-pill">${auth.user?.role || 'STAFF'}</span>
        </a>
        <button class="btn-icon" id="topbar-logout-btn" title="Logout">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#FF5252" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
            <polyline points="16 17 21 12 16 7"></polyline>
            <line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
        </button>
      </div>`
    : `<a href="#/login" class="btn btn-sm btn-primary" style="padding: 6px 14px; min-height: auto; font-size: 12px;">Login</a>`;

  const navLinksHtml = auth.isLoggedIn ? `
    <nav class="app-nav-tabs">
      <a href="#/dashboard" class="nav-tab ${window.location.hash.startsWith('#/dashboard') || window.location.hash === '#/menu' ? 'active' : ''}">Dashboard</a>
      <a href="#/duty" class="nav-tab ${window.location.hash.startsWith('#/duty') || window.location.hash === '#/longhour' ? 'active' : ''}">Live Duties</a>
      <a href="#/roster" class="nav-tab ${window.location.hash.startsWith('#/roster') ? 'active' : ''}">Roster</a>
      <a href="#/crew" class="nav-tab ${window.location.hash.startsWith('#/crew') ? 'active' : ''}">Crew Master</a>
      <a href="#/notifications" class="nav-tab ${window.location.hash.startsWith('#/notifications') ? 'active' : ''}">Notices</a>
      ${isAdmin ? `<a href="#/admin" class="nav-tab admin-tab ${window.location.hash.startsWith('#/admin') ? 'active' : ''}">Admin Panel</a>` : ''}
    </nav>
  ` : '';

  const header = document.createElement('header');
  header.className = 'app-topbar-wrapper';
  header.innerHTML = `
    <div class="app-topbar">
      <div class="topbar-left">
        ${backBtnHtml}
        <div class="topbar-title">
          <h1>${title}</h1>
          <span>${subtitle}</span>
        </div>
      </div>
      <div class="topbar-right">
        ${syncPillHtml}
        ${userHtml}
      </div>
    </div>
    ${navLinksHtml}
  `;

  // Sync indicator live update
  store.subscribeSyncStatus((status) => {
    const pill = header.querySelector('#header-sync-pill');
    if (pill) {
      pill.className = `sync-indicator ${status.toLowerCase()}`;
      const text = pill.querySelector('.text');
      if (text) text.textContent = status;
    }
  });

  if (showBack) {
    const backBtn = header.querySelector('#topbar-back-btn');
    if (backBtn) {
      backBtn.addEventListener('click', () => {
        if (onBack) onBack();
        else window.history.back();
      });
    }
  }

  const logoutBtn = header.querySelector('#topbar-logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      if (confirm('Are you sure you want to sign out?')) {
        store.logout();
        window.location.hash = '#/welcome';
      }
    });
  }

  return header;
}
