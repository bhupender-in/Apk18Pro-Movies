package com.example.models

/**
 * Model representing a Social Media link configured by the Admin.
 * Stored in Firestore under "socialMedia/{id}".
 */
data class SocialMedia(
    val id: String = "",
    val platformName: String = "",
    val url: String = "",
    val iconType: String = "website", // "youtube", "instagram", "telegram", "facebook", "whatsapp", "website"
    val enabled: Boolean = true,
    val order: Int = 0
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "platformName" to platformName,
        "url" to url,
        "iconType" to iconType,
        "enabled" to enabled,
        "order" to order
    )
}
