package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.models.UserSession
import com.example.utils.UserIdGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("apk18pro_session_prefs", Context.MODE_PRIVATE)

    private val _sessionState = MutableStateFlow(loadSession())
    val sessionState: StateFlow<UserSession> = _sessionState.asStateFlow()

    init {
        // Ensure user ID exists on initialization
        if (prefs.getString(KEY_USER_ID, null).isNullOrBlank()) {
            val newUserId = UserIdGenerator.generate()
            prefs.edit()
                .putString(KEY_USER_ID, newUserId)
                .putLong(KEY_CREATED_AT, System.currentTimeMillis())
                .apply()
        }
        _sessionState.value = loadSession()
    }

    fun getUserId(): String {
        return prefs.getString(KEY_USER_ID, null) ?: run {
            val newId = UserIdGenerator.generate()
            prefs.edit().putString(KEY_USER_ID, newId).apply()
            newId
        }
    }

    fun loadSession(): UserSession {
        val userId = prefs.getString(KEY_USER_ID, null) ?: UserIdGenerator.generate().also {
            prefs.edit().putString(KEY_USER_ID, it).apply()
        }
        val isAdmin = prefs.getBoolean(KEY_IS_ADMIN, false)
        val adminName = prefs.getString(KEY_ADMIN_NAME, "") ?: ""
        val createdAt = prefs.getLong(KEY_CREATED_AT, System.currentTimeMillis())
        val lastLogin = System.currentTimeMillis()

        return UserSession(
            userId = userId,
            isGuest = !isAdmin,
            isAdmin = isAdmin,
            adminName = adminName,
            createdAt = createdAt,
            lastLogin = lastLogin,
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
        )
    }

    fun setAdminLoggedIn(adminId: String) {
        prefs.edit()
            .putBoolean(KEY_IS_ADMIN, true)
            .putString(KEY_ADMIN_NAME, adminId)
            .putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
            .apply()
        _sessionState.value = loadSession()
    }

    fun logoutAdmin() {
        prefs.edit()
            .putBoolean(KEY_IS_ADMIN, false)
            .putString(KEY_ADMIN_NAME, "")
            .apply()
        _sessionState.value = loadSession()
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_ADMIN = "is_admin"
        private const val KEY_ADMIN_NAME = "admin_name"
        private const val KEY_CREATED_AT = "created_at"
        private const val KEY_LAST_LOGIN = "last_login"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
