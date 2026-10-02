// Shift Roster Module - Kharsia Lobby Shift Wise Running Staff & Supervisor Assignment
import { store } from '../store.js';
import { renderHeader } from '../components/Header.js';
import { showToast } from '../components/Toast.js';

export function renderRosterView() {
  const container = document.createElement('div');
  container.className = 'fade-in';

  const header = renderHeader({
    title: 'Shift Wise Roster',
    subtitle: 'SECR Kharsia Lobby 24x7 Staff Duty Matrix',
    showBack: true,
    onBack: () => window.location.hash = '#/dashboard'
  });
  container.appendChild(header);

  const content = document.createElement('div');
  content.className = 'app-content';

  let currentShift = '06-14'; // 06-14, 14-22, 22-06
  const todayDate = new Date().toLocaleDateString('en-GB');

  // Default shift roles
  const defaultRoles = [
    { role: 'Lobby CLI', placeholder: 'Chief Loco Inspector' },
    { role: 'CMS Operator', placeholder: 'Crew Management System Operator' },
    { role: 'Desk In-Charge / DI', placeholder: 'Lobby Desk Duty In-Charge' },
    { role: 'TFR (Traffic Relief)', placeholder: 'Loco Pilot / Traffic Relief' },
    { role: 'LH (Long Hour Relief)', placeholder: 'Long Hour Relief Pilot' },
    { role: 'WD (Waiting Duty)', placeholder: 'Waiting Duty Running Staff' },
    { role: 'Sander Boy', placeholder: 'Loco Sanding & Equipment Support' },
    { role: 'TLC Power (BSP)', placeholder: 'Traction Loco Controller (SECR)' }
  ];

  function render() {
    const auth = store.getAuth();
    const canEdit = auth.user?.role === 'ADMIN' || auth.user?.role === 'ROSTER_UPDATER' || auth.user?.role === 'SUPERVISOR' || store.isAdminSessionActive();
    const allRoster = store.getRosterRecords ? store.getRosterRecords() : store.rosterRecords;

    content.innerHTML = `
      <!-- Shift Selector Navigation -->
      <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; margin-bottom: 16px;">
        <div class="tabs-nav" style="margin-bottom: 0; border-bottom: none;">
          <button class="tab-btn ${currentShift === '06-14' ? 'active' : ''}" id="shift-1">
            <span>🌅 06:00 – 14:00 (Morning)</span>
          </button>
          <button class="tab-btn ${currentShift === '14-22' ? 'active' : ''}" id="shift-2">
            <span>☀️ 14:00 – 22:00 (Evening)</span>
          </button>
          <button class="tab-btn ${currentShift === '22-06' ? 'active' : ''}" id="shift-3">
            <span>🌙 22:00 – 06:00 (Night)</span>
          </button>
        </div>

        <div style="font-size: 13px; color: #F59E0B; font-weight: 700; font-family: var(--font-mono);">
          Date: ${todayDate}
        </div>
      </div>

      <!-- Notice for Role Permissions -->
      ${!canEdit ? `
        <div style="background: rgba(56, 189, 248, 0.1); border: 1px solid rgba(56, 189, 248, 0.3); border-radius: 8px; padding: 10px 14px; margin-bottom: 16px; font-size: 12px; color: #93C5FD;">
          ℹ️ Viewing read-only shift roster. Authorized In-Charge, Roster Updaters, or Admin can edit and save changes.
        </div>
      ` : `
        <div style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.3); border-radius: 8px; padding: 10px 14px; margin-bottom: 16px; font-size: 12px; color: #34D399; display: flex; justify-content: space-between; align-items: center;">
          <span>✓ Roster Updater mode enabled. Changes synchronize immediately across Android &amp; Web.</span>
        </div>
      `}

      <!-- Roster Grid -->
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 14px;">
        ${defaultRoles.map((item, idx) => {
          const saved = allRoster.find(r => r.shift === currentShift && r.role === item.role);
          const staffName = saved?.staffName || '';
          const staffId = saved?.staffId || '';
          const mobile = saved?.mobile || '';

          return `
            <div class="card" style="display: flex; flex-direction: column; justify-content: space-between; gap: 10px;">
              <div>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span class="role-pill" style="font-size: 11px;">#0${idx + 1}</span>
                  <span style="font-size: 11px; color: #64748B;">Shift: ${currentShift}</span>
                </div>
                <h4 style="font-size: 15px; font-weight: 700; color: #FFFFFF; margin-top: 6px;">
                  ${item.role}
                </h4>
                <p style="font-size: 11.5px; color: #94A3B8; margin-bottom: 8px;">
                  ${item.placeholder}
                </p>

                ${staffName ? `
                  <div style="background: #090E17; border: 1px solid #1E293B; border-radius: 8px; padding: 10px;">
                    <div style="display: flex; align-items: center; gap: 6px;">
                      <span class="id-pill">${staffId || 'ID'}</span>
                      <strong style="color: #F1F5F9; font-size: 13.5px;">${staffName}</strong>
                    </div>
                    ${mobile ? `
                      <div style="margin-top: 4px; font-size: 12px; color: #10B981; font-family: var(--font-mono);">
                        📞 <a href="tel:${mobile}" style="color: #10B981; text-decoration: none;">${mobile}</a>
                      </div>
                    ` : ''}
                  </div>
                ` : `
                  <div style="background: rgba(239, 68, 68, 0.08); border: 1px dashed rgba(239, 68, 68, 0.3); border-radius: 8px; padding: 10px; color: #FCA5A5; font-size: 12px;">
                    Not yet assigned for this shift
                  </div>
                `}
              </div>

              ${canEdit ? `
                <button class="btn btn-sm btn-outline btn-edit-role" data-role="${item.role}" data-shift="${currentShift}" data-name="${staffName}" data-id="${staffId}" data-mob="${mobile}" style="margin-top: 6px; width: 100%;">
                  ${staffName ? 'Change Assignment' : '+ Assign Staff'}
                </button>
              ` : ''}
            </div>
          `;
        }).join('')}
      </div>

      <!-- Assign Role Modal Container -->
      <div id="roster-modal-wrap"></div>
    `;

    // Shift tab switching
    content.querySelector('#shift-1')?.addEventListener('click', () => { currentShift = '06-14'; render(); });
    content.querySelector('#shift-2')?.addEventListener('click', () => { currentShift = '14-22'; render(); });
    content.querySelector('#shift-3')?.addEventListener('click', () => { currentShift = '22-06'; render(); });

    // Edit button click handlers
    content.querySelectorAll('.btn-edit-role').forEach(btn => {
      btn.addEventListener('click', () => {
        const role = btn.getAttribute('data-role');
        const shift = btn.getAttribute('data-shift');
        const name = btn.getAttribute('data-name');
        const id = btn.getAttribute('data-id');
        const mob = btn.getAttribute('data-mob');
        showAssignModal(role, shift, name, id, mob);
      });
    });
  }

  function showAssignModal(role, shift, curName, curId, curMob) {
    const wrap = content.querySelector('#roster-modal-wrap');
    if (!wrap) return;

    wrap.innerHTML = `
      <div class="modal-backdrop fade-in" style="position: fixed; inset: 0; background: rgba(0,0,0,0.75); display: flex; align-items: center; justify-content: center; z-index: 9999; padding: 16px;">
        <div class="card" style="width: 100%; max-width: 440px; background: #111A29; border: 1px solid #2D466E;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
            <h3 style="color: #FFFFFF; font-size: 16px; font-weight: 700;">Assign Staff for ${role}</h3>
            <button id="modal-r-close" style="background: transparent; border: none; color: #94A3B8; font-size: 20px; cursor: pointer;">✕</button>
          </div>

          <form id="form-roster-assign">
            <div class="form-group" style="margin-bottom: 12px;">
              <label class="form-label">Crew / Staff ID (Auto-fetches)</label>
              <input type="text" id="r-staff-id" class="input-field" value="${curId || ''}" placeholder="e.g. KHS1003" required style="font-family: var(--font-mono); text-transform: uppercase;">
              <div id="r-crew-preview" style="font-size: 12px; color: #10B981; margin-top: 4px; font-weight: 600;"></div>
            </div>

            <div class="form-group" style="margin-bottom: 12px;">
              <label class="form-label">Staff Name</label>
              <input type="text" id="r-staff-name" class="input-field" value="${curName || ''}" placeholder="Full Name" required>
            </div>

            <div class="form-group" style="margin-bottom: 16px;">
              <label class="form-label">Mobile Number</label>
              <input type="text" id="r-mobile" class="input-field" value="${curMob || ''}" placeholder="CUG / Mobile" style="font-family: var(--font-mono);">
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 10px;">
              <button type="button" id="modal-r-cancel" class="btn btn-outline">Cancel</button>
              <button type="submit" class="btn btn-primary">Save to Roster</button>
            </div>
          </form>
        </div>
      </div>
    `;

    const rStaffId = wrap.querySelector('#r-staff-id');
    const rStaffName = wrap.querySelector('#r-staff-name');
    const preview = wrap.querySelector('#r-crew-preview');

    rStaffId?.addEventListener('input', () => {
      const q = rStaffId.value.trim();
      const match = store.findCrewByIdOrName(q);
      if (match) {
        preview.textContent = `✓ Found: ${match.name} (${match.designation})`;
        if (!rStaffName.value || rStaffName.value === curName) {
          rStaffName.value = match.name;
        }
      } else {
        preview.textContent = '';
      }
    });

    wrap.querySelector('#modal-r-close')?.addEventListener('click', () => wrap.innerHTML = '');
    wrap.querySelector('#modal-r-cancel')?.addEventListener('click', () => wrap.innerHTML = '');

    wrap.querySelector('#form-roster-assign')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const staffId = rStaffId.value.trim().toUpperCase();
      const staffName = rStaffName.value.trim();
      const mobile = wrap.querySelector('#r-mobile').value.trim();

      await store.saveRosterItem({
        shift,
        role,
        staffId,
        staffName,
        mobile,
        date: todayDate
      });

      showToast(`Roster updated for ${role} (${shift})`);
      wrap.innerHTML = '';
      render();
    });
  }

  render();
  container.appendChild(content);

  // Subscribe to live roster changes
  store.subscribeRoster(render);

  return container;
}
