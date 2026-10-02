// Kharsia Lobby - Firebase Web SDK Configuration
// Connects to the same Firestore named database as the Android application.
import { initializeApp } from 'firebase/app';
import { 
  initializeFirestore, 
  persistentLocalCache, 
  persistentMultipleTabManager,
  collection,
  doc,
  getDoc,
  getDocs,
  setDoc,
  updateDoc,
  deleteDoc,
  onSnapshot,
  query,
  where,
  orderBy,
  limit,
  serverTimestamp
} from 'firebase/firestore';
import { getAuth, signInAnonymously } from 'firebase/auth';

const firebaseConfig = {
  projectId: "empyrean-bridge-163612",
  appId: "1:920404946899:web:222b36408fc21b95fb4926",
  apiKey: "AIzaSyADnkFNGgQ8SyVR8_-VeJ9rWed-1jLxYVw",
  authDomain: "empyrean-bridge-163612.firebaseapp.com",
  storageBucket: "empyrean-bridge-163612.firebasestorage.app",
  messagingSenderId: "920404946899"
};

export const FIRESTORE_DATABASE_ID = "ai-studio-kharsialobby-8e7ab0fe-8694-491f-8f3c-ada6ac0916e1";

// Initialize Firebase App
export const app = initializeApp(firebaseConfig);

// Initialize Firestore with offline persistence
let firestoreInstance;
try {
  firestoreInstance = initializeFirestore(app, {
    localCache: persistentLocalCache({
      tabManager: persistentMultipleTabManager()
    })
  }, FIRESTORE_DATABASE_ID);
} catch (e) {
  console.warn('Firestore multi-tab cache initialization warning, falling back:', e);
  try {
    firestoreInstance = initializeFirestore(app, {}, FIRESTORE_DATABASE_ID);
  } catch (err) {
    console.error('Firestore initialization error:', err);
  }
}

export const db = firestoreInstance;
export const auth = getAuth(app);

export {
  collection,
  doc,
  getDoc,
  getDocs,
  setDoc,
  updateDoc,
  deleteDoc,
  onSnapshot,
  query,
  where,
  orderBy,
  limit,
  serverTimestamp,
  signInAnonymously
};
