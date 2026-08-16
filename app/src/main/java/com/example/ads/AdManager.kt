package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.models.AdSettings
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Centralized AdMob Interstitial Ad Controller for Apk18pro.
 * Strictly adheres to Google Play & AdMob policies:
 * - Does not show ads during active video playback.
 * - Implements a policy-compliant time throttle (minimum interval between interstitial impressions).
 * - Requires a natural transition point (screen transitions / before media playback).
 * - Pre-loads interstitial ads in the background.
 * - Graceful fallback: if ad fails to load or device is offline, callbacks execute immediately without blocking UI.
 */
class AdManager private constructor(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingAd = false
    private var lastAdShownTimestamp: Long = 0L
    private val isMobileAdsInitialized = AtomicBoolean(false)

    // Dynamic settings updated from Admin dashboard / Firestore
    private var currentSettings: AdSettings = AdSettings()

    // Screen transition counter for pacing
    private var screenTransitionCounter = 0

    init {
        initializeMobileAds()
    }

    fun updateSettings(settings: AdSettings) {
        this.currentSettings = settings
    }

    private fun initializeMobileAds() {
        if (isMobileAdsInitialized.getAndSet(true)) return

        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob SDK Initialized: ${initializationStatus.adapterStatusMap}")
                loadInterstitialAd()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdMob: ${e.message}")
        }
    }

    /**
     * Preloads an interstitial ad in the background.
     */
    fun loadInterstitialAd() {
        if (isLoadingAd || interstitialAd != null) return

        val adUnitId = if (currentSettings.testMode) {
            AdSettings.TEST_INTERSTITIAL_UNIT_ID
        } else {
            currentSettings.interstitialAdUnitId.ifBlank { AdSettings.DEFAULT_INTERSTITIAL_UNIT_ID }
        }

        isLoadingAd = true
        val adRequest = AdRequest.Builder().build()

        try {
            InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d(TAG, "Interstitial ad loaded successfully.")
                        interstitialAd = ad
                        isLoadingAd = false
                        setupAdCallbacks(ad)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message} (Code: ${loadAdError.code})")
                        interstitialAd = null
                        isLoadingAd = false
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during InterstitialAd.load: ${e.message}")
            isLoadingAd = false
        }
    }

    private fun setupAdCallbacks(ad: InterstitialAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad dismissed.")
                interstitialAd = null
                lastAdShownTimestamp = System.currentTimeMillis()
                // Preload next ad for the subsequent eligible transition
                loadInterstitialAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
                interstitialAd = null
                loadInterstitialAd()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad is showing on screen.")
                interstitialAd = null
            }
        }
    }

    /**
     * Checks whether an interstitial ad is permitted to be shown at this moment.
     * Enforces:
     * 1. Admin master toggle (interstitialEnabled)
     * 2. Time-based throttle (policy-compliant minimum interval)
     * 3. Availability of a loaded ad
     */
    private fun canShowInterstitialAd(): Boolean {
        if (!currentSettings.interstitialEnabled) {
            return false
        }

        val currentTime = System.currentTimeMillis()
        val minIntervalMillis = currentSettings.sanitizedIntervalSeconds * 1000L
        val elapsedSinceLastAd = currentTime - lastAdShownTimestamp

        if (elapsedSinceLastAd < minIntervalMillis) {
            Log.d(TAG, "Ad skipped: Throttle active (${(minIntervalMillis - elapsedSinceLastAd) / 1000}s remaining)")
            return false
        }

        return interstitialAd != null
    }

    /**
     * Shows an interstitial ad if conditions are satisfied.
     * @param activity Hosting Activity
     * @param isBeforePlayback True if the transition is immediately before starting playback
     * @param onComplete Callback invoked when ad is finished or if ad is skipped/unavailable
     */
    fun showInterstitialIfAllowed(
        activity: Activity?,
        isBeforePlayback: Boolean = false,
        onComplete: () -> Unit
    ) {
        screenTransitionCounter++

        // Check feature-specific admin switches
        if (isBeforePlayback && !currentSettings.adsBeforePlayback) {
            onComplete()
            return
        }

        if (!isBeforePlayback && !currentSettings.adsOnMovieDetails) {
            onComplete()
            return
        }

        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            onComplete()
            return
        }

        if (canShowInterstitialAd()) {
            val ad = interstitialAd
            if (ad != null) {
                val previousCallback = ad.fullScreenContentCallback
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        previousCallback?.onAdDismissedFullScreenContent()
                        onComplete()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        previousCallback?.onAdFailedToShowFullScreenContent(adError)
                        onComplete()
                    }

                    override fun onAdShowedFullScreenContent() {
                        previousCallback?.onAdShowedFullScreenContent()
                    }
                }

                ad.show(activity)
                return
            }
        }

        // If ad wasn't ready or throttled, proceed immediately
        if (interstitialAd == null && !isLoadingAd) {
            loadInterstitialAd()
        }
        onComplete()
    }

    companion object {
        private const val TAG = "AdManager"

        @Volatile
        private var instance: AdManager? = null

        fun getInstance(context: Context): AdManager {
            return instance ?: synchronized(this) {
                instance ?: AdManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
