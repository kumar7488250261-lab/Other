// Duty & Long-Hour Monitoring View - Connected in Real Time to Firebase Firestore
import { store } from '../store.js';
import { renderHeader } from '../components/Header.js';
import { showToast } from '../components/Toast.js';

export function renderDutyView() {
  const container = document.createElement('div');
  container.className = 'fade-in';

  const header = renderHeader({
    title: 'Live Duty & Long-Hours',
    subtitle: 'Real-time Train & Crew Movement Tracker',
    showBack: true,
    onBack: () => window.location.hash = '#/dashboard'
  });
  container.appendChild(header);

  const content = document.createElement('div');
  content.className = 'app-content';

  let filterStatus = 'ALL'; // ALL, ON_DUTY, LONG_HOUR, AWAITING_RELIEF, RELIEVED
  let searchQuery = '';

  function render() {
    const allDuties = store.getDuties();
    const filtered = allDuties.filter(d => {
      // Status filter
      if (filterStatus === 'ON_DUTY' && (d.status === 'RELIEVED' || d.status === 'COMPLETED')) return false;
      if (filterStatus === 'LONG_HOUR') {
        const dur = store.calculateDutyDuration(d.signOnDate, d.signOnTime);
        if (!dur.isLongHour || d.status === 'RELIEVED') return false;
      }
      if (filterStatus === 'AWAITING_RELIEF' && d.reliefStatus !== 'AWAITING_RELIEF') return false;
      if (filterStatus === 'RELIEVED' && d.status !== 'RELIEVED') return false;

      // Query filter
      if (searchQuery) {
        const q = searchQuery.toLowerCase();
        return (d.crewId && d.crewId.toLowerCase().includes(q)) ||
               (d.crewName && d.crewName.toLowerCase().includes(q)) ||
               (d.trainNo && d.trainNo.toLowerCase().includes(q)) ||
               (d.locoNo && d.locoNo.toLowerCase().includes(q)) ||
               (d.section && d.section.toLowerCase().includes(q));
      }
      return true;
    });

    content.innerHTML = `
      <!-- Action Toolbar -->
      <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; margin-bottom: 16px;">
        <div style="display: flex; gap: 8px; flex-wrap: wrap;">
          <button class="btn btn-sm ${filterStatus === 'ALL' ? 'btn-primary' : 'btn-outline'}" id="filter-all">All (${allDuties.length})</button>
          <button class="btn btn-sm ${filterStatus === 'ON_DUTY' ? 'btn-primary' : 'btn-outline'}" id="filter-onduty">On Duty</button>
          <button class="btn btn-sm ${filterStatus === 'LONG_HOUR' ? 'btn-primary' : 'btn-outline'}" style="${filterStatus === 'LONG_HOUR' ? 'background:#F59E0B; border-color:#F59E0B;' : 'color:#F59E0B;'}" id="filter-longhour">Long Hours (&gt;9h)</button>
          <button class="btn btn-sm ${filterStatus === 'AWAITING_RELIEF' ? 'btn-primary' : 'btn-outline'}" style="${filterStatus === 'AWAITING_RELIEF' ? 'background:#EF4444; border-color:#EF4444;' : 'color:#EF4444;'}" id="filter-relief">Awaiting Relief</button>
        </div>

        <button class="btn btn-primary" id="btn-add-duty" style="padding: 8px 16px;">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="margin-right: 6px;">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          Record Duty
        </button>
      </div>

      <!-- Search Input -->
      <div style="margin-bottom: 16px;">
        <input type="text" 
               id="input-duty-search" 
               class="input-field" 
               style="font-family: var(--font-mono); font-size: 13.5px;" 
               placeholder="Search by Crew ID, Name, Train No, Loco No (e.g. 31422)..." 
               value="${searchQuery}">
      </div>

      <!-- Duties List -->
      <div id="duties-list" style="display: flex; flex-direction: column; gap: 12px;">
        ${filtered.length === 0 ? `
          <div class="card" style="text-align: center; padding: 40px 20px; color: #94A3B8;">
            <p>No duty records found matching the selected filter.</p>
          </div>
        ` : filtered.map(d => {
          const duration = store.calculateDutyDuration(d.signOnDate, d.signOnTime);
          let durationClass = 'tag-normal';
          let durationBadge = `<span class="badge" style="background:#1E293B; color:#94A3B8;">${duration.text}</span>`;
          if (duration.isUrgent) {
            durationBadge = `<span class="badge" style="background:#EF4444; color:#FFFFFF; font-weight:700;">🚨 ${duration.text} (11h+ URGENT)</span>`;
          } else if (duration.isLongHour) {
            durationBadge = `<span class="badge" style="background:#F59E0B; color:#0F172A; font-weight:700;">⚠️ ${duration.text} (9h+ LONG)</span>`;
          }

          return `
            <div class="card duty-card" style="border-left: 4px solid ${duration.isUrgent ? '#EF4444' : duration.isLongHour ? '#F59E0B' : '#10B981'};">
              <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 8px;">
                <div>
                  <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                    <span class="id-pill" style="font-size: 13px;">${d.crewId}</span>
                    <strong style="font-size: 15px; color: #FFFFFF;">${d.crewName}</strong>
                    <span class="role-pill">${d.designation || 'LP'}</span>
                  </div>
                  <div style="margin-top: 4px; font-size: 12.5px; color: #F59E0B; font-weight: 600; font-family: var(--font-mono);">
                    🚆 Train: ${d.trainNo || 'N/A'} • Loco: ${d.locoNo || 'N/A'} • Section: ${d.section || 'KHS Section'}
                  </div>
                </div>
                <div style="display: flex; align-items: center; gap: 8px;">
                  ${durationBadge}
                  <span class="badge" style="background:${d.status === 'RELIEVED' ? '#10B981' : '#1E293B'}; color:#FFF;">
                    ${d.status}
                  </span>
                </div>
              </div>

              <!-- Duty Details Grid -->
              <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 10px; margin-top: 12px; padding-top: 10px; border-top: 1px solid #1E293B; font-size: 12px;">
                <div>
                  <span style="color: #64748B;">Sign-on:</span>
                  <div style="font-family: var(--font-mono); color: #F1F5F9; font-weight: 600;">${d.signOnTime} (${d.signOnDate})</div>
                </div>
                <div>
                  <span style="color: #64748B;">Position:</span>
                  <div style="color: #38BDF8; font-weight: 600;">${d.currentPosition || 'On Run'}</div>
                </div>
                <div>
                  <span style="color: #64748B;">GDR Status:</span>
                  <div style="color: #F1F5F9;">${d.gdrStatus || 'Done'}</div>
                </div>
                <div>
                  <span style="color: #64748B;">Expected Dep:</span>
                  <div style="font-family: var(--font-mono); color: #F1F5F9;">${d.expectedDeparture || '-'}</div>
                </div>
              </div>

              ${d.remarks ? `
                <div style="margin-top: 8px; font-size: 12px; color: #94A3B8; background: #0F172A; padding: 6px 10px; border-radius: 6px;">
                  📝 <em>${d.remarks}</em>
                </div>
              ` : ''}

              <!-- Actions row -->
              <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 12px;">
                ${d.status !== 'RELIEVED' ? `
                  <button class="btn btn-sm btn-outline btn-mark-relieved" data-id="${d.id}" data-crew="${d.crewName}" style="color: #10B981; border-color: #10B981;">
                    Mark Relieved
                  </button>
                  <button class="btn btn-sm btn-outline btn-quick-status" data-id="${d.id}" style="color: #38BDF8; border-color: #38BDF8;">
                    Update Status
                  </button>
                ` : `
                  <span style="font-size: 12px; color: #10B981;">✅ Relieved at ${d.reliefStation || 'Station'} (${d.reliefTime || ''})</span>
                `}
              </div>
            </div>
          `;
        }).join('')}
      </div>

      <!-- Add Duty Record Modal Container -->
      <div id="duty-modal-wrap"></div>
    `;

    // Bind event handlers
    content.querySelector('#filter-all')?.addEventListener('click', () => { filterStatus = 'ALL'; render(); });
    content.querySelector('#filter-onduty')?.addEventListener('click', () => { filterStatus = 'ON_DUTY'; render(); });
    content.querySelector('#filter-longhour')?.addEventListener('click', () => { filterStatus = 'LONG_HOUR'; render(); });
    content.querySelector('#filter-relief')?.addEventListener('click', () => { filterStatus = 'AWAITING_RELIEF'; render(); });

    const searchInput = content.querySelector('#input-duty-search');
    searchInput?.addEventListener('input', (e) => {
      searchQuery = e.target.value.trim();
      render();
    });

    content.querySelector('#btn-add-duty')?.addEventListener('click', () => {
      showAddDutyModal();
    });

    content.querySelectorAll('.btn-mark-relieved').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-id');
        const crew = btn.getAttribute('data-crew');
        const station = prompt(`Relieve ${crew}. Enter Relief Station Code (e.g., RIG, BSP, CPH):`, 'RIG');
        if (station) {
          store.markDutyRelieved(id, station.trim().toUpperCase());
          showToast(`Relief recorded for ${crew} at ${station.trim().toUpperCase()}`);
          render();
        }
      });
    });

    content.querySelectorAll('.btn-quick-status').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-id');
        const newPos = prompt('Update Current Train Position / Remark:', 'Placed in siding');
        if (newPos) {
          const duty = allDuties.find(d => d.id === id);
          if (duty) {
            duty.currentPosition = newPos.trim();
            store.saveDuty(duty);
            showToast('Train position updated');
            render();
          }
        }
      });
    });
  }

  function showAddDutyModal() {
    const wrap = content.querySelector('#duty-modal-wrap');
    if (!wrap) return;

    const todayDate = new Date().toLocaleDateString('en-GB');
    const nowTime = new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });

    wrap.innerHTML = `
      <div class="modal-backdrop fade-in" style="position: fixed; inset: 0; background: rgba(0,0,0,0.75); display: flex; align-items: center; justify-content: center; z-index: 9999; padding: 16px;">
        <div class="card" style="width: 100%; max-width: 520px; max-height: 90vh; overflow-y: auto; background: #111A29; border: 1px solid #2D466E;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <h3 style="color: #FFFFFF; font-size: 17px; font-weight: 700;">Record New Running Duty</h3>
            <button id="modal-close" style="background: transparent; border: none; color: #94A3B8; font-size: 20px; cursor: pointer;">✕</button>
          </div>

          <form id="form-duty-record">
            <div class="form-group" style="margin-bottom: 12px;">
              <label class="form-label">Crew ID (Auto-fetches Name)</label>
              <input type="text" id="m-crew-id" class="input-field" placeholder="e.g. KHS1001" required style="font-family: var(--font-mono); text-transform: uppercase;">
              <div id="crew-match-preview" style="font-size: 12px; color: #10B981; margin-top: 4px; font-weight: 600;"></div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 12px;">
              <div class="form-group">
                <label class="form-label">Train Number</label>
                <input type="text" id="m-train-no" class="input-field" placeholder="e.g. BOXN / 12834" required>
              </div>
              <div class="form-group">
                <label class="form-label">Loco Number</label>
                <input type="text" id="m-loco-no" class="input-field" placeholder="e.g. 31422 WAG9" required style="font-family: var(--font-mono);">
              </div>
            </div>

            <div class="form-group" style="margin-bottom: 12px;">
              <label class="form-label">Section</label>
              <input type="text" id="m-section" class="input-field" placeholder="e.g. KHS - RIG / CHHL - GURA" value="KHS - RIG" required>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 12px;">
              <div class="form-group">
                <label class="form-label">Sign-on Date</label>
                <input type="text" id="m-signon-date" class="input-field" value="${todayDate}" required>
              </div>
              <div class="form-group">
                <label class="form-label">Sign-on Time</label>
                <input type="text" id="m-signon-time" class="input-field" value="${nowTime}" required style="font-family: var(--font-mono);">
              </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 12px;">
              <div class="form-group">
                <label class="form-label">Current Position</label>
                <input type="text" id="m-position" class="input-field" placeholder="e.g. Lobby / Siding" value="Lobby Platform">
              </div>
              <div class="form-group">
                <label class="form-label">GDR Status</label>
                <select id="m-gdr" class="input-field" style="background: #090E17; color: #FFF;">
                  <option value="Done">GDR Done</option>
                  <option value="In Progress">GDR In Progress</option>
                  <option value="Not Required">Not Required</option>
                </select>
              </div>
            </div>

            <div class="form-group" style="margin-bottom: 16px;">
              <label class="form-label">Remarks</label>
              <input type="text" id="m-remarks" class="input-field" placeholder="Operational remarks...">
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 10px;">
              <button type="button" id="modal-cancel" class="btn btn-outline">Cancel</button>
              <button type="submit" class="btn btn-primary">Save &amp; Sync Duty</button>
            </div>
          </form>
        </div>
      </div>
    `;

    const mCrewId = wrap.querySelector('#m-crew-id');
    const preview = wrap.querySelector('#crew-match-preview');
    let matchedProfile = null;

    mCrewId?.addEventListener('input', () => {
      const q = mCrewId.value.trim();
      matchedProfile = store.findCrewByIdOrName(q);
      if (matchedProfile) {
        preview.textContent = `✓ ${matchedProfile.name} • ${matchedProfile.designation}`;
      } else {
        preview.textContent = '';
      }
    });

    wrap.querySelector('#modal-close')?.addEventListener('click', () => wrap.innerHTML = '');
    wrap.querySelector('#modal-cancel')?.addEventListener('click', () => wrap.innerHTML = '');

    wrap.querySelector('#form-duty-record')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const crewId = mCrewId.value.trim().toUpperCase();
      const trainNo = wrap.querySelector('#m-train-no').value.trim();
      const locoNo = wrap.querySelector('#m-loco-no').value.trim();
      const section = wrap.querySelector('#m-section').value.trim();
      const signOnDate = wrap.querySelector('#m-signon-date').value.trim();
      const signOnTime = wrap.querySelector('#m-signon-time').value.trim();
      const position = wrap.querySelector('#m-position').value.trim();
      const gdr = wrap.querySelector('#m-gdr').value;
      const remarks = wrap.querySelector('#m-remarks').value.trim();

      const newDuty = {
        crewId,
        crewName: matchedProfile ? matchedProfile.name : `Crew ${crewId}`,
        designation: matchedProfile ? matchedProfile.designation : 'LP (Goods)',
        trainNo,
        locoNo,
        section,
        signOnDate,
        signOnTime,
        currentPosition: position,
        gdrStatus: gdr,
        status: 'ON_DUTY',
        reliefStatus: 'NORMAL',
        remarks
      };

      await store.saveDuty(newDuty);
      showToast(`Duty saved and synchronized for ${crewId}!`);
      wrap.innerHTML = '';
      render();
    });
  }

  render();
  container.appendChild(content);

  // Real-time listener for duties
  store.subscribeDuties(render);

  return container;
}
