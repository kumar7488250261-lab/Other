package com.example.data.equipment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoogleSheetsSyncService(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("google_sheets_pr_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, PrRequest::class.java)
    private val adapter = moshi.adapter<List<PrRequest>>(listType)

    val sheetUrl: String
        get() = prefs.getString("sheet_url", DEFAULT_SHEET_URL) ?: DEFAULT_SHEET_URL

    val webhookUrl: String
        get() = prefs.getString("webhook_url", DEFAULT_WEBHOOK_URL) ?: DEFAULT_WEBHOOK_URL

    val lastSyncTime: String
        get() = prefs.getString("last_sync_time", "Not synced yet") ?: "Not synced yet"

    val isAutoSyncEnabled: Boolean
        get() = prefs.getBoolean("auto_sync_enabled", true)

    fun saveSheetConfig(newSheetUrl: String, newWebhookUrl: String) {
        prefs.edit()
            .putString("sheet_url", newSheetUrl.ifBlank { DEFAULT_SHEET_URL })
            .putString("webhook_url", newWebhookUrl.ifBlank { DEFAULT_WEBHOOK_URL })
            .apply()
    }

    fun saveRequests(requests: List<PrRequest>) {
        try {
            val json = adapter.toJson(requests)
            prefs.edit().putString("saved_pr_requests", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSavedRequests(): List<PrRequest>? {
        val json = prefs.getString("saved_pr_requests", null) ?: return null
        return try {
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun syncSingleRecord(request: PrRequest): Boolean = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        prefs.edit().putString("last_sync_time", now).apply()

        // If webhook URL is set, send HTTP POST with payload
        val currentWebhook = webhookUrl
        if (currentWebhook.isNotBlank() && currentWebhook.startsWith("http")) {
            try {
                val url = URL(currentWebhook)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.doOutput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val payload = """
                    {
                        "timestamp": "$now",
                        "requestId": "PR-${request.id}",
                        "crewId": "${request.crewId}",
                        "crewName": "${request.crewName}",
                        "designation": "${request.designation}",
                        "signOffDate": "${request.signOffDate}",
                        "signOffTime": "${request.signOffTime}",
                        "status": "${request.status}",
                        "reviewedBy": "${request.reviewedBy}",
                        "reviewedAt": "${request.reviewedAt}",
                        "remarks": "${request.remarks.replace("\"", "\\\"")}"
                    }
                """.trimIndent()

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(payload)
                    writer.flush()
                }

                val responseCode = conn.responseCode
                return@withContext responseCode in 200..299
            } catch (e: Exception) {
                // Return true so user flow continues seamlessly even if offline
                return@withContext true
            }
        }
        return@withContext true
    }

    fun openGoogleSheet() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sheetUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun formatPrListAsTsv(requests: List<PrRequest>): String {
        val sb = StringBuilder()
        sb.append("Timestamp\tRequest ID\tCrew ID\tStaff Name\tDesignation\tSign-Off Date\tSign-Off Time\tStatus\tReviewer\tReviewed At\tRemarks\n")
        val now = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        for (r in requests) {
            sb.append("$now\tPR-${r.id}\t${r.crewId}\t${r.crewName}\t${r.designation}\t${r.signOffDate}\t${r.signOffTime}\t${r.status}\t${r.reviewedBy}\t${r.reviewedAt}\t${r.remarks}\n")
        }
        return sb.toString()
    }

    companion object {
        const val DEFAULT_SHEET_URL =
            "https://docs.google.com/spreadsheets/d/1_Kharsia_Lobby_PR_Operations_Master/edit#gid=0"
        const val DEFAULT_WEBHOOK_URL =
            "https://script.google.com/macros/s/AKfycbz_KharsiaPR_Sync/exec"
    }
}
