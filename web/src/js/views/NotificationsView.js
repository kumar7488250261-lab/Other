// Notifications & Broadcasts View - SECR Kharsia Lobby Operational Notices
import { store } from '../store.js';
import { renderHeader } from '../components/Header.js';
import { showToast } from '../components/Toast.js';

export function renderNotificationsView() {
  const container = document.createElement('div');
  container.className = 'fade-in';

  const header = renderHeader({
    title: 'Lobby Broadcasts & Notices',
    subtitle: 'Safety Bulletins & Operational Circulars',
    showBack: true,
    onBack: () => window.location.hash = '#/dashboard'
  });
  container.appendChild(header);

  const content = document.createElement('div');
  content.className = 'app-content';

  function render() {
    const auth = store.getAuth();
    const canPost = auth.user?.role === 'ADMIN' || auth.user?.role === 'SUPERVISOR' || store.isAdminSessionActive();
    const notifs = store.getNotifications();

    content.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h3 style="font-size: 16px; font-weight: 700; color: #FFFFFF;">
          Active Notices &amp; Bulletins (${notifs.length})
        </h3>
        ${canPost ? `
          <button class="btn btn-primary" id="btn-new-notice" style="padding: 7px 14px; font-size: 13px;">
            + Post Bulletin
          </button>
        ` : ''}
      </div>

      <div style="display: flex; flex-direction: column; gap: 12px;">
        ${notifs.length === 0 ? `
          <div class="card" style="text-align: center; padding: 40px; color: #94A3B8;">
            <p>No operational announcements posted at this time.</p>
          </div>
        ` : notifs.map(n => `
          <div class="card" style="border-left: 4px solid ${n.priority === 'HIGH' ? '#EF4444' : '#38BDF8'};">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 8px;">
              <h4 style="font-size: 15px; font-weight: 700; color: #FFFFFF;">${n.title}</h4>
              <span class="badge" style="background:${n.priority === 'HIGH' ? '#EF4444' : '#1E293B'}; color:#FFF; font-size: 10px;">
                ${n.priority || 'NORMAL'}
              </span>
            </div>
            <p style="margin-top: 8px; font-size: 13px; color: #CBD5E1; line-height: 1.5;">${n.message}</p>
            <div style="margin-top: 10px; font-size: 11px; color: #64748B; font-family: var(--font-mono);">
              Posted: ${new Date(n.createdAt).toLocaleString('en-GB')} • By: ${n.createdBy || 'Lobby Admin'}
            </div>
          </div>
        `).join('')}
      </div>

      <div id="notice-modal-wrap"></div>
    `;

    content.querySelector('#btn-new-notice')?.addEventListener('click', () => {
      showPostModal();
    });
  }

  function showPostModal() {
    const wrap = content.querySelector('#notice-modal-wrap');
    if (!wrap) return;

    wrap.innerHTML = `
      <div class="modal-backdrop fade-in" style="position: fixed; inset: 0; background: rgba(0,0,0,0.75); display: flex; align-items: center; justify-content: center; z-index: 9999; padding: 16px;">
        <div class="card" style="width: 100%; max-width: 480px; background: #111A29; border: 1px solid #2D466E;">
          <h3 style="color: #FFFFFF; font-size: 16px; font-weight: 700; margin-bottom: 14px;">Post Operational Notice</h3>
          <form id="form-notice">
            <div class="form-group" style="margin-bottom: 12px;">
              <label class="form-label">Notice Title</label>
              <input type="text" id="notif-title" class="input-field" placeholder="e.g. Speed Restriction on KHS-RIG Up Line" required>
            </div>
            <div class="form-group" style="margin-bottom: 12px;">
              <label class="form-label">Message Details</label>
              <textarea id="notif-msg" class="input-field" rows="4" placeholder="Full circular instructions..." required></textarea>
            </div>
            <div class="form-group" style="margin-bottom: 16px;">
              <label class="form-label">Priority</label>
              <select id="notif-priority" class="input-field" style="background:#090E17; color:#FFF;">
                <option value="NORMAL">Normal</option>
                <option value="HIGH">High Priority (Urgent Safety)</option>
              </select>
            </div>
            <div style="display: flex; justify-content: flex-end; gap: 10px;">
              <button type="button" id="notif-cancel" class="btn btn-outline">Cancel</button>
              <button type="submit" class="btn btn-primary">Publish Broadcast</button>
            </div>
          </form>
        </div>
      </div>
    `;

    wrap.querySelector('#notif-cancel')?.addEventListener('click', () => wrap.innerHTML = '');
    wrap.querySelector('#form-notice')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const title = wrap.querySelector('#notif-title').value.trim();
      const message = wrap.querySelector('#notif-msg').value.trim();
      const priority = wrap.querySelector('#notif-priority').value;

      await store.postNotification({ title, message, priority });
      showToast('Broadcast published!');
      wrap.innerHTML = '';
      render();
    });
  }

  render();
  container.appendChild(content);

  store.subscribeNotifications(render);
  return container;
}
