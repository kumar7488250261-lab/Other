// Store Register Screen (Equipment Fast Issue & Fast Return)
import { store } from '../store.js';
import { renderHeader } from '../components/Header.js';
import { showToast } from '../components/Toast.js';
import { promptAdminPin } from '../components/AdminPinModal.js';

export function renderStoreRegisterView() {
  const container = document.createElement('div');
  container.className = 'fade-in';

  const header = renderHeader({
    title: 'Store register',
    subtitle: 'Fast Issue & Fast Return / CHO Equipment',
    showBack: true,
    onBack: () => window.location.hash = '#/menu'
  });
  container.appendChild(header);

  const content = document.createElement('div');
  content.className = 'app-content';

  const EQUIPMENT_LIST = [
    'Walkie-Talkie (VHF Set)',
    'Tri-Color LED Torch',
    'Breathalyzer Device',
    'Portable Unit Guard (PUG)',
    'Loco Pilot Tool Kit',
    'Red/Green Hand Signal Flag',
    'Detonator / Fog Signals Box',
    'Speedo-Chart / Memo Pad',
    'Flashing Tail Lamp (LV Lamp)'
  ];

  content.innerHTML = `
    <!-- Tabs Header -->
    <div class="tabs-header">
      <button class="tab-btn active" id="tab-fast-issue">Fast Issue</button>
      <button class="tab-btn" id="tab-fast-return">Fast Return (<span id="count-issued">0</span>)</button>
      <button class="tab-btn" id="tab-store-history">Records Log</button>
      <button class="tab-btn" id="tab-store-supervisor" style="margin-left: auto; color: #BA68C8;">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align: -2px; margin-right: 4px;">
          <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
          <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
        </svg>
        Supervisor Dashboard
      </button>
    </div>

    <!-- 1. Fast Issue Section -->
    <div id="section-fast-issue" class="card" style="margin-bottom: 20px;">
      <h3 style="font-size: 17px; font-weight: 700; color: #BA68C8; margin-bottom: 16px; display: flex; align-items: center; gap: 8px;">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"></path>
        </svg>
        <span>Fast Issue Equipment</span>
      </h3>

      <form id="form-fast-issue">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="issue-equipment-name">Select Equipment</label>
            <select id="issue-equipment-name" class="form-select" required>
              ${EQUIPMENT_LIST.map(e => `<option value="${e}">${e}</option>`).join('')}
            </select>
          </div>

          <div class="form-group">
            <label class="form-label" for="issue-serial-no">Equipment Serial / Asset No.</label>
            <input type="text" id="issue-serial-no" class="form-input" placeholder="e.g. WT-204, TC-88" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="issue-crew-id">Crew ID (Auto-Fetch)</label>
            <input type="text" id="issue-crew-id" class="form-input" placeholder="e.g. KHS1001" required />
            <span id="issue-crew-helper" style="font-size: 11px; color: #BA68C8; margin-top: 2px;">Enter Crew ID to load name & designation</span>
          </div>

          <div class="form-group">
            <label class="form-label" for="issue-crew-name">Issued To (Name)</label>
            <input type="text" id="issue-crew-name" class="form-input" placeholder="Staff Name" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="issue-designation">Designation</label>
            <input type="text" id="issue-designation" class="form-input" placeholder="e.g. LPG / SALP / Guard" required />
          </div>

          <div class="form-group">
            <label class="form-label" for="issue-category">Staff Category</label>
            <select id="issue-category" class="form-select">
              <option value="LP">Loco Pilot (LP)</option>
              <option value="ALP">Assistant Loco Pilot (ALP)</option>
              <option value="Guard">Train Manager / Guard</option>
              <option value="Shunter">Shunter</option>
              <option value="Other">Other Staff</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="issue-train-no">Train Number</label>
            <input type="text" id="issue-train-no" class="form-input" placeholder="e.g. N/BOXN, 12833, BCN" />
          </div>

          <div class="form-group">
            <label class="form-label" for="issue-to-station">Destination Station</label>
            <input type="text" id="issue-to-station" class="form-input" placeholder="e.g. RIG, BSP, CPH" />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label" for="issue-remarks">Condition / Remarks</label>
          <input type="text" id="issue-remarks" class="form-input" placeholder="e.g. Battery 100% charged, Tested OK" />
        </div>

        <button type="submit" class="btn btn-primary btn-full" style="background: #BA68C8; box-shadow: 0 4px 14px rgba(186, 104, 200, 0.35);">
          <span>Submit Fast Issue</span>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
        </button>
      </form>
    </div>

    <!-- 2. Fast Return Section -->
    <div id="section-fast-return" style="display: none; flex-direction: column; gap: 12px;">
      <div id="return-items-container" style="display: flex; flex-direction: column; gap: 12px;"></div>
    </div>

    <!-- 3. Store History Section -->
    <div id="section-store-history" style="display: none; flex-direction: column; gap: 12px;">
      <div id="history-items-container" style="display: flex; flex-direction: column; gap: 12px;"></div>
    </div>
  `;

  container.appendChild(content);

  // Form Elements
  const crewIdInput = content.querySelector('#issue-crew-id');
  const crewNameInput = content.querySelector('#issue-crew-name');
  const designationInput = content.querySelector('#issue-designation');
  const categorySelect = content.querySelector('#issue-category');
  const helper = content.querySelector('#issue-crew-helper');
  const countIssuedBadge = content.querySelector('#count-issued');

  // Auto-fetch crew
  crewIdInput.addEventListener('input', (e) => {
    const term = e.target.value.trim();
    if (term.length >= 3) {
      const match = store.findCrewByIdOrName(term);
      if (match) {
        crewNameInput.value = match.name;
        designationInput.value = match.designation || match.category;
        if (match.category === 'LP') categorySelect.value = 'LP';
        else if (match.category === 'ALP') categorySelect.value = 'ALP';
        else if (match.category?.includes('Guard')) categorySelect.value = 'Guard';
        helper.textContent = `Auto-fetched: ${match.name} (${match.designation})`;
        helper.style.color = '#00E676';
      }
    }
  });

  // Submit Fast Issue
  const formIssue = content.querySelector('#form-fast-issue');
  formIssue.addEventListener('submit', (e) => {
    e.preventDefault();
    const record = {
      equipmentName: content.querySelector('#issue-equipment-name').value,
      equipmentSerialNo: content.querySelector('#issue-serial-no').value.trim(),
      issuedToCrewId: crewIdInput.value.trim(),
      issuedToCrewName: crewNameInput.value.trim(),
      designation: designationInput.value.trim(),
      category: categorySelect.value,
      trainNo: content.querySelector('#issue-train-no').value.trim(),
      fromStation: 'KHS',
      toStation: content.querySelector('#issue-to-station').value.trim(),
      remarks: content.querySelector('#issue-remarks').value.trim()
    };

    store.addStoreIssue(record);
    showToast(`Issued ${record.equipmentName} (${record.equipmentSerialNo}) to ${record.issuedToCrewName}`);
    formIssue.reset();
    helper.textContent = 'Enter Crew ID to load name & designation';
    helper.style.color = '#BA68C8';
    switchTab('return');
  });

  // Tabs Management
  const tabIssue = content.querySelector('#tab-fast-issue');
  const tabReturn = content.querySelector('#tab-fast-return');
  const tabHistory = content.querySelector('#tab-store-history');
  const tabSupervisor = content.querySelector('#tab-store-supervisor');

  const secIssue = content.querySelector('#section-fast-issue');
  const secReturn = content.querySelector('#section-fast-return');
  const secHistory = content.querySelector('#section-store-history');

  function updateCount() {
    const list = store.getStoreRecords();
    const active = list.filter(r => r.status === 'ISSUED').length;
    countIssuedBadge.textContent = active;
  }
  updateCount();

  function switchTab(tab) {
    [tabIssue, tabReturn, tabHistory].forEach(t => t.classList.remove('active'));
    secIssue.style.display = 'none';
    secReturn.style.display = 'none';
    secHistory.style.display = 'none';

    if (tab === 'issue') {
      tabIssue.classList.add('active');
      secIssue.style.display = 'block';
    } else if (tab === 'return') {
      tabReturn.classList.add('active');
      secReturn.style.display = 'flex';
      renderReturnList();
    } else if (tab === 'history') {
      tabHistory.classList.add('active');
      secHistory.style.display = 'flex';
      renderHistoryList();
    }
    updateCount();
  }

  tabIssue.addEventListener('click', () => switchTab('issue'));
  tabReturn.addEventListener('click', () => switchTab('return'));
  tabHistory.addEventListener('click', () => switchTab('history'));

  tabSupervisor.addEventListener('click', () => {
    promptAdminPin(() => {
      switchTab('history');
      showToast('Supervisor mode unlocked: all store logs visible');
    }, 'Enter 4-Digit In-Charge PIN to view Supervisor Store Dashboard:');
  });

  // Render Issued Items for Return
  function renderReturnList() {
    const container = content.querySelector('#return-items-container');
    container.innerHTML = '';
    const records = store.getStoreRecords().filter(r => r.status === 'ISSUED');

    if (records.length === 0) {
      container.innerHTML = `
        <div class="card" style="text-align: center; padding: 40px 20px;">
          <p style="color: #90A4AE; font-size: 15px;">No active issued equipment. All store items accounted for!</p>
        </div>
      `;
      return;
    }

    records.forEach(r => {
      const card = document.createElement('div');
      card.className = 'card';
      card.style.padding = '16px 18px';

      card.innerHTML = `
        <div style="display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 8px;">
          <div>
            <h4 style="font-size: 16px; font-weight: 800; color: #FFFFFF;">${r.equipmentName}</h4>
            <span style="font-size: 12px; color: #BA68C8; font-weight: 700;">
              Serial: ${r.equipmentSerialNo}
            </span>
          </div>
          <span class="status-pill status-issued">ISSUED</span>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)); gap: 8px; padding: 10px 12px; background: rgba(7, 18, 31, 0.6); border-radius: var(--radius-sm); font-size: 12px; margin-bottom: 12px;">
          <div>
            <span style="color: #607D8B; display: block;">Issued To</span>
            <strong style="color: #FFFFFF;">${r.issuedToCrewName} (${r.issuedToCrewId})</strong>
          </div>
          <div>
            <span style="color: #607D8B; display: block;">Issue Date & Time</span>
            <strong style="color: #FFFFFF;">${r.issueDate} ${r.issueTime}</strong>
          </div>
          <div>
            <span style="color: #607D8B; display: block;">Train / Destination</span>
            <strong style="color: #64B5F6;">${r.trainNo || 'Local'} &rarr; ${r.toStation || 'KHS'}</strong>
          </div>
        </div>

        <div style="display: flex; justify-content: flex-end;">
          <button class="btn btn-primary btn-sm btn-return-action" data-id="${r.id}" style="background: #00E676; color: #060D18; font-weight: 800;">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="margin-right: 4px;">
              <polyline points="9 11 12 14 22 4"></polyline>
              <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path>
            </svg>
            Confirm Return Equipment
          </button>
        </div>
      `;

      card.querySelector('.btn-return-action').addEventListener('click', () => {
        const returnNotes = prompt(`Return ${r.equipmentName} (${r.equipmentSerialNo}) from ${r.issuedToCrewName}?\nEnter condition remarks (optional):`, 'Checked OK, undamaged');
        if (returnNotes !== null) {
          const today = new Date().toLocaleDateString('en-GB');
          const time = new Date().toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
          store.returnStoreEquipment(r.id, today, time, returnNotes);
          showToast(`Equipment ${r.equipmentSerialNo} returned successfully`);
          renderReturnList();
          updateCount();
        }
      });

      container.appendChild(card);
    });
  }

  // Render Full History Log
  function renderHistoryList() {
    const container = content.querySelector('#history-items-container');
    container.innerHTML = '';
    const records = store.getStoreRecords();

    if (records.length === 0) {
      container.innerHTML = `<div class="card" style="text-align: center; padding: 40px;"><p style="color: #90A4AE;">No store records yet.</p></div>`;
      return;
    }

    records.forEach(r => {
      const card = document.createElement('div');
      card.className = 'card';
      card.style.padding = '14px 16px';
      const isIssued = r.status === 'ISSUED';

      card.innerHTML = `
        <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px;">
          <div>
            <strong style="color: #FFFFFF; font-size: 15px;">${r.equipmentName}</strong>
            <span style="font-size: 12px; color: #90A4AE; margin-left: 6px;">[${r.equipmentSerialNo}]</span>
          </div>
          <span class="status-pill ${isIssued ? 'status-issued' : 'status-returned'}">${r.status}</span>
        </div>
        <p style="font-size: 12px; color: #B0BEC5;">Issued to <strong>${r.issuedToCrewName}</strong> (${r.issuedToCrewId} - ${r.designation}) on ${r.issueDate} ${r.issueTime}</p>
        ${r.returnDate ? `<p style="font-size: 12px; color: #00E676; margin-top: 2px;">Returned on ${r.returnDate} ${r.returnTime} ${r.remarks ? `| ${r.remarks}` : ''}</p>` : ''}
      `;
      container.appendChild(card);
    });
  }

  return container;
}
