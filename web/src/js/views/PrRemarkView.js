// PR Remark Screen (Periodical Rest)
import { store } from '../store.js';
import { renderHeader } from '../components/Header.js';
import { showToast } from '../components/Toast.js';
import { promptAdminPin } from '../components/AdminPinModal.js';

export function renderPrRemarkView() {
  const container = document.createElement('div');
  container.className = 'fade-in';

  const header = renderHeader({
    title: 'PR remark',
    subtitle: 'Periodical Rest & Sign-off Details',
    showBack: true,
    onBack: () => window.location.hash = '#/menu'
  });
  container.appendChild(header);

  const content = document.createElement('div');
  content.className = 'app-content';

  content.innerHTML = `
    <!-- Tabs Header -->
    <div class="tabs-header">
      <button class="tab-btn active" id="tab-pr-form">Submit PR Request</button>
      <button class="tab-btn" id="tab-pr-list">PR Requests Status (<span id="pr-pending-badge">0</span>)</button>
      <button class="tab-btn" id="tab-pr-admin" style="margin-left: auto; color: #F1B748;">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align: -2px; margin-right: 4px;">
          <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
          <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
        </svg>
        In-Charge Admin
      </button>
    </div>

    <!-- Section 1: Submit PR Form -->
    <div id="pr-form-section" class="card" style="margin-bottom: 20px;">
      <h3 style="font-size: 17px; font-weight: 700; color: #64B5F6; margin-bottom: 16px; display: flex; align-items: center; gap: 8px;">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <polyline points="12 6 12 12 16 14"></polyline>
        </svg>
        <span>Mark Periodical Rest (PR)</span>
      </h3>

      <form id="form-pr-submit">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="pr-crew-id">Crew ID (Auto-Fetch)</label>
            <input 
              type="text" 
              id="pr-crew-id" 
              class="form-input" 
              placeholder="e.g. KHS1001, KHS1003" 
              required 
            />
            <span id="crew-id-helper" style="font-size: 11px; color: #64B5F6; margin-top: 2px;">Enter Crew ID to automatically load details</span>
          </div>

          <div class="form-group">
            <label class="form-label" for="pr-crew-name">Crew Name</label>
            <input 
              type="text" 
              id="pr-crew-name" 
              class="form-input" 
              placeholder="Name of Loco Pilot / Staff" 
              required 
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="pr-designation">Designation & Cadre</label>
            <input 
              type="text" 
              id="pr-designation" 
              class="form-input" 
              placeholder="e.g. LPG - Loco Pilot (Goods)" 
              required 
            />
          </div>

          <div class="form-group">
            <label class="form-label" for="pr-request-date">Request Date</label>
            <input 
              type="date" 
              id="pr-request-date" 
              class="form-input" 
              required 
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="pr-signoff-date">Sign-Off Date</label>
            <input 
              type="date" 
              id="pr-signoff-date" 
              class="form-input" 
              required 
            />
          </div>

          <div class="form-group">
            <label class="form-label" for="pr-signoff-time">Sign-Off Time (HH:MM)</label>
            <input 
              type="time" 
              id="pr-signoff-time" 
              class="form-input" 
              required 
            />
          </div>
        </div>

        <button type="submit" class="btn btn-primary btn-full" style="margin-top: 10px;">
          <span>Submit PR Remark</span>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
        </button>
      </form>
    </div>

    <!-- Section 2: PR Status List -->
    <div id="pr-list-section" style="display: none; flex-direction: column; gap: 12px;">
      <div id="pr-items-container" style="display: flex; flex-direction: column; gap: 12px;"></div>
    </div>
  `;

  container.appendChild(content);

  // Set default dates
  const todayStr = new Date().toISOString().split('T')[0];
  const nowTime = new Date().toTimeString().slice(0, 5);
  content.querySelector('#pr-request-date').value = todayStr;
  content.querySelector('#pr-signoff-date').value = todayStr;
  content.querySelector('#pr-signoff-time').value = nowTime;

  // Auto-fetch crew details
  const crewIdInput = content.querySelector('#pr-crew-id');
  const crewNameInput = content.querySelector('#pr-crew-name');
  const designationInput = content.querySelector('#pr-designation');
  const helper = content.querySelector('#crew-id-helper');

  crewIdInput.addEventListener('input', (e) => {
    const term = e.target.value.trim();
    if (term.length >= 3) {
      const match = store.findCrewByIdOrName(term);
      if (match) {
        crewNameInput.value = match.name;
        designationInput.value = match.designation || match.category;
        helper.textContent = `Found: ${match.name} (${match.designation})`;
        helper.style.color = '#00E676';
      } else {
        helper.textContent = 'Auto-fetch: No exact match found, enter manually';
        helper.style.color = '#90A4AE';
      }
    }
  });

  // Handle PR Form Submission
  const form = content.querySelector('#form-pr-submit');
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const req = {
      crewId: crewIdInput.value.trim(),
      crewName: crewNameInput.value.trim(),
      designation: designationInput.value.trim(),
      requestDate: content.querySelector('#pr-request-date').value,
      signOffDate: content.querySelector('#pr-signoff-date').value,
      signOffTime: content.querySelector('#pr-signoff-time').value
    };

    store.addPrRequest(req);
    showToast(`PR Remark submitted for ${req.crewName} (${req.crewId})`);
    form.reset();
    content.querySelector('#pr-request-date').value = todayStr;
    content.querySelector('#pr-signoff-date').value = todayStr;
    content.querySelector('#pr-signoff-time').value = nowTime;
    helper.textContent = 'Enter Crew ID to automatically load details';
    helper.style.color = '#64B5F6';

    // Switch to status tab
    switchTab('list');
  });

  // Tabs Management
  const tabForm = content.querySelector('#tab-pr-form');
  const tabList = content.querySelector('#tab-pr-list');
  const tabAdmin = content.querySelector('#tab-pr-admin');
  const formSection = content.querySelector('#pr-form-section');
  const listSection = content.querySelector('#pr-list-section');
  const pendingBadge = content.querySelector('#pr-pending-badge');

  function updateBadge() {
    const list = store.getPrRequests();
    const pending = list.filter(p => p.status === 'Pending').length;
    pendingBadge.textContent = pending;
  }
  updateBadge();

  function switchTab(tab) {
    if (tab === 'form') {
      tabForm.classList.add('active');
      tabList.classList.remove('active');
      formSection.style.display = 'block';
      listSection.style.display = 'none';
    } else {
      tabList.classList.add('active');
      tabForm.classList.remove('active');
      formSection.style.display = 'none';
      listSection.style.display = 'flex';
      renderPrList();
    }
  }

  tabForm.addEventListener('click', () => switchTab('form'));
  tabList.addEventListener('click', () => switchTab('list'));

  tabAdmin.addEventListener('click', () => {
    promptAdminPin(() => {
      switchTab('list');
      renderPrList(true);
    }, 'Enter 4-Digit In-Charge PIN to manage and review PR remarks:');
  });

  function renderPrList(isAdmin = false) {
    const itemsContainer = content.querySelector('#pr-items-container');
    itemsContainer.innerHTML = '';
    const list = store.getPrRequests();
    updateBadge();

    if (list.length === 0) {
      itemsContainer.innerHTML = `
        <div class="card" style="text-align: center; padding: 40px 20px;">
          <p style="color: #90A4AE;">No PR remarks found</p>
        </div>
      `;
      return;
    }

    list.forEach(item => {
      const card = document.createElement('div');
      card.className = 'card';
      card.style.padding = '16px 18px';

      let statusClass = 'status-pending';
      if (item.status === 'Confirmed') statusClass = 'status-confirmed';
      if (item.status === 'Not Due') statusClass = 'status-notdue';

      card.innerHTML = `
        <div style="display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 8px;">
          <div>
            <div style="display: flex; align-items: center; gap: 8px;">
              <h4 style="font-size: 16px; font-weight: 800; color: #FFFFFF;">${item.crewName}</h4>
              <span style="font-size: 11px; background: rgba(41, 121, 255, 0.15); color: #64B5F6; padding: 2px 8px; border-radius: 4px; font-weight: 700;">
                ${item.crewId}
              </span>
            </div>
            <p style="font-size: 12px; color: #90A4AE; font-weight: 600; margin-top: 2px;">
              ${item.designation}
            </p>
          </div>
          <span class="status-pill ${statusClass}">${item.status}</span>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)); gap: 8px; padding: 10px 12px; background: rgba(7, 18, 31, 0.6); border-radius: var(--radius-sm); font-size: 12px; margin-bottom: 8px;">
          <div>
            <span style="color: #607D8B; display: block;">Sign-off Time</span>
            <strong style="color: #FFFFFF;">${item.signOffDate} ${item.signOffTime}</strong>
          </div>
          <div>
            <span style="color: #607D8B; display: block;">Request Date</span>
            <strong style="color: #FFFFFF;">${item.requestDate}</strong>
          </div>
          ${item.reviewedBy ? `
            <div>
              <span style="color: #607D8B; display: block;">Reviewed By</span>
              <strong style="color: #00E676;">${item.reviewedBy}</strong>
            </div>
          ` : ''}
        </div>

        ${item.remarks ? `
          <div style="font-size: 12px; color: #F1B748; background: rgba(241, 183, 72, 0.1); padding: 6px 10px; border-radius: 4px; margin-bottom: 8px;">
            <strong>Remarks:</strong> ${item.remarks}
          </div>
        ` : ''}

        <!-- Admin Actions if authenticated or clicked -->
        <div style="display: flex; gap: 8px; justify-content: flex-end; margin-top: 8px;">
          <button class="btn btn-outline btn-sm btn-action-review" data-id="${item.id}">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right: 4px;">
              <path d="M12 20h9"></path>
              <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
            </svg>
            Review / Update PR
          </button>
        </div>
      `;

      // Review Action Button
      card.querySelector('.btn-action-review').addEventListener('click', () => {
        promptAdminPin(() => {
          const newStatus = prompt(`Update Status for ${item.crewName} (${item.crewId}):\nEnter 1 for Confirmed (Granted)\nEnter 2 for Not Due\nEnter 3 for Pending`, '1');
          let mappedStatus = item.status;
          if (newStatus === '1') mappedStatus = 'Confirmed';
          else if (newStatus === '2') mappedStatus = 'Not Due';
          else if (newStatus === '3') mappedStatus = 'Pending';
          else return;

          const remarks = prompt('Enter Admin remarks / period (e.g. PR Granted for 30 hours):', item.remarks || 'PR Granted for 30 hrs');
          store.reviewPrRequest(item.id, mappedStatus, remarks || '', 'Lobby In-Charge');
          showToast(`PR updated to ${mappedStatus}`);
          renderPrList(true);
        });
      });

      itemsContainer.appendChild(card);
    });
  }

  return container;
}
