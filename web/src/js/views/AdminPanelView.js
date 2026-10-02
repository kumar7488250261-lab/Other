// Admin Panel View - User Approval, Role Assignment, Audit History & Configuration
import { store } from '../store.js';
import { renderHeader } from '../components/Header.js';
import { showToast } from '../components/Toast.js';

export function renderAdminPanelView() {
  const container = document.createElement('div');
  container.className = 'fade-in';

  const auth = store.getAuth();
  const isAdmin = auth.user?.role === 'ADMIN' || store.isAdminSessionActive();

  if (!isAdmin) {
    const header = renderHeader({
      title: 'Restricted Access',
      subtitle: 'Administrator Credentials Required',
      showBack: true,
      onBack: () => window.location.hash = '#/dashboard'
    });
    container.appendChild(header);

    const content = document.createElement('div');
    content.className = 'app-content';
    content.innerHTML = `
      <div class="card" style="max-width: 440px; margin: 40px auto; text-align: center;">
        <span style="font-size: 40px;">🔒</span>
        <h3 style="color: #FFFFFF; font-size: 18px; font-weight: 700; margin: 12px 0;">Admin PIN Required</h3>
        <p style="font-size: 13px; color: #94A3B8; margin-bottom: 20px;">
          Only authorized Chief Crew Controllers (CCC) or In-Charge staff can manage user approvals and roles.
        </p>
        <div style="display: flex; gap: 8px; justify-content: center;">
          <input type="password" id="admin-pin-input" class="input-field" placeholder="Enter PIN (1234)" style="max-width: 160px; text-align: center; font-family: var(--font-mono);">
          <button class="btn btn-primary" id="btn-admin-auth">Unlock</button>
        </div>
      </div>
    `;

    content.querySelector('#btn-admin-auth')?.addEventListener('click', () => {
      const pin = content.querySelector('#admin-pin-input').value.trim();
      if (store.verifyAdminPin(pin)) {
        showToast('Admin authenticated!');
        window.location.hash = '#/admin';
      } else {
        showToast('Invalid PIN! Default is 1234');
      }
    });

    container.appendChild(content);
    return container;
  }

  const header = renderHeader({
    title: 'Admin Control Center',
    subtitle: 'User Approvals, Roles & Audit Trail',
    showBack: true,
    onBack: () => window.location.hash = '#/dashboard'
  });
  container.appendChild(header);

  const content = document.createElement('div');
  content.className = 'app-content';

  let currentTab = 'pending'; // pending, all_users, audit, config

  function render() {
    const users = store.getUsers();
    const pendingUsers = users.filter(u => u.status === 'PENDING');
    const auditLogs = store.getAuditLogs();
    const config = store.getAppConfig();

    content.innerHTML = `
      <!-- Admin Tab Switcher -->
      <div class="tabs-nav" style="margin-bottom: 20px;">
        <button class="tab-btn ${currentTab === 'pending' ? 'active' : ''}" id="a-tab-pending">
          <span>Pending Approvals</span>
          <span class="tab-badge" style="background:#EF4444; color:#FFF;">${pendingUsers.length}</span>
        </button>
        <button class="tab-btn ${currentTab === 'all_users' ? 'active' : ''}" id="a-tab-users">
          <span>All Registered Staff</span>
          <span class="tab-badge">${users.length}</span>
        </button>
        <button class="tab-btn ${currentTab === 'audit' ? 'active' : ''}" id="a-tab-audit">
          <span>Audit Trail</span>
          <span class="tab-badge">${auditLogs.length}</span>
        </button>
        <button class="tab-btn ${currentTab === 'config' ? 'active' : ''}" id="a-tab-config">
          <span>App Configuration</span>
        </button>
      </div>

      <!-- Tab 1: Pending Approvals -->
      ${currentTab === 'pending' ? `
        <div>
          <h3 style="color: #FFFFFF; font-size: 16px; font-weight: 700; margin-bottom: 12px;">
            Registration Approval Requests (${pendingUsers.length})
          </h3>
          ${pendingUsers.length === 0 ? `
            <div class="card" style="text-align: center; padding: 40px; color: #10B981;">
              <span style="font-size: 28px;">✓</span>
              <p style="margin-top: 8px;">All registration requests have been reviewed and approved!</p>
            </div>
          ` : pendingUsers.map(u => `
            <div class="card" style="margin-bottom: 12px; border-left: 4px solid #F59E0B;">
              <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
                <div>
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <span class="id-pill">${u.crewId}</span>
                    <strong style="color: #FFFFFF; font-size: 15px;">${u.name}</strong>
                    <span class="role-pill">${u.role || 'STAFF'}</span>
                  </div>
                  <div style="margin-top: 4px; font-size: 12px; color: #94A3B8; font-family: var(--font-mono);">
                    📱 Mobile: ${u.mobile} • Requested: ${new Date(u.createdAt).toLocaleDateString('en-GB')}
                  </div>
                </div>

                <div style="display: flex; align-items: center; gap: 8px;">
                  <select class="input-field role-select" data-uid="${u.uid}" style="width: auto; padding: 6px 10px; font-size: 12px; background: #090E17; color: #FFF;">
                    <option value="STAFF" ${u.role === 'STAFF' ? 'selected' : ''}>Role: STAFF</option>
                    <option value="SUPERVISOR" ${u.role === 'SUPERVISOR' ? 'selected' : ''}>Role: SUPERVISOR</option>
                    <option value="ROSTER_UPDATER" ${u.role === 'ROSTER_UPDATER' ? 'selected' : ''}>Role: ROSTER_UPDATER</option>
                    <option value="ADMIN" ${u.role === 'ADMIN' ? 'selected' : ''}>Role: ADMIN</option>
                  </select>
                  <button class="btn btn-sm btn-primary btn-approve-user" data-uid="${u.uid}" style="background: #10B981; border: none;">
                    Approve
                  </button>
                  <button class="btn btn-sm btn-outline btn-reject-user" data-uid="${u.uid}" style="color: #EF4444; border-color: #EF4444;">
                    Reject
                  </button>
                </div>
              </div>
            </div>
          `).join('')}
        </div>
      ` : ''}

      <!-- Tab 2: All Users Directory -->
      ${currentTab === 'all_users' ? `
        <div class="card" style="padding: 0; overflow: hidden;">
          <div style="overflow-x: auto;">
            <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 13px;">
              <thead>
                <tr style="background: #090E17; border-bottom: 1px solid #1E293B; color: #F59E0B; font-family: var(--font-mono); font-size: 11px;">
                  <th style="padding: 10px 14px;">Crew ID</th>
                  <th style="padding: 10px 14px;">Name</th>
                  <th style="padding: 10px 14px;">Mobile</th>
                  <th style="padding: 10px 14px;">Role</th>
                  <th style="padding: 10px 14px;">Status</th>
                  <th style="padding: 10px 14px;">Actions</th>
                </tr>
              </thead>
              <tbody>
                ${users.map(u => `
                  <tr style="border-bottom: 1px solid #111A29;">
                    <td style="padding: 10px 14px;"><span class="id-pill">${u.crewId}</span></td>
                    <td style="padding: 10px 14px; font-weight: 600; color: #FFF;">${u.name}</td>
                    <td style="padding: 10px 14px; font-family: var(--font-mono); color: #94A3B8;">${u.mobile}</td>
                    <td style="padding: 10px 14px;"><span class="role-pill">${u.role}</span></td>
                    <td style="padding: 10px 14px;">
                      <span class="badge" style="background:${u.status === 'APPROVED' ? '#10B981' : u.status === 'PENDING' ? '#F59E0B' : '#EF4444'}; color:#FFF; font-size:10px;">
                        ${u.status}
                      </span>
                    </td>
                    <td style="padding: 10px 14px;">
                      ${u.status === 'APPROVED' ? `
                        <button class="btn btn-sm btn-outline btn-disable-user" data-uid="${u.uid}" style="padding: 4px 8px; font-size: 11px; color: #EF4444; border-color: #EF4444;">
                          Disable
                        </button>
                      ` : `
                        <button class="btn btn-sm btn-primary btn-approve-user" data-uid="${u.uid}" style="padding: 4px 8px; font-size: 11px; background: #10B981; border:none;">
                          Approve
                        </button>
                      `}
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      ` : ''}

      <!-- Tab 3: Audit Trail -->
      ${currentTab === 'audit' ? `
        <div class="card" style="padding: 0; overflow: hidden;">
          <div style="overflow-x: auto;">
            <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 12.5px;">
              <thead>
                <tr style="background: #090E17; border-bottom: 1px solid #1E293B; color: #F59E0B; font-family: var(--font-mono); font-size: 11px;">
                  <th style="padding: 10px 14px;">Timestamp</th>
                  <th style="padding: 10px 14px;">Action</th>
                  <th style="padding: 10px 14px;">Performed By</th>
                  <th style="padding: 10px 14px;">Details</th>
                </tr>
              </thead>
              <tbody>
                ${auditLogs.length === 0 ? `
                  <tr><td colspan="4" style="padding: 24px; text-align: center; color: #64748B;">No administrative actions recorded yet.</td></tr>
                ` : auditLogs.map(a => `
                  <tr style="border-bottom: 1px solid #111A29;">
                    <td style="padding: 10px 14px; font-family: var(--font-mono); color: #64748B;">${new Date(a.timestamp).toLocaleString('en-GB')}</td>
                    <td style="padding: 10px 14px;"><strong style="color: #38BDF8;">${a.action}</strong></td>
                    <td style="padding: 10px 14px; font-family: var(--font-mono); color: #F1F5F9;">${a.performedBy}</td>
                    <td style="padding: 10px 14px; color: #CBD5E1;">${a.details}</td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      ` : ''}

      <!-- Tab 4: Configuration -->
      ${currentTab === 'config' ? `
        <div class="card" style="max-width: 600px;">
          <h3 style="color: #FFFFFF; font-size: 16px; font-weight: 700; margin-bottom: 14px;">
            Lobby Operational Thresholds
          </h3>
          <form id="form-config">
            <div class="form-group" style="margin-bottom: 12px;">
              <label class="form-label">Long-Hour Warning Threshold (Hours)</label>
              <input type="number" step="0.5" id="cfg-long" class="input-field" value="${config.longHourThresholdHours}">
              <small style="color: #64748B; font-size: 11px;">Triggers Amber alert when running duty exceeds this duration (Default: 9.0h)</small>
            </div>
            <div class="form-group" style="margin-bottom: 12px;">
              <label class="form-label">Urgent Relief Alert Threshold (Hours)</label>
              <input type="number" step="0.5" id="cfg-urgent" class="input-field" value="${config.urgentReliefHours}">
              <small style="color: #64748B; font-size: 11px;">Triggers Red alert requiring immediate station relief (Default: 11.0h)</small>
            </div>
            <div class="form-group" style="margin-bottom: 16px;">
              <label class="form-label">Official Android APK Download URL</label>
              <input type="text" id="cfg-apk" class="input-field" value="${config.latestApkUrl}">
            </div>
            <button type="submit" class="btn btn-primary">Save Configuration</button>
          </form>
        </div>
      ` : ''}
    `;

    // Tab bindings
    content.querySelector('#a-tab-pending')?.addEventListener('click', () => { currentTab = 'pending'; render(); });
    content.querySelector('#a-tab-users')?.addEventListener('click', () => { currentTab = 'all_users'; render(); });
    content.querySelector('#a-tab-audit')?.addEventListener('click', () => { currentTab = 'audit'; render(); });
    content.querySelector('#a-tab-config')?.addEventListener('click', () => { currentTab = 'config'; render(); });

    // User action bindings
    content.querySelectorAll('.btn-approve-user').forEach(btn => {
      btn.addEventListener('click', async () => {
        const uid = btn.getAttribute('data-uid');
        const roleSel = content.querySelector(`.role-select[data-uid="${uid}"]`);
        const role = roleSel ? roleSel.value : 'STAFF';
        await store.approveUser(uid, role);
        showToast('User approved successfully!');
        render();
      });
    });

    content.querySelectorAll('.btn-reject-user').forEach(btn => {
      btn.addEventListener('click', async () => {
        const uid = btn.getAttribute('data-uid');
        if (confirm('Are you sure you want to reject this registration request?')) {
          await store.rejectUser(uid);
          showToast('User registration rejected.');
          render();
        }
      });
    });

    content.querySelectorAll('.btn-disable-user').forEach(btn => {
      btn.addEventListener('click', async () => {
        const uid = btn.getAttribute('data-uid');
        await store.disableUser(uid);
        showToast('User account disabled.');
        render();
      });
    });

    content.querySelector('#form-config')?.addEventListener('submit', (e) => {
      e.preventDefault();
      config.longHourThresholdHours = parseFloat(content.querySelector('#cfg-long').value) || 9.0;
      config.urgentReliefHours = parseFloat(content.querySelector('#cfg-urgent').value) || 11.0;
      config.latestApkUrl = content.querySelector('#cfg-apk').value.trim();
      showToast('Configuration saved!');
    });
  }

  render();
  container.appendChild(content);

  store.subscribeUsers(render);
  return container;
}
