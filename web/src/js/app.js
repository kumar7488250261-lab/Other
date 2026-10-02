// Kharsia Lobby - Main Application Entry & Hash Router
// Common Multi-Client Platform with Shared Firebase Backend
import { store } from './store.js';
import { renderWelcomeView } from './views/WelcomeView.js';
import { renderLoginView } from './views/LoginView.js';
import { renderDashboardView } from './views/DashboardView.js';
import { renderDutyView } from './views/DutyView.js';
import { renderRosterView } from './views/RosterView.js';
import { renderCrewView } from './views/CrewView.js';
import { renderNotificationsView } from './views/NotificationsView.js';
import { renderProfileView } from './views/ProfileView.js';
import { renderAdminPanelView } from './views/AdminPanelView.js';
import { renderPrRemarkView } from './views/PrRemarkView.js';
import { renderStoreRegisterView } from './views/StoreRegisterView.js';
import { renderJeepMovementView } from './views/JeepMovementView.js';

const routes = {
  '#/welcome': renderWelcomeView,
  '#/landing': renderWelcomeView,
  '#/login': renderLoginView,
  '#/dashboard': renderDashboardView,
  '#/menu': renderDashboardView,
  '#/duty': renderDutyView,
  '#/longhour': renderDutyView,
  '#/roster': renderRosterView,
  '#/crew': renderCrewView,
  '#/notifications': renderNotificationsView,
  '#/profile': renderProfileView,
  '#/admin': renderAdminPanelView,
  '#/pr': renderPrRemarkView,
  '#/store': renderStoreRegisterView,
  '#/jeep': renderJeepMovementView
};

async function router() {
  const appContainer = document.getElementById('app');
  if (!appContainer) return;

  // Initialize store if not already initialized
  await store.init();

  const rawHash = window.location.hash || '#/welcome';
  const cleanHash = rawHash.split('?')[0];

  // Route matching
  const viewFn = routes[cleanHash];

  appContainer.innerHTML = '';
  window.scrollTo(0, 0);

  if (viewFn) {
    const viewEl = viewFn();
    appContainer.appendChild(viewEl);
  } else {
    // Default fallback
    const auth = store.getAuth();
    if (auth.isLoggedIn) {
      window.location.hash = '#/dashboard';
    } else {
      window.location.hash = '#/welcome';
    }
  }
}

// Service Worker Registration for Offline PWA
function registerServiceWorker() {
  if ('serviceWorker' in navigator && window.location.protocol.startsWith('http')) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('./service-worker.js')
        .then((reg) => {
          console.log('Kharsia Lobby PWA ServiceWorker active:', reg.scope);
        })
        .catch((err) => {
          console.warn('PWA ServiceWorker registration notice:', err);
        });
    });
  }
}

// Global bootstrap
window.addEventListener('DOMContentLoaded', async () => {
  await store.init();
  router();
  registerServiceWorker();
});

window.addEventListener('hashchange', router);
