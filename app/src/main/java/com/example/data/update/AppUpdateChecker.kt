package com.example.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val isUpdateAvailable: Boolean = false,
    val currentVersion: String = "1.0",
    val latestVersion: String = "1.0",
    val releaseUrl: String = "https://github.com/abhishekused/Newapk/releases",
    val releaseNotes: String = ""
)

object AppUpdateChecker {

    private const val TAG = "KharsiaUpdateChecker"
    private const val GITHUB_OWNER = "abhishekused"
    private const val GITHUB_REPO = "Newapk"
    private const val RELEASES_API = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
    const val CURRENT_VERSION_NAME = "1.0"

    suspend fun checkForUpdate(): AppUpdateInfo = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(RELEASES_API)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "KharsiaLobby-Android")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val tagName = json.optString("tag_name", "").removePrefix("v").trim()
                val htmlUrl = json.optString("html_url", "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases")
                val body = json.optString("body", "")

                // Find APK asset download url if present
                var apkUrl = htmlUrl
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = asset.optString("browser_download_url", htmlUrl)
                            break
                        }
                    }
                }

                val hasUpdate = isVersionGreater(tagName, CURRENT_VERSION_NAME)
                return@withContext AppUpdateInfo(
                    isUpdateAvailable = hasUpdate,
                    currentVersion = CURRENT_VERSION_NAME,
                    latestVersion = tagName.ifBlank { CURRENT_VERSION_NAME },
                    releaseUrl = apkUrl,
                    releaseNotes = body
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Update check failed (safe fallback to current): ${e.message}")
        } finally {
            connection?.disconnect()
        }

        return@withContext AppUpdateInfo(
            isUpdateAvailable = false,
            currentVersion = CURRENT_VERSION_NAME,
            latestVersion = CURRENT_VERSION_NAME
        )
    }

    private fun isVersionGreater(latest: String, current: String): Boolean {
        if (latest.isBlank() || current.isBlank()) return false
        try {
            val latestParts = latest.split(".").mapNotNull { it.takeWhile { ch -> ch.isDigit() }.toIntOrNull() }
            val currentParts = current.split(".").mapNotNull { it.takeWhile { ch -> ch.isDigit() }.toIntOrNull() }
            val length = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until length) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
        } catch (_: Exception) {}
        return false
    }

    fun openUpdatePage(context: Context, releaseUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch release download browser", e)
        }
    }
}
