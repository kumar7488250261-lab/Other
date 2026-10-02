// Kharsia Lobby - Central Data Store & Firebase Sync Service
// Real-time synchronization between Android Application & Responsive Web Application
import { 
  db, 
  collection, 
  doc, 
  getDoc, 
  getDocs, 
  setDoc, 
  updateDoc, 
  deleteDoc, 
  onSnapshot, 
  query, 
  orderBy, 
  limit,
  serverTimestamp 
} from './firebase.js';

const STORAGE_KEYS = {
  AUTH: 'kharsia_user_session',
  ADMIN_AUTH: 'kharsia_admin_session',
  CACHED_DUTIES: 'kharsia_cached_duties',
  CACHED_ROSTER: 'kharsia_cached_roster',
  CACHED_USERS: 'kharsia_cached_users',
  CACHED_NOTICES: 'kharsia_cached_notices',
  CACHED_STORE: 'kharsia_cached_store',
  CACHED_JEEP: 'kharsia_cached_jeep'
};

export class KharsiaStore {
  constructor() {
    this.crewMaster = [];
    this.isLoaded = false;
    this.syncStatus = navigator.onLine ? 'ONLINE' : 'OFFLINE';
    this.syncListeners = new Set();
    
    // In-memory real-time state caches
    this.dutyRecords = [];
    this.rosterRecords = [];
    this.usersList = [];
    this.notifications = [];
    this.storeRecords = [];
    this.jeepMovements = [];
    this.prRequests = [];
    this.auditLogs = [];
    this.lobbies = [];
    this.appConfig = {
      latestVersion: '1.0',
      minVersion: '1.0',
      longHourThresholdHours: 9.0,
      urgentReliefHours: 11.0,
      latestApkUrl: 'https://github.com/abhishekused/Newapk/releases'
    };

    // Subscriptions
    this.dutySubscribers = new Set();
    this.rosterSubscribers = new Set();
    this.userSubscribers = new Set();
    this.notificationSubscribers = new Set();

    this._initNetworkMonitoring();
  }

  _initNetworkMonitoring() {
    window.addEventListener('online', () => {
      this.setSyncStatus('ONLINE');
    });
    window.addEventListener('offline', () => {
      this.setSyncStatus('OFFLINE');
    });
  }

  setSyncStatus(status) {
    this.syncStatus = status;
    this.syncListeners.forEach(fn => fn(status));
  }

  subscribeSyncStatus(callback) {
    this.syncListeners.add(callback);
    callback(this.syncStatus);
    return () => this.syncListeners.delete(callback);
  }

  async init() {
    if (this.isLoaded) return;

    // Load static crew master (LP, ALP, Guard) for auto-fill
    try {
      const crewRes = await fetch('./data/kharsia_crew_master.json').then(r => r.json()).catch(() => []);
      this.crewMaster = Array.isArray(crewRes) ? crewRes : [];
    } catch (e) {
      console.warn('Crew master load notice:', e);
      this.crewMaster = [];
    }

    // Load static directory for all lobbies
    try {
      const dirRes = await fetch('./data/kharsia_directory.json').then(r => r.json()).catch(() => ({ lobbies: [] }));
      this.lobbies = Array.isArray(dirRes?.lobbies) ? dirRes.lobbies : [];
    } catch (e) {
      console.warn('Directory load notice:', e);
      this.lobbies = [];
    }

    // Load initial cached datasets
    this._loadLocalStorageCache();

    // Start real-time Firestore listeners
    this._initFirestoreListeners();

    this.isLoaded = true;
  }

  _loadLocalStorageCache() {
    try {
      this.dutyRecords = JSON.parse(localStorage.getItem(STORAGE_KEYS.CACHED_DUTIES) || '[]');
      this.rosterRecords = JSON.parse(localStorage.getItem(STORAGE_KEYS.CACHED_ROSTER) || '[]');
      this.usersList = JSON.parse(localStorage.getItem(STORAGE_KEYS.CACHED_USERS) || '[]');
      this.notifications = JSON.parse(localStorage.getItem(STORAGE_KEYS.CACHED_NOTICES) || '[]');
      this.storeRecords = JSON.parse(localStorage.getItem(STORAGE_KEYS.CACHED_STORE) || '[]');
      this.jeepMovements = JSON.parse(localStorage.getItem(STORAGE_KEYS.CACHED_JEEP) || '[]');
    } catch (_) {}

    // Seed realistic sample duties if empty
    if (!this.dutyRecords || this.dutyRecords.length === 0) {
      const today = new Date().toLocaleDateString('en-GB');
      this.dutyRecords = [
        {
          id: 'DUTY_KHS1042',
          crewId: 'KHS1042',
          crewName: 'Rajesh Kumar',
          designation: 'LP (Goods)',
          trainNo: 'BOXN/KHS-RIG',
          locoNo: '31422',
          section: 'KHS - RIG',
          signOnDate: today,
          signOnTime: '06:00',
          currentPosition: 'At Home Signal',
          status: 'LONG_HOUR',
          gdrStatus: 'Completed',
          expectedDeparture: '08:30',
          reliefStatus: 'AWAITING_RELIEF',
          reliefStation: 'Raigarh (RIG)',
          remarks: 'Awaiting line clearance, relieved crew standing by',
          updatedAt: new Date().toISOString(),
          updatedBy: 'CMS/KHS'
        },
        {
          id: 'DUTY_KHS1008',
          crewId: 'KHS1008',
          crewName: 'Nitish Kumar',
          designation: 'SALP',
          trainNo: 'N/GURDA-CHHL',
          locoNo: '31580',
          section: 'GURA - CHHL',
          signOnDate: today,
          signOnTime: '10:15',
          currentPosition: 'Placed in Siding',
          status: 'ON_DUTY',
          gdrStatus: 'In Progress',
          expectedDeparture: '13:00',
          reliefStatus: 'NORMAL',
          reliefStation: '',
          remarks: 'Loading in progress at Chhal Silo',
          updatedAt: new Date().toISOString(),
          updatedBy: 'Lobby In-Charge'
        }
      ];
      this._saveLocal(STORAGE_KEYS.CACHED_DUTIES, this.dutyRecords);
    }
  }

  _initFirestoreListeners() {
    if (!db) return;

    try {
      // 1. Listen to duty_records
      const dutyCol = collection(db, 'duty_records');
      onSnapshot(dutyCol, (snapshot) => {
        this.setSyncStatus('SYNCING');
        const items = [];
        snapshot.forEach(doc => {
          items.push({ id: doc.id, ...doc.data() });
        });
        if (items.length > 0) {
          this.dutyRecords = items;
          this._saveLocal(STORAGE_KEYS.CACHED_DUTIES, items);
          this.dutySubscribers.forEach(cb => cb(this.dutyRecords));
        }
        setTimeout(() => this.setSyncStatus(navigator.onLine ? 'ONLINE' : 'OFFLINE'), 400);
      }, (err) => {
        console.warn('Firestore duty_records listener:', err.message);
      });

      // 2. Listen to roster
      const rosterCol = collection(db, 'roster');
      onSnapshot(rosterCol, (snapshot) => {
        const items = [];
        snapshot.forEach(doc => {
          items.push({ id: doc.id, ...doc.data() });
        });
        if (items.length > 0) {
          this.rosterRecords = items;
          this._saveLocal(STORAGE_KEYS.CACHED_ROSTER, items);
          this.rosterSubscribers.forEach(cb => cb(this.rosterRecords));
        }
      }, () => {});

      // 3. Listen to users
      const usersCol = collection(db, 'users');
      onSnapshot(usersCol, (snapshot) => {
        const items = [];
        snapshot.forEach(doc => {
          items.push({ id: doc.id, ...doc.data() });
        });
        if (items.length > 0) {
          this.usersList = items;
          this._saveLocal(STORAGE_KEYS.CACHED_USERS, items);
          this.userSubscribers.forEach(cb => cb(this.usersList));
        }
      }, () => {});

      // 4. Listen to notifications
      const notifCol = collection(db, 'notifications');
      onSnapshot(notifCol, (snapshot) => {
        const items = [];
        snapshot.forEach(doc => {
          items.push({ id: doc.id, ...doc.data() });
        });
        if (items.length > 0) {
          this.notifications = items;
          this._saveLocal(STORAGE_KEYS.CACHED_NOTICES, items);
          this.notificationSubscribers.forEach(cb => cb(this.notifications));
        }
      }, () => {});
    } catch (e) {
      console.warn('Firestore initialization notice:', e);
    }
  }

  _saveLocal(key, data) {
    try {
      localStorage.setItem(key, JSON.stringify(data));
    } catch (_) {}
  }

  // --- AUTHENTICATION & USER SESSIONS ---
  getAuth() {
    const raw = localStorage.getItem(STORAGE_KEYS.AUTH);
    if (!raw) return { isLoggedIn: false, user: null };
    try {
      return JSON.parse(raw);
    } catch {
      return { isLoggedIn: false, user: null };
    }
  }

  async login({ crewId, mobile, password }) {
    this.setSyncStatus('SYNCING');
    const cleanId = (crewId || '').trim().toUpperCase();
    const cleanMob = (mobile || '').trim();

    // 1. Check local seed/admin credentials or existing users
    let matchedUser = this.usersList.find(u => 
      u.crewId?.toUpperCase() === cleanId || 
      (cleanMob && u.mobile === cleanMob)
    );

    // If super admin bypass or test admin
    if (cleanId === 'ADMIN' || cleanId === 'KHS_ADMIN' || cleanId === 'CCC_KHS') {
      const adminSession = {
        isLoggedIn: true,
        user: {
          uid: 'admin_khs',
          crewId: cleanId,
          name: 'Chief Crew Controller (Kharsia)',
          role: 'ADMIN',
          status: 'APPROVED',
          mobile: '9752442786'
        }
      };
      localStorage.setItem(STORAGE_KEYS.AUTH, JSON.stringify(adminSession));
      this.setSyncStatus('ONLINE');
      return { success: true, user: adminSession.user };
    }

    if (!matchedUser) {
      // Find in crew master to auto-create
      const crewProfile = this.findCrewByIdOrName(cleanId);
      matchedUser = {
        uid: `user_${cleanId}`,
        crewId: cleanId,
        name: crewProfile ? crewProfile.name : `Staff ${cleanId}`,
        designation: crewProfile ? crewProfile.designation : 'Running Crew',
        mobile: cleanMob || '9752000000',
        role: 'STAFF',
        status: 'PENDING',
        createdAt: new Date().toISOString()
      };
      await this.saveUser(matchedUser);
    }

    if (matchedUser.status === 'REJECTED') {
      this.setSyncStatus('ONLINE');
      return { success: false, error: 'Your access request has been rejected by Lobby Admin.' };
    }

    if (matchedUser.status === 'DISABLED') {
      this.setSyncStatus('ONLINE');
      return { success: false, error: 'Your account is disabled. Please contact CCC Kharsia.' };
    }

    if (matchedUser.status === 'PENDING') {
      this.setSyncStatus('ONLINE');
      return { 
        success: false, 
        pending: true, 
        error: 'Your registration is pending Admin Approval. Please contact Chief Crew Controller (CCC) Kharsia.' 
      };
    }

    // Approved user
    const session = {
      isLoggedIn: true,
      user: matchedUser
    };
    localStorage.setItem(STORAGE_KEYS.AUTH, JSON.stringify(session));
    this.setSyncStatus('ONLINE');
    return { success: true, user: matchedUser };
  }

  async register({ crewId, name, mobile, role = 'STAFF' }) {
    this.setSyncStatus('SYNCING');
    const cleanId = crewId.trim().toUpperCase();
    const newUser = {
      uid: `user_${cleanId}_${Date.now()}`,
      crewId: cleanId,
      name: name.trim(),
      mobile: mobile.trim(),
      role: role,
      status: 'PENDING',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    await this.saveUser(newUser);
    await this.logAudit({
      action: 'USER_REGISTER_REQUEST',
      performedBy: cleanId,
      targetId: newUser.uid,
      details: `User registered with role ${role}, awaiting approval.`
    });

    this.setSyncStatus('ONLINE');
    return newUser;
  }

  logout() {
    localStorage.removeItem(STORAGE_KEYS.AUTH);
    localStorage.removeItem(STORAGE_KEYS.ADMIN_AUTH);
  }

  // --- USER MANAGEMENT & APPROVAL ---
  async saveUser(user) {
    const idx = this.usersList.findIndex(u => u.uid === user.uid || u.crewId === user.crewId);
    if (idx >= 0) {
      this.usersList[idx] = { ...this.usersList[idx], ...user, updatedAt: new Date().toISOString() };
    } else {
      this.usersList.push(user);
    }
    this._saveLocal(STORAGE_KEYS.CACHED_USERS, this.usersList);
    this.userSubscribers.forEach(cb => cb(this.usersList));

    // Sync to Firestore
    if (db) {
      try {
        await setDoc(doc(db, 'users', user.uid), user, { merge: true });
      } catch (err) {
        console.warn('Sync user to Firestore notice:', err.message);
      }
    }
  }

  async approveUser(uid, role = 'STAFF') {
    const user = this.usersList.find(u => u.uid === uid);
    if (!user) return;
    user.status = 'APPROVED';
    user.role = role;
    user.updatedAt = new Date().toISOString();
    await this.saveUser(user);
    await this.logAudit({
      action: 'USER_APPROVED',
      performedBy: this.getAuth().user?.crewId || 'ADMIN',
      targetId: uid,
      details: `Approved user ${user.crewId} with role ${role}`
    });
  }

  async rejectUser(uid) {
    const user = this.usersList.find(u => u.uid === uid);
    if (!user) return;
    user.status = 'REJECTED';
    user.updatedAt = new Date().toISOString();
    await this.saveUser(user);
    await this.logAudit({
      action: 'USER_REJECTED',
      performedBy: this.getAuth().user?.crewId || 'ADMIN',
      targetId: uid,
      details: `Rejected registration for ${user.crewId}`
    });
  }

  async disableUser(uid) {
    const user = this.usersList.find(u => u.uid === uid);
    if (!user) return;
    user.status = 'DISABLED';
    user.updatedAt = new Date().toISOString();
    await this.saveUser(user);
  }

  getUsers() {
    return this.usersList;
  }

  subscribeUsers(cb) {
    this.userSubscribers.add(cb);
    cb(this.usersList);
    return () => this.userSubscribers.delete(cb);
  }

  // --- DUTY & LONG-HOURS MODULE ---
  getDuties() {
    return this.dutyRecords;
  }

  subscribeDuties(cb) {
    this.dutySubscribers.add(cb);
    cb(this.dutyRecords);
    return () => this.dutySubscribers.delete(cb);
  }

  async saveDuty(dutyData) {
    this.setSyncStatus('SYNCING');
    const id = dutyData.id || `DUTY_${dutyData.crewId}_${Date.now()}`;
    const duty = {
      ...dutyData,
      id,
      updatedAt: new Date().toISOString(),
      updatedBy: this.getAuth().user?.crewId || 'WEB_USER'
    };

    const idx = this.dutyRecords.findIndex(d => d.id === id);
    if (idx >= 0) {
      this.dutyRecords[idx] = duty;
    } else {
      this.dutyRecords.unshift(duty);
    }

    this._saveLocal(STORAGE_KEYS.CACHED_DUTIES, this.dutyRecords);
    this.dutySubscribers.forEach(cb => cb(this.dutyRecords));

    // Firestore sync
    if (db) {
      try {
        await setDoc(doc(db, 'duty_records', id), duty, { merge: true });
      } catch (err) {
        console.warn('Sync duty to Firestore notice:', err.message);
      }
    }
    this.setSyncStatus('ONLINE');
    return duty;
  }

  async markDutyRelieved(id, reliefStation, remarks = '') {
    const duty = this.dutyRecords.find(d => d.id === id);
    if (!duty) return;
    duty.status = 'RELIEVED';
    duty.reliefStatus = 'RELIEVED';
    duty.reliefStation = reliefStation;
    duty.reliefTime = new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
    duty.remarks = remarks || duty.remarks;
    await this.saveDuty(duty);
    await this.logAudit({
      action: 'DUTY_RELIEVED',
      performedBy: this.getAuth().user?.crewId || 'STAFF',
      targetId: id,
      details: `Relieved at ${reliefStation}`
    });
  }

  calculateDutyDuration(signOnDate, signOnTime) {
    if (!signOnTime) return { hours: 0, text: '0h 00m', isLongHour: false, isUrgent: false };
    try {
      const now = new Date();
      const parts = signOnTime.split(':');
      const dutyStart = new Date();
      dutyStart.setHours(parseInt(parts[0], 10), parseInt(parts[1], 10), 0, 0);

      let diffMs = now - dutyStart;
      if (diffMs < 0) diffMs += 24 * 60 * 60 * 1000; // overnight duty

      const totalMinutes = Math.floor(diffMs / 60000);
      const hours = Math.floor(totalMinutes / 60);
      const mins = totalMinutes % 60;
      const hoursDecimal = hours + (mins / 60);

      const isLongHour = hoursDecimal >= this.appConfig.longHourThresholdHours;
      const isUrgent = hoursDecimal >= this.appConfig.urgentReliefHours;

      return {
        hours: hoursDecimal,
        text: `${hours}h ${String(mins).padStart(2, '0')}m`,
        isLongHour,
        isUrgent
      };
    } catch {
      return { hours: 0, text: '0h 00m', isLongHour: false, isUrgent: false };
    }
  }

  // --- ROSTER MANAGEMENT ---
  getRoster(shift = '06-14', date = '') {
    return this.rosterRecords.filter(r => (!shift || r.shift === shift) && (!date || r.date === date));
  }

  subscribeRoster(cb) {
    this.rosterSubscribers.add(cb);
    cb(this.rosterRecords);
    return () => this.rosterSubscribers.delete(cb);
  }

  async saveRosterItem(item) {
    this.setSyncStatus('SYNCING');
    const id = item.id || `ROSTER_${item.shift}_${item.role}_${Date.now()}`;
    const record = {
      ...item,
      id,
      updatedAt: new Date().toISOString(),
      updatedBy: this.getAuth().user?.crewId || 'ROSTER_OFFICER'
    };

    const idx = this.rosterRecords.findIndex(r => r.id === id);
    if (idx >= 0) {
      this.rosterRecords[idx] = record;
    } else {
      this.rosterRecords.push(record);
    }

    this._saveLocal(STORAGE_KEYS.CACHED_ROSTER, this.rosterRecords);
    this.rosterSubscribers.forEach(cb => cb(this.rosterRecords));

    if (db) {
      try {
        await setDoc(doc(db, 'roster', id), record, { merge: true });
      } catch (err) {
        console.warn('Sync roster to Firestore notice:', err.message);
      }
    }
    this.setSyncStatus('ONLINE');
    return record;
  }

  // --- NOTIFICATIONS ---
  getNotifications() {
    return this.notifications;
  }

  subscribeNotifications(cb) {
    this.notificationSubscribers.add(cb);
    cb(this.notifications);
    return () => this.notificationSubscribers.delete(cb);
  }

  async postNotification({ title, message, priority = 'NORMAL', targetRole = 'ALL' }) {
    this.setSyncStatus('SYNCING');
    const id = `NOTIF_${Date.now()}`;
    const notif = {
      id,
      title,
      message,
      priority,
      targetRole,
      createdAt: new Date().toISOString(),
      createdBy: this.getAuth().user?.crewId || 'ADMIN'
    };

    this.notifications.unshift(notif);
    this._saveLocal(STORAGE_KEYS.CACHED_NOTICES, this.notifications);
    this.notificationSubscribers.forEach(cb => cb(this.notifications));

    if (db) {
      try {
        await setDoc(doc(db, 'notifications', id), notif);
      } catch (err) {
        console.warn('Sync notification notice:', err.message);
      }
    }
    this.setSyncStatus('ONLINE');
    return notif;
  }

  // --- AUDIT TRAIL ---
  async logAudit({ action, performedBy, targetId, details }) {
    const log = {
      id: `AUDIT_${Date.now()}`,
      action,
      performedBy: performedBy || 'SYSTEM',
      targetId: targetId || '',
      details: details || '',
      timestamp: new Date().toISOString()
    };
    this.auditLogs.unshift(log);
    if (db) {
      try {
        await setDoc(doc(db, 'audit_history', log.id), log);
      } catch (_) {}
    }
  }

  getAuditLogs() {
    return this.auditLogs;
  }

  // --- CREW MASTER SEARCH (Without publishing personal staff phone directory) ---
  findCrewByIdOrName(query) {
    if (!query || !this.crewMaster.length) return null;
    const clean = query.trim().toUpperCase();
    return this.crewMaster.find(c => 
      c.crewId?.toUpperCase() === clean ||
      c.name?.toUpperCase().includes(clean)
    );
  }

  getAllCrew() {
    return this.crewMaster;
  }

  // --- APP CONFIGURATION ---
  getAppConfig() {
    return this.appConfig;
  }

  // --- PR REQUESTS ---
  getPrRequests() {
    return this.prRequests || [];
  }

  addPrRequest(req) {
    if (!this.prRequests) this.prRequests = [];
    const item = { ...req, id: Date.now(), status: req.status || 'Pending' };
    this.prRequests.unshift(item);
    this._saveLocal('kharsia_web_pr_requests', this.prRequests);
    return item;
  }

  reviewPrRequest(id, status, remarks = '') {
    if (!this.prRequests) return;
    const item = this.prRequests.find(p => p.id === id);
    if (item) {
      item.status = status;
      item.remarks = remarks;
      this._saveLocal('kharsia_web_pr_requests', this.prRequests);
    }
  }

  // --- LOBBY DIRECTORY ---
  getLobbies() {
    return this.lobbies || [];
  }

  getOtherLobbies() {
    return (this.lobbies || []).filter(l => l.code !== 'KHS' && l.code !== 'CTRL');
  }

  getAllOtherCrew() {
    const list = [];
    for (const lobby of this.getOtherLobbies()) {
      for (const cat of lobby.categories || []) {
        for (const contact of cat.contacts || []) {
          list.push({
            lobbyCode: lobby.code,
            lobbyName: lobby.name,
            category: cat.category,
            name: contact.name,
            designation: contact.designation,
            mobile: contact.mobile || contact.cug || ''
          });
        }
      }
    }
    return list;
  }

  // --- STORE REGISTER ---
  getStoreRecords() {
    return this.storeRecords || [];
  }

  addStoreIssue(rec) {
    if (!this.storeRecords) this.storeRecords = [];
    const item = { ...rec, id: Date.now(), status: 'ISSUED' };
    this.storeRecords.unshift(item);
    this._saveLocal(STORAGE_KEYS.CACHED_STORE, this.storeRecords);
    return item;
  }

  returnStoreEquipment(id, returnedAt, remarks = '') {
    if (!this.storeRecords) return;
    const item = this.storeRecords.find(s => s.id === id);
    if (item) {
      item.status = 'RETURNED';
      item.returnedAt = returnedAt;
      item.remarks = remarks;
      this._saveLocal(STORAGE_KEYS.CACHED_STORE, this.storeRecords);
    }
  }

  // --- JEEP MOVEMENTS ---
  getJeepMovements() {
    return this.jeepMovements || [];
  }

  getJeepAvailability() {
    return [
      { vehicleNo: 'CG 13 J 1089', isAvailable: true, driver: 'Shyam Sundar' },
      { vehicleNo: 'CG 13 U 8989', isAvailable: true, driver: 'Ram Kumar' },
      { vehicleNo: 'CG 13 AB 2222', isAvailable: false, driver: 'Ganesh Das' },
      { vehicleNo: 'CG 13 J 3131', isAvailable: true, driver: 'Anand Lal' }
    ];
  }

  addJeepMovement(mov) {
    if (!this.jeepMovements) this.jeepMovements = [];
    const item = { ...mov, id: Date.now() };
    this.jeepMovements.unshift(item);
    this._saveLocal(STORAGE_KEYS.CACHED_JEEP, this.jeepMovements);
    return item;
  }

  // In-Charge PIN check
  verifyAdminPin(enteredPin) {
    const valid = enteredPin === '1234';
    if (valid) {
      localStorage.setItem(STORAGE_KEYS.ADMIN_AUTH, JSON.stringify({
        authenticated: true,
        expiry: Date.now() + 30 * 60 * 1000
      }));
    }
    return valid;
  }

  isAdminSessionActive() {
    const raw = localStorage.getItem(STORAGE_KEYS.ADMIN_AUTH);
    if (!raw) return false;
    try {
      const s = JSON.parse(raw);
      return s.authenticated && Date.now() < s.expiry;
    } catch {
      return false;
    }
  }
}

export const store = new KharsiaStore();
