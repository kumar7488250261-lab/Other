// Admin In-Charge PIN Verification Modal
import { store } from '../store.js';
import { showToast } from './Toast.js';

export function promptAdminPin(onSuccess, promptText = 'Enter 4-Digit In-Charge PIN to proceed:') {
  if (store.isAdminSessionActive()) {
    onSuccess();
    return;
  }

  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay';
  overlay.innerHTML = `
    <div class="modal-card">
      <h3 class="modal-title">Lobby In-Charge Authorization</h3>
      <p class="modal-desc">${promptText}</p>
      <div class="form-group">
        <label class="form-label">PIN CODE (Default: 1234)</label>
        <input type="password" id="admin-pin-input" class="form-input" maxlength="6" placeholder="Enter PIN (e.g. 1234)" autofocus />
      </div>
      <div style="display: flex; gap: 10px; justify-content: flex-end; margin-top: 18px;">
        <button type="button" class="btn btn-outline btn-sm" id="admin-pin-cancel">Cancel</button>
        <button type="button" class="btn btn-primary btn-sm" id="admin-pin-submit">Authorize</button>
      </div>
    </div>
  `;

  document.body.appendChild(overlay);

  const pinInput = overlay.querySelector('#admin-pin-input');
  const cancelBtn = overlay.querySelector('#admin-pin-cancel');
  const submitBtn = overlay.querySelector('#admin-pin-submit');

  const close = () => overlay.remove();

  cancelBtn.addEventListener('click', close);
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) close();
  });

  const verify = () => {
    const val = pinInput.value.trim();
    if (store.verifyAdminPin(val)) {
      showToast('Supervisor Authorization Verified');
      close();
      onSuccess();
    } else {
      alert('Invalid PIN code! Please use default PIN 1234 or configured PIN.');
      pinInput.value = '';
      pinInput.focus();
    }
  };

  submitBtn.addEventListener('click', verify);
  pinInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') verify();
  });
}
