// Jeep Movement & FIFO Availability Screen
import { store, KharsiaStore } from '../store.js';
import { renderHeader } from '../components/Header.js';
import { showToast } from '../components/Toast.js';

export function renderJeepMovementView() {
  const container = document.createElement('div');
  container.className = 'fade-in';

  const header = renderHeader({
    title: 'Jeep movement',
    subtitle: 'Jeep Availability & Entry (जीप उपलब्धता एवं एंट्री)',
    showBack: true,
    onBack: () => window.location.hash = '#/menu'
  });
  container.appendChild(header);

  const content = document.createElement('div');
  content.className = 'app-content';

  content.innerHTML = `
    <!-- Tabs Header -->
    <div class="tabs-header">
      <button class="tab-btn active" id="tab-jeep-avail">Jeep Availability (FIFO Turns)</button>
      <button class="tab-btn" id="tab-jeep-entry">Movement Entry</button>
      <button class="tab-btn" id="tab-jeep-log">Movement Logs</button>
    </div>

    <!-- 1. Jeep Availability Tab -->
    <div id="section-jeep-avail" style="display: flex; flex-direction: column; gap: 12px;">
      <div style="background: rgba(255, 64, 129, 0.1); border: 1px solid rgba(255, 64, 129, 0.3); border-radius: var(--radius-md); padding: 12px 16px; margin-bottom: 6px; font-size: 13px; color: #FF80AB; display: flex; align-items: center; gap: 10px;">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="8" x2="12" y2="12"></line>
          <line x1="12" y1="16" x2="12.01" y2="16"></line>
        </svg>
        <span>Turn order is automatically managed First-In-First-Out (FIFO) based on Kharsia Lobby arrival time.</span>
      </div>

      <div id="jeeps-grid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 14px;"></div>
    </div>

    <!-- 2. Movement Entry Tab -->
    <div id="section-jeep-entry" class="card" style="display: none; margin-bottom: 20px;">
      <h3 style="font-size: 17px; font-weight: 700; color: #FF4081; margin-bottom: 16px; display: flex; align-items: center; gap: 8px;">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.5 2.8C2.1 11.2 2 11.6 2 12v4c0 .6.4 1 1 1h2"></path>
          <circle cx="7" cy="17" r="2"></circle>
          <circle cx="17" cy="17" r="2"></circle>
        </svg>
        <span>Jeep Movement Entry Form (आउटवर्ड एवं रिटर्न)</span>
      </h3>

      <form id="form-jeep-movement">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="entry-jeep-no">Select Jeep No.</label>
            <select id="entry-jeep-no" class="form-select" required>
              ${KharsiaStore.CORE_JEEPS.map(j => `<option value="${j}">Jeep No. ${j}</option>`).join('')}
            </select>
          </div>

          <div class="form-group">
            <label class="form-label" for="entry-driver-name">Driver Name</label>
            <input type="text" id="entry-driver-name" class="form-input" placeholder="Driver Name" required />
          </div>
        </div>

        <!-- Outward Journey Header -->
        <h4 style="font-size: 14px; font-weight: 700; color: #64B5F6; margin: 16px 0 10px; border-bottom: 1px solid #1E3A5F; padding-bottom: 6px;">
          1. OUTWARD JOURNEY (जाने की यात्रा)
        </h4>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="out-from-stn">From Station</label>
            <select id="out-from-stn" class="form-select">
              ${KharsiaStore.STATIONS.map(s => `<option value="${s.code}" ${s.code === 'KHS' ? 'selected' : ''}>${s.name} (${s.code})</option>`).join('')}
            </select>
          </div>

          <div class="form-group">
            <label class="form-label" for="out-to-stn">To Station</label>
            <select id="out-to-stn" class="form-select">
              ${KharsiaStore.STATIONS.map(s => `<option value="${s.code}" ${s.code === 'ROB' ? 'selected' : ''}>${s.name} (${s.code})</option>`).join('')}
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="out-dep-time">Departure Time</label>
            <input type="time" id="out-dep-time" class="form-input" required />
          </div>

          <div class="form-group">
            <label class="form-label" for="out-arr-time">Arrival Time (At Destination)</label>
            <input type="time" id="out-arr-time" class="form-input" />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label" for="out-relief-time">Crew Relief Time</label>
          <input type="time" id="out-relief-time" class="form-input" />
        </div>

        <!-- Outward Crew Slots (7 Slots) -->
        <div style="margin: 14px 0;">
          <label class="form-label" style="margin-bottom: 8px;">Outward Crew Slots (7 Slots: Empty / Crew / Other)</label>
          <div id="outward-slots-container" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 8px;">
            ${[1, 2, 3, 4, 5, 6, 7].map(slot => `
              <div style="background: rgba(7, 18, 31, 0.7); border: 1px solid #1E3A5F; border-radius: var(--radius-sm); padding: 8px;">
                <span style="font-size: 10px; color: #64B5F6; font-weight: 700; display: block;">Slot ${slot}</span>
                <input type="text" class="form-input outward-slot-input" placeholder="Empty (खाली) or Crew ID" style="min-height: 38px; padding: 6px 10px; font-size: 12px; margin-top: 4px;" />
              </div>
            `).join('')}
          </div>
        </div>

        <!-- Returning Journey Header -->
        <h4 style="font-size: 14px; font-weight: 700; color: #00E676; margin: 20px 0 10px; border-bottom: 1px solid #1E3A5F; padding-bottom: 6px;">
          2. RETURNING JOURNEY (लौटने की यात्रा)
        </h4>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="ret-from-stn">Returning From</label>
            <select id="ret-from-stn" class="form-select">
              ${KharsiaStore.STATIONS.map(s => `<option value="${s.code}" ${s.code === 'ROB' ? 'selected' : ''}>${s.name} (${s.code})</option>`).join('')}
            </select>
          </div>

          <div class="form-group">
            <label class="form-label" for="ret-to-stn">Returning To (Lobby)</label>
            <select id="ret-to-stn" class="form-select">
              <option value="KHS" selected>Kharsia Lobby (KHS)</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="ret-dep-time">Return Departure Time</label>
            <input type="time" id="ret-dep-time" class="form-input" />
          </div>

          <div class="form-group">
            <label class="form-label" for="ret-arr-time">Return Arrival At KHS</label>
            <input type="time" id="ret-arr-time" class="form-input" />
          </div>
        </div>

        <!-- Returning Crew Slots (7 Slots) -->
        <div style="margin: 14px 0;">
          <label class="form-label" style="margin-bottom: 8px;">Returning Crew Slots (7 Slots: Empty / Crew / Other)</label>
          <div id="returning-slots-container" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 8px;">
            ${[1, 2, 3, 4, 5, 6, 7].map(slot => `
              <div style="background: rgba(7, 18, 31, 0.7); border: 1px solid #1E3A5F; border-radius: var(--radius-sm); padding: 8px;">
                <span style="font-size: 10px; color: #00E676; font-weight: 700; display: block;">Slot ${slot}</span>
                <input type="text" class="form-input returning-slot-input" placeholder="Empty (खाली) or Crew ID" style="min-height: 38px; padding: 6px 10px; font-size: 12px; margin-top: 4px;" />
              </div>
            `).join('')}
          </div>
        </div>

        <button type="submit" class="btn btn-primary btn-full" style="background: #FF4081; box-shadow: 0 4px 14px rgba(255, 64, 129, 0.35); font-weight: 800; margin-top: 10px;">
          <span>Save Jeep Movement Entry</span>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
        </button>
      </form>
    </div>

    <!-- 3. Movement Logs Tab -->
    <div id="section-jeep-log" style="display: none; flex-direction: column; gap: 12px;">
      <div id="movement-logs-container" style="display: flex; flex-direction: column; gap: 12px;"></div>
    </div>
  `;

  container.appendChild(content);

  // Set default times
  const nowTime = new Date().toTimeString().slice(0, 5);
  content.querySelector('#out-dep-time').value = nowTime;

  // Tabs Management
  const tabAvail = content.querySelector('#tab-jeep-avail');
  const tabEntry = content.querySelector('#tab-jeep-entry');
  const tabLog = content.querySelector('#tab-jeep-log');
  const secAvail = content.querySelector('#section-jeep-avail');
  const secEntry = content.querySelector('#section-jeep-entry');
  const secLog = content.querySelector('#section-jeep-log');

  function switchTab(tab) {
    [tabAvail, tabEntry, tabLog].forEach(t => t.classList.remove('active'));
    secAvail.style.display = 'none';
    secEntry.style.display = 'none';
    secLog.style.display = 'none';

    if (tab === 'avail') {
      tabAvail.classList.add('active');
      secAvail.style.display = 'flex';
      renderJeepAvailability();
    } else if (tab === 'entry') {
      tabEntry.classList.add('active');
      secEntry.style.display = 'block';
    } else if (tab === 'log') {
      tabLog.classList.add('active');
      secLog.style.display = 'flex';
      renderMovementLogs();
    }
  }

  tabAvail.addEventListener('click', () => switchTab('avail'));
  tabEntry.addEventListener('click', () => switchTab('entry'));
  tabLog.addEventListener('click', () => switchTab('log'));

  // Render Jeep Availability Cards
  function renderJeepAvailability() {
    const grid = content.querySelector('#jeeps-grid');
    grid.innerHTML = '';
    const jeeps = store.getJeepAvailability();

    jeeps.forEach(j => {
      const card = document.createElement('div');
      card.className = 'card';
      const isBreakdown = j.jeepNo === 'Breakdown';

      card.innerHTML = `
        <div style="display: flex; align-items: flex-start; justify-content: space-between; gap: 10px; margin-bottom: 12px;">
          <div style="display: flex; align-items: center; gap: 10px;">
            <div style="width: 46px; height: 46px; border-radius: 12px; background: rgba(255, 64, 129, 0.15); color: #FF4081; display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 16px;">
              ${j.jeepNo}
            </div>
            <div>
              <h4 style="font-size: 17px; font-weight: 800; color: #FFFFFF;">Jeep ${j.jeepNo}</h4>
              <span style="font-size: 12px; color: #B0BEC5;">Driver: <strong>${j.driverName}</strong></span>
            </div>
          </div>

          ${j.turnNumber ? `
            <span style="background: rgba(241, 183, 72, 0.15); color: #F1B748; border: 1px solid rgba(241, 183, 72, 0.4); padding: 4px 10px; border-radius: var(--radius-pill); font-size: 12px; font-weight: 800;">
              TURN ${j.turnNumber}
            </span>
          ` : `
            <span class="status-pill ${isBreakdown ? 'status-notdue' : j.isAvailable ? 'status-confirmed' : 'status-pending'}">
              ${isBreakdown ? 'BREAKDOWN' : j.isAvailable ? 'AVAILABLE' : 'ON MOVEMENT'}
            </span>
          `}
        </div>

        <div style="background: rgba(7, 18, 31, 0.6); border-radius: var(--radius-sm); padding: 10px 12px; font-size: 12px; margin-bottom: 12px;">
          <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
            <span style="color: #607D8B;">Current Location</span>
            <strong style="color: #64B5F6;">${j.currentLocation}</strong>
          </div>
          <div style="display: flex; justify-content: space-between;">
            <span style="color: #607D8B;">Last Recorded Arrival</span>
            <strong style="color: #FFFFFF;">${j.lastArrivalDate} ${j.lastArrivalTime}</strong>
          </div>
        </div>

        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 11px; color: #90A4AE;">${j.statusDescription}</span>
          ${!isBreakdown ? `
            <button class="btn btn-outline btn-sm btn-select-jeep" data-jeep="${j.jeepNo}" style="padding: 6px 12px; font-size: 12px;">
              New Movement &rarr;
            </button>
          ` : ''}
        </div>
      `;

      const selBtn = card.querySelector('.btn-select-jeep');
      if (selBtn) {
        selBtn.addEventListener('click', () => {
          content.querySelector('#entry-jeep-no').value = j.jeepNo;
          content.querySelector('#entry-driver-name').value = j.driverName !== 'Regular Staff' ? j.driverName : '';
          switchTab('entry');
        });
      }

      grid.appendChild(card);
    });
  }

  // Handle Movement Form Submit
  const formJeep = content.querySelector('#form-jeep-movement');
  formJeep.addEventListener('submit', (e) => {
    e.preventDefault();
    const outwardSlots = Array.from(content.querySelectorAll('.outward-slot-input')).map(i => i.value.trim() || 'Empty');
    const returningSlots = Array.from(content.querySelectorAll('.returning-slot-input')).map(i => i.value.trim() || 'Empty');

    const today = new Date().toLocaleDateString('en-GB');

    const record = {
      jeepNo: content.querySelector('#entry-jeep-no').value,
      driverName: content.querySelector('#entry-driver-name').value.trim(),
      fromStation: content.querySelector('#out-from-stn').value,
      toStation: content.querySelector('#out-to-stn').value,
      departureDate: today,
      departureTime: content.querySelector('#out-dep-time').value,
      arrivalDate: today,
      arrivalTime: content.querySelector('#out-arr-time').value,
      reliefTime: content.querySelector('#out-relief-time').value,
      outwardCrews: outwardSlots,
      returningFromStation: content.querySelector('#ret-from-stn').value,
      returningToStation: content.querySelector('#ret-to-stn').value,
      returningDepartureDate: today,
      returningDepartureTime: content.querySelector('#ret-dep-time').value,
      returningArrivalDate: today,
      returningArrivalTime: content.querySelector('#ret-arr-time').value,
      returningCrews: returningSlots
    };

    store.addJeepMovement(record);
    showToast(`Movement recorded for Jeep No. ${record.jeepNo}`);
    formJeep.reset();
    switchTab('avail');
  });

  // Render Movement Logs
  function renderMovementLogs() {
    const container = content.querySelector('#movement-logs-container');
    container.innerHTML = '';
    const list = store.getJeepMovements();

    if (list.length === 0) {
      container.innerHTML = `<div class="card" style="text-align: center; padding: 40px;"><p style="color: #90A4AE;">No jeep movements recorded yet.</p></div>`;
      return;
    }

    list.forEach(m => {
      const card = document.createElement('div');
      card.className = 'card';
      card.style.padding = '14px 16px';
      card.innerHTML = `
        <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px;">
          <strong style="color: #FFFFFF; font-size: 16px;">Jeep ${m.jeepNo} • Driver: ${m.driverName}</strong>
          <span style="font-size: 11px; background: rgba(255, 64, 129, 0.15); color: #FF4081; padding: 2px 8px; border-radius: 4px; font-weight: 700;">
            ${m.fromStation} &rarr; ${m.toStation}
          </span>
        </div>
        <p style="font-size: 12px; color: #B0BEC5;">
          Departed: ${m.departureDate} ${m.departureTime} ${m.arrivalTime ? `• Arrived: ${m.arrivalTime}` : ''}
        </p>
        ${m.returningArrivalTime ? `
          <p style="font-size: 12px; color: #00E676; margin-top: 2px;">
            Returned from ${m.returningFromStation} to KHS at ${m.returningArrivalTime}
          </p>
        ` : ''}
      `;
      container.appendChild(card);
    });
  }

  renderJeepAvailability();
  return container;
}
