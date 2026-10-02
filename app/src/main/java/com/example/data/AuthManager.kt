package com.example.data

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kharsia_auth_prefs", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var currentUserId: String
        get() = prefs.getString("current_user_id", "KHS1234") ?: "KHS1234"
        set(value) = prefs.edit().putString("current_user_id", value).apply()

    fun login(userId: String): Boolean {
        isLoggedIn = true
        currentUserId = userId
        return true
    }

    fun logout() {
        isLoggedIn = false
        currentUserId = ""
    }
}
