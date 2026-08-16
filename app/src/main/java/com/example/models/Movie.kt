package com.example.models

/**
 * Data model representing a Movie in Apk18pro.
 * Stored in Firestore under "movies/{movieId}".
 */
data class Movie(
    val id: String = "",
    val name: String = "",
    val bannerUrl: String = "",
    val posterUrl: String = "",
    val streamUrl: String = "",
    val description: String = "",
    val category: String = "Action",
    val language: String = "English",
    val year: String = "2024",
    val featured: Boolean = false,
    val published: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val views: Long = 0L
) {
    /**
     * Helper to get display image (prefers poster, falls back to banner).
     */
    val displayPosterUrl: String
        get() = posterUrl.ifBlank { bannerUrl }

    /**
     * Helper to get banner display image (prefers banner, falls back to poster).
     */
    val displayBannerUrl: String
        get() = bannerUrl.ifBlank { posterUrl }

    /**
     * Check if stream URL is valid format.
     */
    val isStreamValid: Boolean
        get() = streamUrl.isNotBlank() && (
            streamUrl.startsWith("http://", ignoreCase = true) ||
            streamUrl.startsWith("https://", ignoreCase = true) ||
            streamUrl.startsWith("rtmp://", ignoreCase = true)
        )

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "bannerUrl" to bannerUrl,
        "posterUrl" to posterUrl,
        "streamUrl" to streamUrl,
        "description" to description,
        "category" to category,
        "language" to language,
        "year" to year,
        "featured" to featured,
        "published" to published,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "views" to views
    )
}
