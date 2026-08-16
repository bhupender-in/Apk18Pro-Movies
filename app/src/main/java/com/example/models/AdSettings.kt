package com.example.models

/**
 * Advertisement settings managed from Admin Dashboard and stored in Firestore "appSettings/adSettings".
 */
data class AdSettings(
    val interstitialEnabled: Boolean = true,
    val minIntervalSeconds: Long = 60L, // Minimum safe interval between ads
    val adsOnMovieDetails: Boolean = true,
    val adsBeforePlayback: Boolean = true,
    val interstitialAdUnitId: String = "ca-app-pub-1010265026946536/3463665451",
    val testMode: Boolean = false
) {
    companion object {
        const val SAFE_MIN_INTERVAL_SECONDS = 30L // Google Play / AdMob UX policy floor
        const val DEFAULT_INTERSTITIAL_UNIT_ID = "ca-app-pub-1010265026946536/3463665451"
        const val TEST_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    }

    /**
     * Enforce policy-compliant minimum interval so settings can never violate ad rules.
     */
    val sanitizedIntervalSeconds: Long
        get() = maxOf(SAFE_MIN_INTERVAL_SECONDS, minIntervalSeconds)

    fun toMap(): Map<String, Any?> = mapOf(
        "interstitialEnabled" to interstitialEnabled,
        "minIntervalSeconds" to sanitizedIntervalSeconds,
        "adsOnMovieDetails" to adsOnMovieDetails,
        "adsBeforePlayback" to adsBeforePlayback,
        "interstitialAdUnitId" to interstitialAdUnitId,
        "testMode" to testMode
    )
}
