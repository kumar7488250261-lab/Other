// User Profile View - Shows Crew ID, Designation, Role, and Approval Status
import { store } from '../store.js';
import { renderHeader } from '../components/Header.js';
import { showToast } from '../components/Toast.js';

export function renderProfileView() {
  const container = document.createElement('div');
  container.className = 'fade-in';

  const header = renderHeader({
    title: 'Staff Profile',
    subtitle: 'Kharsia Lobby Identity & Role Credentials',
    showBack: true,
    onBack: () => window.location.hash = '#/dashboard'
  });
  container.appendChild(header);

  const content = document.createElement('div');
  content.className = 'app-content';

  const auth = store.getAuth();
  const user = auth.user || {
    crewId: 'KHS1234',
    name: 'Kharsia Staff',
    role: 'STAFF',
    status: 'APPROVED',
    mobile: '9752000000'
  };

  content.innerHTML = `
    <div class="card" style="max-width: 560px; margin: 0 auto; border-color: #2D466E;">
      <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 20px;">
        <div style="width: 64px; height: 64px; border-radius: 50%; background: #1E293B; display: flex; align-items: center; justify-content: center; font-size: 28px; border: 2px solid #F59E0B;">
          🚂
        </div>
        <div>
          <h2 style="font-size: 18px; font-weight: 700; color: #FFFFFF;">${user.name}</h2>
          <div style="display: flex; align-items: center; gap: 8px; margin-top: 4px;">
            <span class="id-pill">${user.crewId}</span>
            <span class="role-pill">${user.role}</span>
            <span class="badge" style="background: ${user.status === 'APPROVED' ? '#10B981' : '#F59E0B'}; color: #FFF;">
              ${user.status}
            </span>
          </div>
        </div>
      </div>

      <div style="display: flex; flex-direction: column; gap: 12px; font-size: 13.5px; border-top: 1px solid #1E293B; padding-top: 16px;">
        <div style="display: flex; justify-content: space-between;">
          <span style="color: #64748B;">Lobby Base:</span>
          <strong style="color: #F1F5F9;">Kharsia Combined Lobby (KHS)</strong>
        </div>
        <div style="display: flex; justify-content: space-between;">
          <span style="color: #64748B;">Division:</span>
          <strong style="color: #F1F5F9;">SECR Bilaspur Division</strong>
        </div>
        <div style="display: flex; justify-content: space-between;">
          <span style="color: #64748B;">Designation:</span>
          <strong style="color: #F1F5F9;">${user.designation || 'Running Staff'}</strong>
        </div>
        <div style="display: flex; justify-content: space-between;">
          <span style="color: #64748B;">Registered Mobile:</span>
          <span style="font-family: var(--font-mono); color: #10B981;">${user.mobile || 'Confidential'}</span>
        </div>
        <div style="display: flex; justify-content: space-between;">
          <span style="color: #64748B;">Sync Mode:</span>
          <span style="color: #38BDF8;">Firestore Real-Time Shared Multi-Client</span>
        </div>
      </div>

      <!-- Quick Admin Access PIN -->
      ${user.role !== 'ADMIN' ? `
        <div style="margin-top: 24px; padding-top: 16px; border-top: 1px solid #1E293B;">
          <h4 style="font-size: 13px; font-weight: 700; color: #F59E0B; margin-bottom: 8px;">Supervisor / Admin Access</h4>
          <p style="font-size: 12px; color: #94A3B8; margin-bottom: 12px;">
            If you are a Chief Crew Controller or Lobby In-Charge, enter the Admin PIN (1234) to unlock administrative controls:
          </p>
          <div style="display: flex; gap: 8px;">
            <input type="password" id="input-profile-pin" class="input-field" placeholder="Enter PIN" style="max-width: 140px; font-family: var(--font-mono); text-align: center;">
            <button class="btn btn-primary" id="btn-unlock-admin">Verify PIN</button>
          </div>
        </div>
      ` : ''}

      <div style="margin-top: 24px; display: flex; justify-content: flex-end; gap: 10px;">
        <button class="btn btn-outline" id="btn-profile-logout" style="color: #EF4444; border-color: #EF4444;">
          Sign Out
        </button>
      </div>
    </div>
  `;

  content.querySelector('#btn-unlock-admin')?.addEventListener('click', () => {
    const pin = content.querySelector('#input-profile-pin').value.trim();
    if (store.verifyAdminPin(pin)) {
      showToast('Admin mode verified successfully!');
      window.location.hash = '#/admin';
    } else {
      showToast('Incorrect Admin PIN! Default is 1234');
    }
  });

  content.querySelector('#btn-profile-logout')?.addEventListener('click', () => {
    if (confirm('Are you sure you want to sign out?')) {
      store.logout();
      window.location.hash = '#/welcome';
    }
  });

  container.appendChild(content);
  return container;
}
