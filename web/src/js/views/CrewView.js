// Crew Master & Other Lobbies Directory View
import { store } from '../store.js';
import { renderHeader } from '../components/Header.js';

export function renderCrewView() {
  const container = document.createElement('div');
  container.className = 'fade-in';

  const header = renderHeader({
    title: 'Staff & Crew Directory',
    subtitle: 'SECR Bilaspur Division • Category-Wise Directory',
    showBack: true,
    onBack: () => window.location.hash = '#/dashboard'
  });
  container.appendChild(header);

  const content = document.createElement('div');
  content.className = 'app-content';

  let currentTab = 'other'; // 'kharsia' or 'other'
  let selectedCategory = 'ALL'; // ALL, LP, ALP, GUARD
  let selectedOtherLobby = 'ALL';
  let selectedOtherCategory = 'ALL';
  let searchQuery = '';

  function render() {
    const allCrew = store.getAllCrew();
    const otherLobbies = store.getOtherLobbies();
    const allOtherCrew = store.getAllOtherCrew();

    // Counts for Kharsia
    const countLP = allCrew.filter(c => c.category === 'LP').length;
    const countALP = allCrew.filter(c => c.category === 'ALP').length;
    const countGuard = allCrew.filter(c => c.category === 'GUARD').length;

    // Filter Kharsia
    const filteredKharsia = allCrew.filter(c => {
      if (selectedCategory !== 'ALL' && c.category !== selectedCategory) return false;
      if (searchQuery) {
        const q = searchQuery.toLowerCase();
        return (c.crewId && c.crewId.toLowerCase().includes(q)) ||
               (c.name && c.name.toLowerCase().includes(q)) ||
               (c.designation && c.designation.toLowerCase().includes(q)) ||
               (c.category && c.category.toLowerCase().includes(q));
      }
      return true;
    });

    // Filter Other Lobbies
    const filteredOther = allOtherCrew.filter(c => {
      if (selectedOtherLobby !== 'ALL' && c.lobbyCode !== selectedOtherLobby) return false;
      if (selectedOtherCategory !== 'ALL') {
        const cat = c.category.toLowerCase();
        const sel = selectedOtherCategory.toLowerCase();
        if (sel === 'lp (goods)' && !cat.includes('goods')) return false;
        else if (sel === 'alp' && (!cat.includes('alp') && !cat.includes('assistant'))) return false;
        else if (sel === 'tm / guard' && (!cat.includes('guard') && !cat.includes('manager'))) return false;
        else if (sel === 'shunting' && !cat.includes('shunting')) return false;
        else if (sel === 'passenger' && !cat.includes('passenger')) return false;
        else if (sel === 'cli' && !cat.includes('cli')) return false;
        else if (sel === 'ccc' && (!cat.includes('ccc') && !cat.includes('controlling'))) return false;
      }
      if (searchQuery) {
        const q = searchQuery.toLowerCase();
        return (c.name && c.name.toLowerCase().includes(q)) ||
               (c.mobile && c.mobile.includes(q)) ||
               (c.designation && c.designation.toLowerCase().includes(q)) ||
               (c.lobbyCode && c.lobbyCode.toLowerCase().includes(q)) ||
               (c.category && c.category.toLowerCase().includes(q));
      }
      return true;
    });

    content.innerHTML = `
      <!-- Top Level Directory Tabs -->
      <div style="display: flex; gap: 8px; margin-bottom: 16px; border-bottom: 2px solid #1E293B; padding-bottom: 8px;">
        <button class="btn btn-sm ${currentTab === 'other' ? 'btn-primary' : 'btn-outline'}" id="btn-tab-other" style="font-weight: 700;">
          Other Lobbies (${allOtherCrew.length} Crew)
        </button>
        <button class="btn btn-sm ${currentTab === 'kharsia' ? 'btn-primary' : 'btn-outline'}" id="btn-tab-kharsia" style="font-weight: 700;">
          Kharsia Lobby (${allCrew.length})
        </button>
      </div>

      <!-- Search Input -->
      <div style="margin-bottom: 16px;">
        <input type="text" 
               id="input-crew-search" 
               class="input-field" 
               placeholder="Search by Name, Mobile number, Designation, or Lobby code..." 
               value="${searchQuery}" 
               style="font-family: var(--font-mono);">
      </div>

      ${currentTab === 'other' ? `
        <!-- Other Lobbies View -->
        <div style="margin-bottom: 14px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
            <span style="font-size: 12px; font-weight: 700; color: #F59E0B; text-transform: uppercase;">Select Lobby:</span>
            ${selectedOtherLobby !== 'ALL' ? `<button id="btn-clear-lobby" style="background: none; border: none; color: #38BDF8; font-size: 11px; cursor: pointer; text-decoration: underline;">Clear Lobby</button>` : ''}
          </div>
          <div style="display: flex; gap: 6px; overflow-x: auto; padding-bottom: 8px; scrollbar-width: thin;">
            <button class="btn btn-sm ${selectedOtherLobby === 'ALL' ? 'btn-primary' : 'btn-outline'}" style="white-space: nowrap; font-size: 11.5px; padding: 4px 10px;" data-lobby="ALL">
              All Lobbies (${allOtherCrew.length})
            </button>
            ${otherLobbies.map(l => {
              const count = (l.categories || []).reduce((acc, c) => acc + (c.contacts ? c.contacts.length : 0), 0);
              return `
                <button class="btn btn-sm ${selectedOtherLobby === l.code ? 'btn-primary' : 'btn-outline'}" style="white-space: nowrap; font-size: 11.5px; padding: 4px 10px;" data-lobby="${l.code}">
                  ${l.code} (${count})
                </button>
              `;
            }).join('')}
          </div>
        </div>

        <!-- Category Wise Selector -->
        <div style="margin-bottom: 16px;">
          <div style="font-size: 12px; font-weight: 700; color: #94A3B8; text-transform: uppercase; margin-bottom: 6px;">
            Category Wise Filter (श्रेणी अनुसार):
          </div>
          <div style="display: flex; gap: 6px; overflow-x: auto; padding-bottom: 8px; scrollbar-width: thin;">
            ${[
              { id: 'ALL', label: 'All' },
              { id: 'LP (Goods)', label: 'LP (Goods)' },
              { id: 'ALP', label: 'ALP' },
              { id: 'TM / Guard', label: 'TM / Guard' },
              { id: 'Shunting', label: 'Shunting' },
              { id: 'Passenger', label: 'LP (Pass)' },
              { id: 'CLI', label: 'CLI' },
              { id: 'CCC', label: 'CCC' }
            ].map(c => `
              <button class="btn btn-sm ${selectedOtherCategory === c.id ? 'btn-primary' : 'btn-outline'}" style="white-space: nowrap; font-size: 11px; padding: 4px 8px;" data-other-cat="${c.id}">
                ${c.label}
              </button>
            `).join('')}
          </div>
        </div>

        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
          <span style="font-size: 12px; font-weight: 700; color: #F59E0B;">
            Showing ${filteredOther.length} Crew Members
          </span>
          <span style="font-size: 11px; color: #94A3B8;">
            ${selectedOtherLobby !== 'ALL' ? `Lobby: ${selectedOtherLobby}` : 'All 12 Lobbies'}
          </span>
        </div>

        <!-- Crew Cards Grid / List -->
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 10px;">
          ${filteredOther.length === 0 ? `
            <div class="card" style="padding: 30px; text-align: center; color: #94A3B8; grid-column: 1 / -1;">
              No crew members match the selected lobby, category, or search filter.
            </div>
          ` : filteredOther.slice(0, 300).map(c => `
            <div class="card" style="padding: 12px; display: flex; justify-content: space-between; align-items: center; gap: 10px; background: #0E1726; border-color: #1E293B;">
              <div>
                <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 3px;">
                  <span style="font-weight: 700; color: #FFFFFF; font-size: 13.5px;">${c.name}</span>
                  <span class="id-pill" style="font-size: 10px; padding: 1px 5px;">${c.lobbyCode}</span>
                </div>
                <div style="font-size: 11.5px; color: #94A3B8; margin-bottom: 4px;">
                  ${c.category}
                </div>
                ${c.mobile ? `
                  <a href="tel:${c.mobile}" style="font-family: var(--font-mono); font-size: 12px; color: #00E676; text-decoration: none; font-weight: 600;">
                    📞 ${c.mobile}
                  </a>
                ` : `<span style="font-size: 11px; color: #64748B;">No Phone</span>`}
              </div>
              ${c.mobile ? `
                <a href="tel:${c.mobile}" class="btn btn-sm btn-primary" style="padding: 6px 12px; font-size: 11px; background: #00E676; border-color: #00E676; color: #000000; font-weight: 700; text-decoration: none; border-radius: 6px;">
                  CALL
                </a>
              ` : ''}
            </div>
          `).join('')}
        </div>
        ${filteredOther.length > 300 ? `
          <div style="margin-top: 14px; text-align: center; color: #94A3B8; font-size: 12px;">
            Showing first 300 crew members. Use search or category filter to narrow down results.
          </div>
        ` : ''}
      ` : `
        <!-- Kharsia Crew View -->
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; margin-bottom: 16px;">
          <div style="display: flex; gap: 8px; flex-wrap: wrap;">
            <button class="btn btn-sm ${selectedCategory === 'ALL' ? 'btn-primary' : 'btn-outline'}" id="c-cat-all">All (${allCrew.length})</button>
            <button class="btn btn-sm ${selectedCategory === 'LP' ? 'btn-primary' : 'btn-outline'}" id="c-cat-lp">Loco Pilots (${countLP})</button>
            <button class="btn btn-sm ${selectedCategory === 'ALP' ? 'btn-primary' : 'btn-outline'}" id="c-cat-alp">ALPs (${countALP})</button>
            <button class="btn btn-sm ${selectedCategory === 'GUARD' ? 'btn-primary' : 'btn-outline'}" id="c-cat-guard">Train Managers (${countGuard})</button>
          </div>

          <span style="font-size: 12px; color: #94A3B8; font-family: var(--font-mono);">
            Showing ${filteredKharsia.length} of ${allCrew.length} crew
          </span>
        </div>

        <!-- Crew Table Container -->
        <div class="card" style="padding: 0; overflow: hidden; border-color: #1E293B;">
          <div style="overflow-x: auto;">
            <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 13.5px;">
              <thead>
                <tr style="background: #090E17; border-bottom: 1px solid #1E293B; color: #F59E0B; font-family: var(--font-mono); font-size: 11.5px; text-transform: uppercase;">
                  <th style="padding: 12px 16px;">Crew ID</th>
                  <th style="padding: 12px 16px;">Full Name</th>
                  <th style="padding: 12px 16px;">Designation</th>
                  <th style="padding: 12px 16px;">Category</th>
                  <th style="padding: 12px 16px;">Cadre</th>
                </tr>
              </thead>
              <tbody>
                ${filteredKharsia.length === 0 ? `
                  <tr><td colspan="5" style="padding: 30px; text-align: center; color: #94A3B8;">No crew members match your search.</td></tr>
                ` : filteredKharsia.slice(0, 100).map(c => `
                  <tr style="border-bottom: 1px solid #111A29; transition: background 0.1s ease;">
                    <td style="padding: 10px 16px;">
                      <span class="id-pill">${c.crewId}</span>
                    </td>
                    <td style="padding: 10px 16px; font-weight: 600; color: #FFFFFF;">
                      ${c.name}
                    </td>
                    <td style="padding: 10px 16px; color: #94A3B8; font-size: 12.5px;">
                      ${c.designation}
                    </td>
                    <td style="padding: 10px 16px;">
                      <span class="role-pill">${c.category}</span>
                    </td>
                    <td style="padding: 10px 16px; color: #64748B; font-size: 12px;">
                      ${c.cadre || 'Running (Loco)'}
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
          ${filteredKharsia.length > 100 ? `
            <div style="padding: 10px; text-align: center; color: #94A3B8; font-size: 12px; background: #090E17; border-top: 1px solid #1E293B;">
              Showing first 100 results. Type in search to narrow down.
            </div>
          ` : ''}
        </div>
      `}
    `;

    // Bind tab buttons
    content.querySelector('#btn-tab-other')?.addEventListener('click', () => { currentTab = 'other'; render(); });
    content.querySelector('#btn-tab-kharsia')?.addEventListener('click', () => { currentTab = 'kharsia'; render(); });

    // Bind other lobby selector buttons
    content.querySelectorAll('[data-lobby]')?.forEach(btn => {
      btn.addEventListener('click', () => {
        selectedOtherLobby = btn.getAttribute('data-lobby');
        selectedOtherCategory = 'ALL';
        render();
      });
    });

    content.querySelector('#btn-clear-lobby')?.addEventListener('click', () => {
      selectedOtherLobby = 'ALL';
      render();
    });

    // Bind other category buttons
    content.querySelectorAll('[data-other-cat]')?.forEach(btn => {
      btn.addEventListener('click', () => {
        selectedOtherCategory = btn.getAttribute('data-other-cat');
        render();
      });
    });

    // Kharsia category filters
    content.querySelector('#c-cat-all')?.addEventListener('click', () => { selectedCategory = 'ALL'; render(); });
    content.querySelector('#c-cat-lp')?.addEventListener('click', () => { selectedCategory = 'LP'; render(); });
    content.querySelector('#c-cat-alp')?.addEventListener('click', () => { selectedCategory = 'ALP'; render(); });
    content.querySelector('#c-cat-guard')?.addEventListener('click', () => { selectedCategory = 'GUARD'; render(); });

    // Search input
    content.querySelector('#input-crew-search')?.addEventListener('input', (e) => {
      searchQuery = e.target.value.trim();
      render();
    });
  }

  render();
  container.appendChild(content);
  return container;
}
