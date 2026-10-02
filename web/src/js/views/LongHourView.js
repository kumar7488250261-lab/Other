// Long Hour Duty Monitoring & Update Screen
import { store } from '../store.js';
import { renderHeader } from '../components/Header.js';
import { showToast } from '../components/Toast.js';
import { promptAdminPin } from '../components/AdminPinModal.js';

export function renderLongHourView() {
  const container = document.createElement('div');
  container.className = 'fade-in';

  const header = renderHeader({
    title: 'Long hour update',
    subtitle: 'Crew Duty Hours & Overtime Monitoring',
    showBack: true,
    onBack: () => window.location.hash = '#/menu'
  });
  container.appendChild(header);

  const content = document.createElement('div');
  content.className = 'app-content';

  content.innerHTML = `
    <!-- Tabs Header -->
    <div class="tabs-header">
      <button class="tab-btn active" id="tab-long-active">Active Duties (<span id="count-active-duties">0</span>)</button>
      <button class="tab-btn" id="tab-long-register">Register New Duty</button>
      <button class="tab-btn" id="tab-long-history">Completed Duties</button>
    </div>

    <!-- 1. Active Duties Section -->
    <div id="section-long-active" style="display: flex; flex-direction: column; gap: 14px;">
      <div id="active-duties-container" style="display: flex; flex-direction: column; gap: 14px;"></div>
    </div>

    <!-- 2. Register New Duty Section -->
    <div id="section-long-register" class="card" style="display: none; margin-bottom: 20px;">
      <h3 style="font-size: 17px; font-weight: 700; color: #FFB74D; margin-bottom: 16px; display: flex; align-items: center; gap: 8px;">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M5 22h14"></path>
          <path d="M5 2h14"></path>
          <path d="M17 22v-4.172a2 2 0 0 0-.586-1.414L12 12l-4.414 4.414A2 2 0 0 0 7 17.828V22"></path>
          <path d="M7 2v4.172a2 2 0 0 0 .586 1.414L12 12l4.414-4.414A2 2 0 0 0 17 6.172V2"></path>
        </svg>
        <span>Register Crew Train Duty</span>
      </h3>

      <form id="form-duty-register">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="duty-lpg-id">LPG Crew ID (Auto-Fetch)</label>
            <input type="text" id="duty-lpg-id" class="form-input" placeholder="e.g. KHS1001" required />
          </div>
          <div class="form-group">
            <label class="form-label" for="duty-lpg-name">LPG Name</label>
            <input type="text" id="duty-lpg-name" class="form-input" placeholder="Loco Pilot (Goods) Name" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="duty-alp-id">ALP Crew ID (Auto-Fetch)</label>
            <input type="text" id="duty-alp-id" class="form-input" placeholder="e.g. KHS1006" required />
          </div>
          <div class="form-group">
            <label class="form-label" for="duty-alp-name">ALP Name</label>
            <input type="text" id="duty-alp-name" class="form-input" placeholder="Assistant Loco Pilot Name" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="duty-train-no">Train Number</label>
            <input type="text" id="duty-train-no" class="form-input" placeholder="e.g. BCN/E, 12834, BOXN" required />
          </div>
          <div class="form-group">
            <label class="form-label" for="duty-loco-no">Loco Number</label>
            <input type="text" id="duty-loco-no" class="form-input" placeholder="e.g. 31456 / 32190" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="duty-signon-date">Sign-On Date</label>
            <input type="date" id="duty-signon-date" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label" for="duty-signon-time">Sign-On Time</label>
            <input type="time" id="duty-signon-time" class="form-input" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="duty-direction">Direction</label>
            <select id="duty-direction" class="form-select">
              <option value="UP">UP (Towards Bilaspur)</option>
              <option value="DN">DN (Towards Raigarh/Jharsuguda)</option>
              <option value="CIC">CIC (Champa - Korba Section)</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label" for="duty-station-code">Current Station Code</label>
            <input type="text" id="duty-station-code" class="form-input" placeholder="e.g. KHS, ROB, BEF, RIG" value="KHS" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="duty-train-position">Train Position</label>
            <select id="duty-train-position" class="form-select">
              <option value="1">Position 1 (Roadside / Loop line)</option>
              <option value="2">Position 2 (Main line / Starter cleared)</option>
              <option value="3">Position 3 (Block Section)</option>
              <option value="4">Position 4 (Signal Outer)</option>
              <option value="5">Position 5 (Yard Waiting)</option>
              <option value="6">Position 6 (Siding Placement)</option>
              <option value="7">Position 7 (Relief / Handover)</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label" for="duty-position-time">Position Timing</label>
            <input type="time" id="duty-position-time" class="form-input" />
          </div>
        </div>

        <button type="submit" class="btn btn-primary btn-full" style="background: #FFB74D; color: #060D18; font-weight: 800; box-shadow: 0 4px 14px rgba(255, 183, 77, 0.35);">
          <span>Register Active Duty</span>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
        </button>
      </form>
    </div>

    <!-- 3. Completed Duties Section -->
    <div id="section-long-history" style="display: none; flex-direction: column; gap: 12px;">
      <div id="completed-duties-container" style="display: flex; flex-direction: column; gap: 12px;"></div>
    </div>
  `;

  container.appendChild(content);

  // Set default dates
  const todayStr = new Date().toISOString().split('T')[0];
  const nowTime = new Date().toTimeString().slice(0, 5);
  content.querySelector('#duty-signon-date').value = todayStr;
  content.querySelector('#duty-signon-time').value = nowTime;
  content.querySelector('#duty-position-time').value = nowTime;

  // Auto-fetch for LPG and ALP
  const lpgId = content.querySelector('#duty-lpg-id');
  const lpgName = content.querySelector('#duty-lpg-name');
  lpgId.addEventListener('input', (e) => {
    const term = e.target.value.trim();
    if (term.length >= 3) {
      const match = store.findCrewByIdOrName(term);
      if (match) lpgName.value = match.name;
    }
  });

  const alpId = content.querySelector('#duty-alp-id');
  const alpName = content.querySelector('#duty-alp-name');
  alpId.addEventListener('input', (e) => {
    const term = e.target.value.trim();
    if (term.length >= 3) {
      const match = store.findCrewByIdOrName(term);
      if (match) alpName.value = match.name;
    }
  });

  // Calculate elapsed duty hours
  function calculateHours(signOnDate, signOnTime) {
    try {
      let parts = (signOnDate || '').split('-');
      // Support dd-mm-yyyy or yyyy-mm-dd
      let d, m, y;
      if (parts[0].length === 4) {
        y = parseInt(parts[0]); m = parseInt(parts[1]) - 1; d = parseInt(parts[2]);
      } else {
        d = parseInt(parts[0]); m = parseInt(parts[1]) - 1; y = parseInt(parts[2]);
      }
      const [hh, mm] = (signOnTime || '00:00').split(':').map(Number);
      const signOnDateTime = new Date(y, m, d, hh, mm);
      const diffMs = Date.now() - signOnDateTime.getTime();
      const diffMins = Math.max(0, Math.floor(diffMs / (1000 * 60)));
      const hrs = Math.floor(diffMins / 60);
      const mins = diffMins % 60;
      return { totalHours: hrs + mins / 60, display: `${hrs}h ${mins}m` };
    } catch {
      return { totalHours: 0, display: '0h' };
    }
  }

  // Handle Form Submit
  const formDuty = content.querySelector('#form-duty-register');
  formDuty.addEventListener('submit', (e) => {
    e.preventDefault();
    const record = {
      lpgId: lpgId.value.trim(),
      lpgName: lpgName.value.trim(),
      alpId: alpId.value.trim(),
      alpName: alpName.value.trim(),
      trainNo: content.querySelector('#duty-train-no').value.trim(),
      locoNo: content.querySelector('#duty-loco-no').value.trim(),
      signOnDate: content.querySelector('#duty-signon-date').value,
      signOnTime: content.querySelector('#duty-signon-time').value,
      direction: content.querySelector('#duty-direction').value,
      currentStationCode: content.querySelector('#duty-station-code').value.trim().toUpperCase(),
      currentTrainPosition: content.querySelector('#duty-train-position').value,
      positionTiming: content.querySelector('#duty-position-time').value
    };

    store.addLongHourRecord(record);
    showToast(`Registered duty for Train ${record.trainNo}`);
    formDuty.reset();
    content.querySelector('#duty-signon-date').value = todayStr;
    content.querySelector('#duty-signon-time').value = nowTime;
    content.querySelector('#duty-position-time').value = nowTime;
    switchTab('active');
  });

  // Tab switching
  const tabActive = content.querySelector('#tab-long-active');
  const tabRegister = content.querySelector('#tab-long-register');
  const tabHistory = content.querySelector('#tab-long-history');
  const secActive = content.querySelector('#section-long-active');
  const secRegister = content.querySelector('#section-long-register');
  const secHistory = content.querySelector('#section-long-history');
  const countBadge = content.querySelector('#count-active-duties');

  function updateBadge() {
    const list = store.getLongHourRecords().filter(r => !r.isClosed);
    countBadge.textContent = list.length;
  }
  updateBadge();

  function switchTab(tab) {
    [tabActive, tabRegister, tabHistory].forEach(t => t.classList.remove('active'));
    secActive.style.display = 'none';
    secRegister.style.display = 'none';
    secHistory.style.display = 'none';

    if (tab === 'active') {
      tabActive.classList.add('active');
      secActive.style.display = 'flex';
      renderActiveDuties();
    } else if (tab === 'register') {
      tabRegister.classList.add('active');
      secRegister.style.display = 'block';
    } else if (tab === 'history') {
      tabHistory.classList.add('active');
      secHistory.style.display = 'flex';
      renderCompletedDuties();
    }
    updateBadge();
  }

  tabActive.addEventListener('click', () => switchTab('active'));
  tabRegister.addEventListener('click', () => switchTab('register'));
  tabHistory.addEventListener('click', () => switchTab('history'));

  // Render Active Duties
  function renderActiveDuties() {
    const container = content.querySelector('#active-duties-container');
    container.innerHTML = '';
    const records = store.getLongHourRecords().filter(r => !r.isClosed);

    if (records.length === 0) {
      container.innerHTML = `
        <div class="card" style="text-align: center; padding: 40px 20px;">
          <p style="color: #90A4AE; font-size: 15px;">No active long hour duties currently tracked.</p>
        </div>
      `;
      return;
    }

    records.forEach(r => {
      const duration = calculateHours(r.signOnDate, r.signOnTime);
      const isUrgent = duration.totalHours >= 11;
      const isWarning = duration.totalHours >= 9 && duration.totalHours < 11;

      const card = document.createElement('div');
      card.className = 'card';
      if (isUrgent) {
        card.style.borderColor = '#E74C3C';
        card.style.boxShadow = '0 0 16px rgba(231, 76, 60, 0.3)';
      } else if (isWarning) {
        card.style.borderColor = '#FFB74D';
      }

      card.innerHTML = `
        <div style="display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 10px;">
          <div>
            <div style="display: flex; align-items: center; gap: 8px;">
              <h4 style="font-size: 18px; font-weight: 800; color: #FFFFFF;">Train: ${r.trainNo}</h4>
              <span style="font-size: 12px; background: #13273F; color: #64B5F6; padding: 2px 8px; border-radius: 4px; font-weight: 700;">
                Loco ${r.locoNo}
              </span>
              <span style="font-size: 11px; background: rgba(241, 183, 72, 0.15); color: #F1B748; padding: 2px 8px; border-radius: 4px; font-weight: 700;">
                ${r.direction}
              </span>
            </div>
            <p style="font-size: 13px; color: #B0BEC5; margin-top: 2px;">
              Station: <strong>${r.currentStationCode}</strong> • Pos ${r.currentTrainPosition} (${r.positionTiming || 'N/A'})
            </p>
          </div>

          <div style="text-align: right;">
            <div style="font-size: 20px; font-weight: 800; color: ${isUrgent ? '#E74C3C' : isWarning ? '#FFB74D' : '#00E676'};">
              ${duration.display}
            </div>
            <span style="font-size: 10px; font-weight: 700; text-transform: uppercase; color: ${isUrgent ? '#E74C3C' : isWarning ? '#FFB74D' : '#90A4AE'};">
              ${isUrgent ? 'RELIEF URGENT' : isWarning ? 'LONG HOUR' : 'RUNNING'}
            </span>
          </div>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)); gap: 8px; padding: 10px 12px; background: rgba(7, 18, 31, 0.6); border-radius: var(--radius-sm); font-size: 12px; margin-bottom: 12px;">
          <div>
            <span style="color: #607D8B; display: block;">LPG (Loco Pilot)</span>
            <strong style="color: #FFFFFF;">${r.lpgName}</strong>
            <span style="color: #64B5F6; display: block; font-size: 10px;">${r.lpgId}</span>
          </div>
          <div>
            <span style="color: #607D8B; display: block;">ALP (Assistant LP)</span>
            <strong style="color: #FFFFFF;">${r.alpName}</strong>
            <span style="color: #64B5F6; display: block; font-size: 10px;">${r.alpId}</span>
          </div>
          <div>
            <span style="color: #607D8B; display: block;">Sign-On</span>
            <strong style="color: #FFFFFF;">${r.signOnDate} ${r.signOnTime}</strong>
          </div>
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 8px;">
          <button class="btn btn-outline btn-sm btn-close-duty" data-id="${r.id}" style="color: #FFB74D; border-color: #FFB74D;">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right: 4px;">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
            Relieve Crew / Close Duty
          </button>
        </div>
      `;

      card.querySelector('.btn-close-duty').addEventListener('click', () => {
        promptAdminPin(() => {
          const stn = prompt('Enter Relief Station Code (e.g. RIG, KHS, BSP):', r.currentStationCode || 'KHS');
          if (!stn) return;
          const time = prompt('Enter Relief Time (HH:MM):', new Date().toTimeString().slice(0, 5));
          if (!time) return;
          store.closeLongHourDuty(r.id, stn, time);
          showToast(`Duty for Train ${r.trainNo} relieved at ${stn}`);
          renderActiveDuties();
          updateBadge();
        }, 'Enter In-Charge PIN to approve relief and close duty:');
      });

      container.appendChild(card);
    });
  }

  // Render Completed Duties
  function renderCompletedDuties() {
    const container = content.querySelector('#completed-duties-container');
    container.innerHTML = '';
    const records = store.getLongHourRecords().filter(r => r.isClosed);

    if (records.length === 0) {
      container.innerHTML = `<div class="card" style="text-align: center; padding: 40px;"><p style="color: #90A4AE;">No completed duties recorded.</p></div>`;
      return;
    }

    records.forEach(r => {
      const card = document.createElement('div');
      card.className = 'card';
      card.style.padding = '14px 16px';
      card.innerHTML = `
        <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px;">
          <strong style="color: #FFFFFF; font-size: 15px;">Train ${r.trainNo} (Loco ${r.locoNo})</strong>
          <span class="status-pill status-confirmed">CLOSED</span>
        </div>
        <p style="font-size: 12px; color: #B0BEC5;">Crew: ${r.lpgName} (${r.lpgId}) & ${r.alpName} (${r.alpId})</p>
        <p style="font-size: 12px; color: #00E676; margin-top: 2px;">
          Relieved at <strong>${r.reliefStationCode}</strong> on ${r.reliefDate || ''} ${r.reliefTime || ''}
        </p>
      `;
      container.appendChild(card);
    });
  }

  renderActiveDuties();
  return container;
}
