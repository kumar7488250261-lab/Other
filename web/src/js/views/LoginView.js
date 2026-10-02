// Login & User Registration View - Authentication & Approval Workflow
import { store } from '../store.js';
import { renderHeader } from '../components/Header.js';
import { showToast } from '../components/Toast.js';

export function renderLoginView() {
  const container = document.createElement('div');
  container.className = 'fade-in';

  const header = renderHeader({
    title: 'Staff Authentication',
    subtitle: 'SECR Kharsia Lobby Multi-Client Access',
    showBack: true,
    onBack: () => window.location.hash = '#/welcome'
  });
  container.appendChild(header);

  const content = document.createElement('div');
  content.className = 'app-content';

  let isRegisterTab = false;
  let otpSent = false;
  let simulatedOtp = '7788';

  function render() {
    content.innerHTML = `
      <div class="card" style="max-width: 440px; margin: 20px auto; border-color: #2D466E;">
        <!-- Tab Selector: Login / Register -->
        <div style="display: flex; border-bottom: 2px solid #1E293B; margin-bottom: 20px;">
          <button id="tab-login" class="tab-btn ${!isRegisterTab ? 'active' : ''}" style="flex: 1; text-align: center; justify-content: center;">
            <span>Staff Sign In</span>
          </button>
          <button id="tab-register" class="tab-btn ${isRegisterTab ? 'active' : ''}" style="flex: 1; text-align: center; justify-content: center;">
            <span>Register Access</span>
          </button>
        </div>

        ${!isRegisterTab ? `
          <!-- Sign In Form -->
          <form id="form-login">
            <div class="form-group" style="margin-bottom: 14px;">
              <label class="form-label">Staff / Crew ID or Mobile</label>
              <input type="text" id="l-crew-id" class="input-field" placeholder="Enter Crew ID or Mobile" required style="font-family: var(--font-mono); text-transform: uppercase;">
              <small style="color: #64748B; font-size: 11px;">Enter your registered Crew ID or Mobile number</small>
            </div>

            <div class="form-group" style="margin-bottom: 14px;">
              <label class="form-label">Password / PIN</label>
              <input type="password" id="l-password" class="input-field" placeholder="Enter Password / PIN" required>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 10px;">
              Authenticate Staff
            </button>
          </form>
        ` : `
          <!-- Register Request Form -->
          <form id="form-register">
            <p style="font-size: 12px; color: #94A3B8; margin-bottom: 14px;">
              New staff registrations require Chief Crew Controller (CCC) approval before accessing running lobby records.
            </p>

            <div class="form-group" style="margin-bottom: 12px;">
              <label class="form-label">Crew ID (Auto-verifies with Master)</label>
              <input type="text" id="r-crew-id" class="input-field" placeholder="e.g. KHS1042" required style="font-family: var(--font-mono); text-transform: uppercase;">
              <div id="r-match-badge" style="font-size: 12px; color: #10B981; margin-top: 4px; font-weight: 600;"></div>
            </div>

            <div class="form-group" style="margin-bottom: 12px;">
              <label class="form-label">Full Name</label>
              <input type="text" id="r-name" class="input-field" placeholder="Staff Name" required>
            </div>

            <div class="form-group" style="margin-bottom: 12px;">
              <label class="form-label">Mobile Number</label>
              <div style="display: flex; gap: 8px;">
                <input type="tel" id="r-mobile" class="input-field" placeholder="10-digit Mobile / CUG" required style="font-family: var(--font-mono);">
                <button type="button" id="btn-send-otp" class="btn btn-outline" style="white-space: nowrap; font-size: 12px;">
                  ${otpSent ? 'Resend OTP' : 'Send OTP'}
                </button>
              </div>
            </div>

            ${otpSent ? `
              <div class="form-group fade-in" style="margin-bottom: 12px; background: rgba(16, 185, 129, 0.1); padding: 10px; border-radius: 6px; border: 1px dashed #10B981;">
                <label class="form-label" style="color: #10B981;">Enter 4-Digit Verification Code (OTP)</label>
                <input type="text" id="r-otp" class="input-field" placeholder="Enter ${simulatedOtp}" value="${simulatedOtp}" required style="font-family: var(--font-mono); text-align: center; letter-spacing: 4px; font-size: 16px;">
                <small style="color: #34D399; font-size: 11px;">Verification OTP delivered: ${simulatedOtp}</small>
              </div>
            ` : ''}

            <div class="form-group" style="margin-bottom: 16px;">
              <label class="form-label">Requested Role</label>
              <select id="r-role" class="input-field" style="background:#090E17; color:#FFF;">
                <option value="STAFF">STAFF (Running LP/ALP/Guard)</option>
                <option value="SUPERVISOR">SUPERVISOR (Lobby Desk)</option>
                <option value="ROSTER_UPDATER">ROSTER_UPDATER</option>
              </select>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%;">
              Submit Registration for Approval
            </button>
          </form>
        `}
      </div>
    `;

    // Tab bindings
    content.querySelector('#tab-login')?.addEventListener('click', () => { isRegisterTab = false; render(); });
    content.querySelector('#tab-register')?.addEventListener('click', () => { isRegisterTab = true; render(); });

    // Login submit
    content.querySelector('#form-login')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const crewId = content.querySelector('#l-crew-id').value.trim();
      const password = content.querySelector('#l-password').value.trim();

      const res = await store.login({ crewId, password });
      if (res.success) {
        showToast(`Welcome ${res.user.name || crewId}!`);
        window.location.hash = '#/dashboard';
      } else if (res.pending) {
        alert(res.error);
      } else {
        alert(res.error || 'Authentication failed. Please verify credentials.');
      }
    });

    // Register auto-fetch
    const rCrewId = content.querySelector('#r-crew-id');
    const rName = content.querySelector('#r-name');
    const badge = content.querySelector('#r-match-badge');

    rCrewId?.addEventListener('input', () => {
      const q = rCrewId.value.trim();
      const match = store.findCrewByIdOrName(q);
      if (match) {
        badge.textContent = `✓ Found in Master: ${match.name} (${match.designation})`;
        if (!rName.value) rName.value = match.name;
      } else {
        badge.textContent = '';
      }
    });

    content.querySelector('#btn-send-otp')?.addEventListener('click', () => {
      const mob = content.querySelector('#r-mobile').value.trim();
      if (!mob || mob.length < 10) {
        alert('Please enter a valid 10-digit mobile number first.');
        return;
      }
      otpSent = true;
      showToast(`Verification code sent to ${mob}`);
      render();
    });

    // Register submit
    content.querySelector('#form-register')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const crewId = rCrewId.value.trim().toUpperCase();
      const name = rName.value.trim();
      const mobile = content.querySelector('#r-mobile').value.trim();
      const role = content.querySelector('#r-role').value;

      await store.register({ crewId, name, mobile, role });
      alert(`Registration submitted for ${crewId} (${name}). Status is PENDING. The Chief Crew Controller (CCC) Kharsia must review and approve your profile before login.`);
      isRegisterTab = false;
      render();
    });
  }

  render();
  container.appendChild(content);
  return container;
}
