// Roaster & TLC Update Screen
import { store } from '../store.js';
import { renderHeader } from '../components/Header.js';
import { showToast } from '../components/Toast.js';
import { promptAdminPin } from '../components/AdminPinModal.js';

export function renderRosterTlcView() {
  const container = document.createElement('div');
  container.className = 'fade-in';

  const header = renderHeader({
    title: 'Roaster & TLC update',
    subtitle: 'Shift Wise Roaster & TLC Portal',
    showBack: true,
    onBack: () => window.location.hash = '#/menu'
  });
  container.appendChild(header);

  const content = document.createElement('div');
  content.className = 'app-content';

  let selectedShift = '06-14';
  const todayStr = new Date().toLocaleDateString('en-GB');

  content.innerHTML = `
    <!-- Shift Selector Tabs -->
    <div style="display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; justify-content: space-between;">
      <div style="display: flex; gap: 8px;">
        <button class="tab-btn active btn-shift" data-shift="06-14" style="background: rgba(79, 195, 247, 0.15); border-radius: var(--radius-pill); border: 1px solid rgba(79, 195, 247, 0.4);">
          Shift 06:00 - 14:00
        </button>
        <button class="tab-btn btn-shift" data-shift="14-22" style="border-radius: var(--radius-pill); border: 1px solid var(--railway-card-border);">
          Shift 14:00 - 22:00
        </button>
        <button class="tab-btn btn-shift" data-shift="22-06" style="border-radius: var(--radius-pill); border: 1px solid var(--railway-card-border);">
          Shift 22:00 - 06:00
        </button>
      </div>

      <button id="btn-edit-roster" class="btn btn-outline btn-sm" style="color: #4FC3F7; border-color: #4FC3F7;">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right: 4px;">
          <path d="M12 20h9"></path>
          <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
        </svg>
        Admin Portal Entry
      </button>
    </div>

    <!-- Roster Display Board -->
    <div id="roster-board-container" class="card card-gold-border" style="padding: 22px;"></div>

    <!-- Admin Entry Modal Form (Hidden by default) -->
    <div id="roster-modal" class="modal-overlay" style="display: none;">
      <div class="modal-card" style="max-width: 600px; max-height: 90vh; overflow-y: auto;">
        <h3 class="modal-title" style="color: #4FC3F7;">Update Shift Roaster & TLC</h3>
        <p class="modal-desc">Kharsia Lobby Operational Shift Handover</p>

        <form id="form-roster-update">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Shift Timing</label>
              <select id="modal-shift" class="form-select">
                <option value="06-14">06:00 - 14:00</option>
                <option value="14-22">14:00 - 22:00</option>
                <option value="22-06">22:00 - 06:00</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Date (DD/MM/YYYY)</label>
              <input type="text" id="modal-date" class="form-input" value="${todayStr}" required />
            </div>
          </div>

          <h4 style="font-size: 13px; color: #F1B748; margin: 12px 0 6px; text-transform: uppercase;">1. Lobby Crew Roles</h4>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">TFR Crew Name & Mobile</label>
              <input type="text" id="modal-tfr-name" class="form-input" placeholder="TFR Crew Name" />
              <input type="tel" id="modal-tfr-mobile" class="form-input" placeholder="TFR Mobile No" style="margin-top: 4px;" />
            </div>
            <div class="form-group">
              <label class="form-label">LH Crew Name & Mobile</label>
              <input type="text" id="modal-lh-name" class="form-input" placeholder="LH Crew Name" />
              <input type="tel" id="modal-lh-mobile" class="form-input" placeholder="LH Mobile No" style="margin-top: 4px;" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">DI Crew Name & Mobile</label>
              <input type="text" id="modal-di-name" class="form-input" placeholder="DI Crew Name" />
              <input type="tel" id="modal-di-mobile" class="form-input" placeholder="DI Mobile No" style="margin-top: 4px;" />
            </div>
            <div class="form-group">
              <label class="form-label">WD Crew Name & Mobile</label>
              <input type="text" id="modal-wd-name" class="form-input" placeholder="WD Crew Name" />
              <input type="tel" id="modal-wd-mobile" class="form-input" placeholder="WD Mobile No" style="margin-top: 4px;" />
            </div>
          </div>

          <h4 style="font-size: 13px; color: #F1B748; margin: 12px 0 6px; text-transform: uppercase;">2. CMS, CLI & Staff</h4>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">CMS Operator Name</label>
              <input type="text" id="modal-cms-name" class="form-input" placeholder="CMS Name" />
            </div>
            <div class="form-group">
              <label class="form-label">Lobby CLI Name & Mobile</label>
              <input type="text" id="modal-cli-name" class="form-input" placeholder="CLI Name" />
              <input type="tel" id="modal-cli-mobile" class="form-input" placeholder="CLI Mobile" style="margin-top: 4px;" />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">Sander Boy Name</label>
            <input type="text" id="modal-sander-name" class="form-input" placeholder="Sander Staff Name" />
          </div>

          <h4 style="font-size: 13px; color: #F1B748; margin: 12px 0 6px; text-transform: uppercase;">3. Bilaspur TLC (Control)</h4>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">TLC ML (Main Line) Name & Mob</label>
              <input type="text" id="modal-tlc-ml-name" class="form-input" placeholder="TLC ML" />
              <input type="tel" id="modal-tlc-ml-mobile" class="form-input" placeholder="TLC ML Mobile" style="margin-top: 4px;" />
            </div>
            <div class="form-group">
              <label class="form-label">TLC LH (Long Haul) Name & Mob</label>
              <input type="text" id="modal-tlc-lh-name" class="form-input" placeholder="TLC LH" />
              <input type="tel" id="modal-tlc-lh-mobile" class="form-input" placeholder="TLC LH Mobile" style="margin-top: 4px;" />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">Shift Handover Remarks</label>
            <textarea id="modal-remarks" class="form-textarea" rows="2" placeholder="Operational notes, train line up, power status..."></textarea>
          </div>

          <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 16px;">
            <button type="button" id="btn-cancel-modal" class="btn btn-outline btn-sm">Cancel</button>
            <button type="submit" class="btn btn-primary btn-sm" style="background: #4FC3F7; color: #060D18; font-weight: 800;">Save Roaster</button>
          </div>
        </form>
      </div>
    </div>
  `;

  container.appendChild(content);

  const boardContainer = content.querySelector('#roster-board-container');
  const shiftBtns = content.querySelectorAll('.btn-shift');

  function renderBoard() {
    const list = store.getRosterTlcRecords(null, selectedShift);
    const data = list.length > 0 ? list[0] : null;

    if (!data) {
      boardContainer.innerHTML = `
        <div style="text-align: center; padding: 30px;">
          <p style="color: #90A4AE; font-size: 15px; margin-bottom: 12px;">No roaster entry recorded for Shift ${selectedShift}</p>
          <button class="btn btn-primary btn-sm" id="btn-create-roster-entry" style="background: #4FC3F7; color: #060D18; font-weight: 800;">
            + Create Shift Roaster Entry
          </button>
        </div>
      `;
      boardContainer.querySelector('#btn-create-roster-entry').addEventListener('click', openModal);
      return;
    }

    boardContainer.innerHTML = `
      <div style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #1E4D7A; padding-bottom: 14px; margin-bottom: 18px;">
        <div>
          <h3 style="font-size: 19px; font-weight: 800; color: #FFFFFF;">
            Shift: ${data.shiftTiming}
          </h3>
          <span style="font-size: 12px; color: #F1B748; font-weight: 700;">
            DATE: ${data.rosterDate || todayStr} • KHS COMBINED LOBBY
          </span>
        </div>
        <span class="status-pill status-confirmed">OPERATIONAL</span>
      </div>

      <!-- Staff Grid -->
      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 14px; margin-bottom: 18px;">
        <!-- TFR Crew -->
        <div style="background: rgba(7, 18, 31, 0.7); border: 1px solid #1E3A5F; border-radius: var(--radius-md); padding: 12px 14px;">
          <span style="font-size: 11px; color: #4FC3F7; font-weight: 800; display: block; text-transform: uppercase;">TFR (Traffic Relief)</span>
          <strong style="color: #FFFFFF; font-size: 15px;">${data.tfrCrewName || 'Not Assigned'}</strong>
          ${data.tfrMobile ? `<a href="tel:${data.tfrMobile}" style="display: block; font-size: 12px; color: #00E676; text-decoration: none; margin-top: 2px;">📞 ${data.tfrMobile}</a>` : ''}
        </div>

        <!-- LH Crew -->
        <div style="background: rgba(7, 18, 31, 0.7); border: 1px solid #1E3A5F; border-radius: var(--radius-md); padding: 12px 14px;">
          <span style="font-size: 11px; color: #FFB74D; font-weight: 800; display: block; text-transform: uppercase;">LH (Long Haul)</span>
          <strong style="color: #FFFFFF; font-size: 15px;">${data.lhCrewName || 'Not Assigned'}</strong>
          ${data.lhMobile ? `<a href="tel:${data.lhMobile}" style="display: block; font-size: 12px; color: #00E676; text-decoration: none; margin-top: 2px;">📞 ${data.lhMobile}</a>` : ''}
        </div>

        <!-- DI Crew -->
        <div style="background: rgba(7, 18, 31, 0.7); border: 1px solid #1E3A5F; border-radius: var(--radius-md); padding: 12px 14px;">
          <span style="font-size: 11px; color: #BA68C8; font-weight: 800; display: block; text-transform: uppercase;">DI (Diesel / Inspection)</span>
          <strong style="color: #FFFFFF; font-size: 15px;">${data.diCrewName || 'Not Assigned'}</strong>
          ${data.diMobile ? `<a href="tel:${data.diMobile}" style="display: block; font-size: 12px; color: #00E676; text-decoration: none; margin-top: 2px;">📞 ${data.diMobile}</a>` : ''}
        </div>

        <!-- WD Crew -->
        <div style="background: rgba(7, 18, 31, 0.7); border: 1px solid #1E3A5F; border-radius: var(--radius-md); padding: 12px 14px;">
          <span style="font-size: 11px; color: #00E676; font-weight: 800; display: block; text-transform: uppercase;">WD (Working / Depot)</span>
          <strong style="color: #FFFFFF; font-size: 15px;">${data.wdCrewName || 'Not Assigned'}</strong>
          ${data.wdMobile ? `<a href="tel:${data.wdMobile}" style="display: block; font-size: 12px; color: #00E676; text-decoration: none; margin-top: 2px;">📞 ${data.wdMobile}</a>` : ''}
        </div>

        <!-- CMS Operator -->
        <div style="background: rgba(7, 18, 31, 0.7); border: 1px solid #1E3A5F; border-radius: var(--radius-md); padding: 12px 14px;">
          <span style="font-size: 11px; color: #90CAF9; font-weight: 800; display: block; text-transform: uppercase;">CMS Operator</span>
          <strong style="color: #FFFFFF; font-size: 15px;">${data.cmsName || 'Not Assigned'}</strong>
        </div>

        <!-- Lobby CLI -->
        <div style="background: rgba(7, 18, 31, 0.7); border: 1px solid #1E3A5F; border-radius: var(--radius-md); padding: 12px 14px;">
          <span style="font-size: 11px; color: #F1B748; font-weight: 800; display: block; text-transform: uppercase;">Lobby CLI</span>
          <strong style="color: #FFFFFF; font-size: 15px;">${data.lobbyCliName || 'Not Assigned'}</strong>
          ${data.lobbyCliMobile ? `<a href="tel:${data.lobbyCliMobile}" style="display: block; font-size: 12px; color: #00E676; text-decoration: none; margin-top: 2px;">📞 ${data.lobbyCliMobile}</a>` : ''}
        </div>

        <!-- Sander Boy -->
        <div style="background: rgba(7, 18, 31, 0.7); border: 1px solid #1E3A5F; border-radius: var(--radius-md); padding: 12px 14px;">
          <span style="font-size: 11px; color: #90A4AE; font-weight: 800; display: block; text-transform: uppercase;">Sander Staff</span>
          <strong style="color: #FFFFFF; font-size: 15px;">${data.sanderBoyName || 'Not Assigned'}</strong>
        </div>

        <!-- TLC Bilaspur -->
        <div style="background: rgba(7, 18, 31, 0.7); border: 1px solid #1E3A5F; border-radius: var(--radius-md); padding: 12px 14px;">
          <span style="font-size: 11px; color: #64B5F6; font-weight: 800; display: block; text-transform: uppercase;">TLC Bilaspur</span>
          <strong style="color: #FFFFFF; font-size: 14px;">${data.tlcMlName || 'TLC ML'} / ${data.tlcLhName || 'TLC LH'}</strong>
          ${data.tlcMlMobile ? `<a href="tel:${data.tlcMlMobile}" style="display: block; font-size: 12px; color: #00E676; text-decoration: none; margin-top: 2px;">📞 ${data.tlcMlMobile}</a>` : ''}
        </div>
      </div>

      <!-- Remarks Box -->
      ${data.remarks ? `
        <div style="background: rgba(241, 183, 72, 0.1); border: 1px solid rgba(241, 183, 72, 0.3); border-radius: var(--radius-sm); padding: 12px 14px; font-size: 13px; color: #F1B748;">
          <strong>Shift Handover Log:</strong> ${data.remarks}
        </div>
      ` : ''}
    `;
  }

  // Handle Shift Selection
  shiftBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      shiftBtns.forEach(b => {
        b.classList.remove('active');
        b.style.background = 'transparent';
        b.style.borderColor = 'var(--railway-card-border)';
      });
      btn.classList.add('active');
      btn.style.background = 'rgba(79, 195, 247, 0.15)';
      btn.style.borderColor = 'rgba(79, 195, 247, 0.4)';
      selectedShift = btn.getAttribute('data-shift');
      renderBoard();
    });
  });

  // Modal Handling
  const modal = content.querySelector('#roster-modal');
  const btnEdit = content.querySelector('#btn-edit-roster');
  const btnCancelModal = content.querySelector('#btn-cancel-modal');
  const formRoster = content.querySelector('#form-roster-update');

  function openModal() {
    promptAdminPin(() => {
      modal.style.display = 'flex';
      content.querySelector('#modal-shift').value = selectedShift;
      const list = store.getRosterTlcRecords(null, selectedShift);
      const cur = list.length > 0 ? list[0] : {};
      content.querySelector('#modal-tfr-name').value = cur.tfrCrewName || '';
      content.querySelector('#modal-tfr-mobile').value = cur.tfrMobile || '';
      content.querySelector('#modal-lh-name').value = cur.lhCrewName || '';
      content.querySelector('#modal-lh-mobile').value = cur.lhMobile || '';
      content.querySelector('#modal-di-name').value = cur.diCrewName || '';
      content.querySelector('#modal-di-mobile').value = cur.diMobile || '';
      content.querySelector('#modal-wd-name').value = cur.wdCrewName || '';
      content.querySelector('#modal-wd-mobile').value = cur.wdMobile || '';
      content.querySelector('#modal-cms-name').value = cur.cmsName || '';
      content.querySelector('#modal-cli-name').value = cur.lobbyCliName || '';
      content.querySelector('#modal-cli-mobile').value = cur.lobbyCliMobile || '';
      content.querySelector('#modal-sander-name').value = cur.sanderBoyName || '';
      content.querySelector('#modal-tlc-ml-name').value = cur.tlcMlName || '';
      content.querySelector('#modal-tlc-ml-mobile').value = cur.tlcMlMobile || '';
      content.querySelector('#modal-tlc-lh-name').value = cur.tlcLhName || '';
      content.querySelector('#modal-tlc-lh-mobile').value = cur.tlcLhMobile || '';
      content.querySelector('#modal-remarks').value = cur.remarks || '';
    }, 'Enter In-Charge PIN to edit Roaster & TLC entries:');
  }

  btnEdit.addEventListener('click', openModal);
  btnCancelModal.addEventListener('click', () => modal.style.display = 'none');

  formRoster.addEventListener('submit', (e) => {
    e.preventDefault();
    const updated = {
      shiftTiming: content.querySelector('#modal-shift').value,
      rosterDate: content.querySelector('#modal-date').value.trim(),
      tfrCrewName: content.querySelector('#modal-tfr-name').value.trim(),
      tfrMobile: content.querySelector('#modal-tfr-mobile').value.trim(),
      lhCrewName: content.querySelector('#modal-lh-name').value.trim(),
      lhMobile: content.querySelector('#modal-lh-mobile').value.trim(),
      diCrewName: content.querySelector('#modal-di-name').value.trim(),
      diMobile: content.querySelector('#modal-di-mobile').value.trim(),
      wdCrewName: content.querySelector('#modal-wd-name').value.trim(),
      wdMobile: content.querySelector('#modal-wd-mobile').value.trim(),
      cmsName: content.querySelector('#modal-cms-name').value.trim(),
      lobbyCliName: content.querySelector('#modal-cli-name').value.trim(),
      lobbyCliMobile: content.querySelector('#modal-cli-mobile').value.trim(),
      sanderBoyName: content.querySelector('#modal-sander-name').value.trim(),
      tlcMlName: content.querySelector('#modal-tlc-ml-name').value.trim(),
      tlcMlMobile: content.querySelector('#modal-tlc-ml-mobile').value.trim(),
      tlcLhName: content.querySelector('#modal-tlc-lh-name').value.trim(),
      tlcLhMobile: content.querySelector('#modal-tlc-lh-mobile').value.trim(),
      remarks: content.querySelector('#modal-remarks').value.trim(),
      updatedByAdmin: 'Lobby In-Charge'
    };

    store.saveRosterTlcRecord(updated);
    showToast(`Roaster saved for Shift ${updated.shiftTiming}`);
    modal.style.display = 'none';
    selectedShift = updated.shiftTiming;
    renderBoard();
  });

  renderBoard();
  return container;
}
