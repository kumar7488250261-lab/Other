// Dashboard View - Kharsia Lobby Operational Control & Real-time Overview
import { store } from '../store.js';
import { renderHeader } from '../components/Header.js';

export function renderDashboardView() {
  const container = document.createElement('div');
  container.className = 'fade-in';

  const header = renderHeader({
    title: 'Kharsia Lobby',
    subtitle: 'Operational Dashboard • SECR Bilaspur',
    showBack: false
  });
  container.appendChild(header);

  const content = document.createElement('div');
  content.className = 'app-content';

  function refresh() {
    const auth = store.getAuth();
    const duties = store.getDuties();
    const users = store.getUsers();
    const roster = store.getRoster();
    const notifs = store.getNotifications();
    const config = store.getAppConfig();

    const activeDuties = duties.filter(d => d.status !== 'RELIEVED' && d.status !== 'COMPLETED');
    const awaitingRelief = duties.filter(d => d.reliefStatus === 'AWAITING_RELIEF' || d.status === 'LONG_HOUR');
    const pendingUsers = users.filter(u => u.status === 'PENDING');
    
    let longHourCount = 0;
    activeDuties.forEach(d => {
      const dur = store.calculateDutyDuration(d.signOnDate, d.signOnTime);
      if (dur.isLongHour) longHourCount++;
    });

    content.innerHTML = `
      <!-- Welcome & Status Banner -->
      <div class="card" style="margin-bottom: 20px; background: linear-gradient(135deg, rgba(17, 26, 41, 0.95), rgba(14, 30, 56, 0.95)); border-color: #2D466E;">
        <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 14px;">
          <div style="display: flex; align-items: center; gap: 14px;">
            <img src="./icons/logo.png" alt="Kharsia Lobby Logo" width="56" height="56" class="kharsia-logo" />
            <div>
              <h2 style="font-size: 20px; font-weight: 800; color: #FFFFFF; line-height: 1.2;">
                संयुक्त चालक एवं परिचालक लॉबी • खरसिया
              </h2>
              <p style="font-size: 12.5px; color: #F59E0B; font-weight: 700; letter-spacing: 0.5px; margin-top: 2px;">
                SECR BILASPUR DIVISION • REAL-TIME SYNC
              </p>
            </div>
          </div>
          <div style="display: flex; gap: 8px; align-items: center;">
            <span class="tag-pill" style="font-size: 11px;">User: ${auth.user?.crewId || 'STAFF'}</span>
            <span class="role-pill">${auth.user?.role || 'STAFF'}</span>
          </div>
        </div>
      </div>

      <!-- Live KPI Statistics Grid -->
      <div class="stats-grid" style="margin-bottom: 20px;">
        <div class="stat-box" style="border-top: 3px solid #38BDF8;">
          <span class="num" style="color: #38BDF8;">${activeDuties.length}</span>
          <span class="label">Active Duties</span>
        </div>
        <div class="stat-box" style="border-top: 3px solid #F59E0B;">
          <span class="num" style="color: #F59E0B;">${longHourCount}</span>
          <span class="label">Long Hours (&gt;9h)</span>
        </div>
        <div class="stat-box" style="border-top: 3px solid #EF4444;">
          <span class="num" style="color: #EF4444;">${awaitingRelief.length}</span>
          <span class="label">Awaiting Relief</span>
        </div>
        <div class="stat-box" style="border-top: 3px solid #10B981;">
          <span class="num" style="color: #10B981;">${pendingUsers.length}</span>
          <span class="label">Pending Approvals</span>
        </div>
      </div>

      <!-- Android App Release Banner -->
      <div class="card" style="margin-bottom: 20px; background: rgba(16, 185, 129, 0.08); border-color: rgba(16, 185, 129, 0.35);">
        <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 14px;">
          <div style="display: flex; align-items: center; gap: 12px;">
            <span style="font-size: 32px;">📱</span>
            <div>
              <h3 style="font-size: 16px; font-weight: 700; color: #FFFFFF;">
                Kharsia Lobby Android App (v${config.latestVersion})
              </h3>
              <p style="font-size: 12px; color: #94A3B8;">
                Native Android app with offline persistence and instant push notifications.
              </p>
            </div>
          </div>
          <div style="display: flex; gap: 10px;">
            <a href="${config.latestApkUrl}" target="_blank" class="btn btn-sm btn-primary" style="background: #10B981; border: none; text-decoration: none; padding: 8px 16px;">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="margin-right: 6px; vertical-align: text-bottom;">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="7 10 12 15 17 10"></polyline>
                <line x1="12" y1="15" x2="12" y2="3"></line>
              </svg>
              Download Latest APK
            </a>
          </div>
        </div>
      </div>

      <!-- Quick Action Modules Grid -->
      <h3 style="font-size: 15px; font-weight: 700; color: #F59E0B; margin-bottom: 12px; text-transform: uppercase; letter-spacing: 0.05em;">
        Operational Modules
      </h3>
      <div class="menu-grid">
        <!-- 1. Live Duty / Long Hour Tracker -->
        <a href="#/duty" class="menu-card">
          <div class="menu-icon-wrap" style="background: rgba(245, 158, 11, 0.15); color: #F59E0B;">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
          </div>
          <div class="menu-info">
            <h3>Live Duties &amp; Long-Hours</h3>
            <p>Track Sign-on, charge taking, GDR status, and relief requests</p>
          </div>
          <span class="menu-badge badge-active">${activeDuties.length} ACTIVE &gt;</span>
        </a>

        <!-- 2. Shift Roster -->
        <a href="#/roster" class="menu-card">
          <div class="menu-icon-wrap" style="background: rgba(56, 189, 248, 0.15); color: #38BDF8;">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
          </div>
          <div class="menu-info">
            <h3>Shift Wise Roster</h3>
            <p>06-14, 14-22, 22-06 duties, CMS operator, and Lobby CLI</p>
          </div>
          <span class="menu-badge badge-active">ROSTER &gt;</span>
        </a>

        <!-- 3. Kharsia Running Crew Master -->
        <a href="#/crew" class="menu-card">
          <div class="menu-icon-wrap" style="background: rgba(16, 185, 129, 0.15); color: #10B981;">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
          </div>
          <div class="menu-info">
            <h3>Crew Master Directory</h3>
            <p>Kharsia Goods LP, ALP, and Train Manager cadre list</p>
          </div>
          <span class="menu-badge badge-active">${store.crewMaster.length} CREW &gt;</span>
        </a>

        <!-- 4. Operational Notices -->
        <a href="#/notifications" class="menu-card">
          <div class="menu-icon-wrap" style="background: rgba(139, 92, 246, 0.15); color: #8B5CF6;">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
            </svg>
          </div>
          <div class="menu-info">
            <h3>Lobby Broadcasts &amp; Notices</h3>
            <p>Safety circulars, emergency updates, and division instructions</p>
          </div>
          <span class="menu-badge badge-active">${notifs.length} NOTICES &gt;</span>
        </a>

        <!-- 5. Admin Panel -->
        ${(auth.user?.role === 'ADMIN' || store.isAdminSessionActive()) ? `
        <a href="#/admin" class="menu-card" style="border-color: rgba(245, 158, 11, 0.4);">
          <div class="menu-icon-wrap" style="background: rgba(245, 158, 11, 0.2); color: #F59E0B;">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
            </svg>
          </div>
          <div class="menu-info">
            <h3>Admin Panel &amp; Approvals</h3>
            <p>Manage user approvals, roles, audit logs, and configuration</p>
          </div>
          <span class="menu-badge" style="background: #F59E0B; color: #0F172A;">${pendingUsers.length} PENDING &gt;</span>
        </a>` : ''}
      </div>
    `;
  }

  refresh();
  container.appendChild(content);

  // Subscribe to live store changes
  const unsubDuties = store.subscribeDuties(refresh);
  const unsubUsers = store.subscribeUsers(refresh);

  return container;
}
