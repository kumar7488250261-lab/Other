package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.equipment.EquipmentRecord
import com.example.data.equipment.JeepMovementRecord
import com.example.data.equipment.LongHourDutyRecord
import com.example.data.equipment.RosterTlcRecord
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SyncState {
    ONLINE,
    SYNCING,
    OFFLINE
}

data class UserProfile(
    val uid: String = "",
    val crewId: String = "",
    val name: String = "",
    val mobile: String = "",
    val role: String = "STAFF",
    val status: String = "PENDING",
    val designation: String = ""
)

class FirebaseSyncManager(private val context: Context) {

    companion object {
        private const val TAG = "KharsiaFirebaseSync"
        const val DATABASE_ID = "ai-studio-kharsialobby-8e7ab0fe-8694-491f-8f3c-ada6ac0916e1"
        const val PROJECT_ID = "empyrean-bridge-163612"
        const val API_KEY = "AIzaSyADnkFNGgQ8SyVR8_-VeJ9rWed-1jLxYVw"
        const val APP_ID = "1:920404946899:android:com.kharsialobby.app"
        const val STORAGE_BUCKET = "empyrean-bridge-163612.firebasestorage.app"

        @Volatile
        private var instance: FirebaseSyncManager? = null

        fun getInstance(context: Context): FirebaseSyncManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseSyncManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val _syncState = MutableStateFlow(SyncState.ONLINE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var firestore: FirebaseFirestore? = null

    init {
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setProjectId(PROJECT_ID)
                    .setApplicationId(APP_ID)
                    .setApiKey(API_KEY)
                    .setStorageBucket(STORAGE_BUCKET)
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "FirebaseApp initialized successfully")
            }

            val app = FirebaseApp.getInstance()
            firestore = try {
                FirebaseFirestore.getInstance(app, DATABASE_ID)
            } catch (e: Exception) {
                Log.w(TAG, "Named database init error, fallback to default", e)
                FirebaseFirestore.getInstance(app)
            }
            _syncState.value = SyncState.ONLINE
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization error", e)
            _syncState.value = SyncState.OFFLINE
        }
    }

    // --- DUTY RECORDS REAL-TIME SYNC ---
    fun listenDuties(onDutiesUpdated: (List<LongHourDutyRecord>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("duty_records")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Duty listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        _syncState.value = SyncState.SYNCING
                        val list = mutableListOf<LongHourDutyRecord>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val crewId = doc.getString("crewId") ?: ""
                                val crewName = doc.getString("crewName") ?: ""
                                val designation = doc.getString("designation") ?: ""
                                val trainNo = doc.getString("trainNo") ?: ""
                                val locoNo = doc.getString("locoNo") ?: ""
                                val section = doc.getString("section") ?: ""
                                val signOnTime = doc.getString("signOnTime") ?: ""
                                val signOnDate = doc.getString("signOnDate") ?: ""
                                val status = doc.getString("status") ?: "ON_DUTY"
                                val currentPosition = doc.getString("currentPosition") ?: ""
                                val gdrStatus = doc.getString("gdrStatus") ?: ""
                                val expectedDeparture = doc.getString("expectedDeparture") ?: ""
                                val reliefStatus = doc.getString("reliefStatus") ?: ""
                                val reliefStation = doc.getString("reliefStation") ?: ""
                                val reliefTime = doc.getString("reliefTime") ?: ""
                                val remarks = doc.getString("remarks") ?: ""
                                val dutyHours = doc.getDouble("dutyHours") ?: 0.0

                                list.add(
                                    LongHourDutyRecord(
                                        id = doc.id.hashCode().toLong(),
                                        crewId = crewId,
                                        crewName = crewName,
                                        designation = designation,
                                        trainNo = trainNo,
                                        section = section,
                                        signOnTime = signOnTime,
                                        signOnDate = signOnDate,
                                        dutyHours = dutyHours,
                                        status = status,
                                        reliefStation = reliefStation,
                                        remarks = remarks,
                                        locoNo = locoNo,
                                        currentPosition = currentPosition,
                                        gdrStatus = gdrStatus,
                                        expectedDeparture = expectedDeparture,
                                        reliefStatus = reliefStatus,
                                        reliefTime = reliefTime,
                                        firestoreId = id
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error mapping duty doc ${doc.id}", e)
                            }
                        }
                        if (list.isNotEmpty()) {
                            onDutiesUpdated(list)
                        }
                        _syncState.value = SyncState.ONLINE
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register duties listener", e)
            null
        }
    }

    fun syncDuty(record: LongHourDutyRecord, onComplete: ((Boolean) -> Unit)? = null) {
        val db = firestore ?: run {
            onComplete?.invoke(false)
            return
        }
        _syncState.value = SyncState.SYNCING

        val docId = if (record.firestoreId.isNotBlank()) record.firestoreId else "DUTY_${record.crewId}_${System.currentTimeMillis()}"
        val data = hashMapOf(
            "id" to docId,
            "crewId" to record.crewId,
            "crewName" to record.crewName,
            "designation" to record.designation,
            "trainNo" to record.trainNo,
            "locoNo" to record.locoNo,
            "section" to record.section,
            "signOnTime" to record.signOnTime,
            "signOnDate" to record.signOnDate,
            "status" to record.status,
            "currentPosition" to record.currentPosition,
            "gdrStatus" to record.gdrStatus,
            "expectedDeparture" to record.expectedDeparture,
            "reliefStatus" to record.reliefStatus,
            "reliefStation" to record.reliefStation,
            "reliefTime" to record.reliefTime,
            "remarks" to record.remarks,
            "dutyHours" to record.dutyHours,
            "updatedAt" to System.currentTimeMillis().toString(),
            "updatedBy" to "ANDROID_APP"
        )

        db.collection("duty_records").document(docId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                _syncState.value = SyncState.ONLINE
                onComplete?.invoke(true)
            }
            .addOnFailureListener {
                _syncState.value = SyncState.ONLINE
                onComplete?.invoke(false)
            }
    }

    // --- ROSTER REAL-TIME SYNC ---
    fun listenRoster(onRosterUpdated: (List<RosterTlcRecord>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("roster")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = mutableListOf<RosterTlcRecord>()
                        for (doc in snapshot.documents) {
                            try {
                                list.add(
                                    RosterTlcRecord(
                                        id = doc.id.hashCode().toLong(),
                                        shift = doc.getString("shift") ?: "06-14",
                                        date = doc.getString("date") ?: "",
                                        role = doc.getString("role") ?: "",
                                        staffId = doc.getString("staffId") ?: "",
                                        staffName = doc.getString("staffName") ?: "",
                                        designation = doc.getString("designation") ?: "",
                                        mobile = doc.getString("mobile") ?: "",
                                        remarks = doc.getString("remarks") ?: ""
                                    )
                                )
                            } catch (_: Exception) {}
                        }
                        if (list.isNotEmpty()) {
                            onRosterUpdated(list)
                        }
                    }
                }
        } catch (e: Exception) {
            null
        }
    }

    fun syncRoster(record: RosterTlcRecord) {
        val db = firestore ?: return
        val docId = "ROSTER_${record.shift}_${record.role.replace(" ", "_")}"
        val data = hashMapOf(
            "id" to docId,
            "shift" to record.shift,
            "date" to record.date,
            "role" to record.role,
            "staffId" to record.staffId,
            "staffName" to record.staffName,
            "designation" to record.designation,
            "mobile" to record.mobile,
            "remarks" to record.remarks,
            "updatedAt" to System.currentTimeMillis().toString(),
            "updatedBy" to "ANDROID_ROSTER"
        )
        db.collection("roster").document(docId).set(data, SetOptions.merge())
    }

    // --- USER REGISTRATION & APPROVAL CHECK ---
    fun registerUser(crewId: String, name: String, mobile: String, role: String, onResult: (Boolean, String?) -> Unit) {
        val db = firestore ?: run {
            onResult(false, "Offline or backend not initialized")
            return
        }

        val uid = "user_${crewId.trim().uppercase()}"
        val userData = hashMapOf(
            "uid" to uid,
            "crewId" to crewId.trim().uppercase(),
            "name" to name.trim(),
            "mobile" to mobile.trim(),
            "role" to role,
            "status" to "PENDING",
            "createdAt" to System.currentTimeMillis().toString()
        )

        db.collection("users").document(uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    fun checkUserStatus(crewId: String, onStatus: (String, String) -> Unit) {
        val db = firestore ?: run {
            onStatus("APPROVED", "STAFF")
            return
        }

        val uid = "user_${crewId.trim().uppercase()}"
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val status = doc.getString("status") ?: "APPROVED"
                    val role = doc.getString("role") ?: "STAFF"
                    onStatus(status, role)
                } else {
                    onStatus("APPROVED", "STAFF")
                }
            }
            .addOnFailureListener {
                onStatus("APPROVED", "STAFF")
            }
    }

    // --- AUDIT TRAIL ---
    fun logAudit(action: String, performedBy: String, details: String) {
        val db = firestore ?: return
        val logId = "AUDIT_${System.currentTimeMillis()}"
        val data = hashMapOf(
            "id" to logId,
            "action" to action,
            "performedBy" to performedBy,
            "details" to details,
            "timestamp" to System.currentTimeMillis().toString()
        )
        db.collection("audit_history").document(logId).set(data)
    }
}
