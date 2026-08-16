package com.example.models

/**
 * User session model stored locally and synced to Firestore "users/{userId}".
 */
data class UserSession(
    val userId: String = "",
    val isGuest: Boolean = true,
    val isAdmin: Boolean = false,
    val adminName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis(),
    val deviceModel: String = "",
    val androidVersion: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "isGuest" to isGuest,
        "isAdmin" to isAdmin,
        "adminName" to adminName,
        "createdAt" to createdAt,
        "lastLogin" to lastLogin,
        "deviceModel" to deviceModel,
        "androidVersion" to androidVersion
    )
}
