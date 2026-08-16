package com.example.firebase

import android.util.Log
import com.example.models.AdSettings
import com.example.models.Movie
import com.example.models.SocialMedia
import com.example.models.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Firebase not initialized or missing google-services.json: ${e.message}")
            null
        }
    }

    // In-memory cache & fallback when Firestore is offline/unconfigured
    private val _localMovies = MutableStateFlow<List<Movie>>(getInitialSampleMovies())
    val localMovies = _localMovies.asStateFlow()

    private val _localSocialMedia = MutableStateFlow<List<SocialMedia>>(getInitialSocialMedia())
    val localSocialMedia = _localSocialMedia.asStateFlow()

    private val _localAdSettings = MutableStateFlow(AdSettings())
    val localAdSettings = _localAdSettings.asStateFlow()

    companion object {
        private const val TAG = "FirestoreRepository"
        const val COLLECTION_MOVIES = "movies"
        const val COLLECTION_SOCIAL = "socialMedia"
        const val COLLECTION_APP_SETTINGS = "appSettings"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_ADMINS = "admins"

        @Volatile
        private var instance: FirestoreRepository? = null

        fun getInstance(): FirestoreRepository {
            return instance ?: synchronized(this) {
                instance ?: FirestoreRepository().also { instance = it }
            }
        }

        /**
         * Rich OTT movie catalog seeded out of the box with public domain / Creative Commons streams.
         */
        fun getInitialSampleMovies(): List<Movie> = listOf(
            Movie(
                id = "mov_sintel",
                name = "Sintel: The Dragon's Flight",
                bannerUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=1200&auto=format&fit=crop",
                posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=600&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                description = "A lonely young woman, Sintel, helps and befriends a baby dragon whom she names Scales. But when Scales is snatched away by an adult dragon, Sintel embarks on an epic, dangerous quest to find her lost friend across snowy peaks and hostile lands.",
                category = "Fantasy",
                language = "English",
                year = "2024",
                featured = true,
                published = true,
                createdAt = System.currentTimeMillis() - 1000000,
                views = 14200
            ),
            Movie(
                id = "mov_tears_steel",
                name = "Tears of Steel: Cyber Dawn",
                bannerUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=1200&auto=format&fit=crop",
                posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?q=80&w=600&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                description = "In a dystopian future set in Amsterdam, a group of rebel warriors and scientists stage a desperate cybernetic intervention to prevent a colossal army of destructive robotic behemoths from wiping out humanity.",
                category = "Sci-Fi",
                language = "English",
                year = "2024",
                featured = true,
                published = true,
                createdAt = System.currentTimeMillis() - 2000000,
                views = 28900
            ),
            Movie(
                id = "mov_big_buck",
                name = "Big Buck Bunny: Forest Uprising",
                bannerUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=1200&auto=format&fit=crop",
                posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=600&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                description = "A giant, gentle rabbit wakes up on a sunny morning only to be harassed by a mischievous gang of forest bullies. Pushed to the brink, the gentle giant devises an ingenious, hilarious series of traps to reclaim peace.",
                category = "Animation",
                language = "English",
                year = "2023",
                featured = true,
                published = true,
                createdAt = System.currentTimeMillis() - 3000000,
                views = 35100
            ),
            Movie(
                id = "mov_elephants_dream",
                name = "Elephants Dream: Infinite Machinery",
                bannerUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=1200&auto=format&fit=crop",
                posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=600&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                description = "Two companions explore the surreal, gargantuan depths of a giant, labyrinthine machine whose internal workings respond to thoughts, illusions, and subconscious fears.",
                category = "Sci-Fi",
                language = "English",
                year = "2023",
                featured = false,
                published = true,
                createdAt = System.currentTimeMillis() - 4000000,
                views = 9800
            ),
            Movie(
                id = "mov_cosmos_hls",
                name = "Cosmos: Journey Across Galaxies",
                bannerUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=1200&auto=format&fit=crop",
                posterUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=600&auto=format&fit=crop",
                streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                description = "An astonishing high-definition voyage traversing planetary rings, glowing nebulae, supermassive black holes, and the distant edges of the observable universe.",
                category = "Documentary",
                language = "English",
                year = "2024",
                featured = true,
                published = true,
                createdAt = System.currentTimeMillis() - 5000000,
                views = 42100
            ),
            Movie(
                id = "mov_action_chrono",
                name = "Chrono Velocity: Zero Protocol",
                bannerUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?q=80&w=1200&auto=format&fit=crop",
                posterUrl = "https://images.unsplash.com/photo-1533488765986-dfa2a9939acd?q=80&w=600&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                description = "An elite undercover operative is thrust into high-octane vehicular chases and rooftop shootouts to dismantle an international syndicate trafficking quantum weaponry.",
                category = "Action",
                language = "English",
                year = "2024",
                featured = false,
                published = true,
                createdAt = System.currentTimeMillis() - 6000000,
                views = 19400
            ),
            Movie(
                id = "mov_night_stalker",
                name = "Shadows in the Mist",
                bannerUrl = "https://images.unsplash.com/photo-1509248961158-e54f6934749c?q=80&w=1200&auto=format&fit=crop",
                posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=600&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                description = "A detective investigates a string of inexplicable disappearances in an isolated mountain village blanketed by perpetual fog, unearthing secrets that defy reason.",
                category = "Thriller",
                language = "Hindi",
                year = "2023",
                featured = false,
                published = true,
                createdAt = System.currentTimeMillis() - 7000000,
                views = 11200
            )
        )

        fun getInitialSocialMedia(): List<SocialMedia> = listOf(
            SocialMedia("sm_yt", "YouTube Channel", "https://youtube.com", "youtube", true, 1),
            SocialMedia("sm_tg", "Telegram Community", "https://telegram.org", "telegram", true, 2),
            SocialMedia("sm_ig", "Instagram", "https://instagram.com", "instagram", true, 3),
            SocialMedia("sm_wa", "WhatsApp Updates", "https://whatsapp.com", "whatsapp", true, 4),
            SocialMedia("sm_web", "Official Portal", "https://apk18pro.com", "website", true, 5)
        )
    }

    // =========================================================================
    // MOVIES FLOW & CRUD
    // =========================================================================

    /**
     * Flow of all movies, either from Firestore realtime listener or fallback cache.
     */
    fun getMoviesFlow(): Flow<List<Movie>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(_localMovies.value)
            awaitClose { }
            return@callbackFlow
        }

        var listenerRegistration: ListenerRegistration? = null
        try {
            listenerRegistration = db.collection(COLLECTION_MOVIES)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Firestore movies listener error: ${error.message}")
                        trySend(_localMovies.value)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val movies = snapshot.documents.mapNotNull { doc ->
                            try {
                                Movie(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "",
                                    bannerUrl = doc.getString("bannerUrl") ?: "",
                                    posterUrl = doc.getString("posterUrl") ?: "",
                                    streamUrl = doc.getString("streamUrl") ?: "",
                                    description = doc.getString("description") ?: "",
                                    category = doc.getString("category") ?: "Action",
                                    language = doc.getString("language") ?: "English",
                                    year = doc.getString("year") ?: "2024",
                                    featured = doc.getBoolean("featured") ?: false,
                                    published = doc.getBoolean("published") ?: true,
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                                    views = doc.getLong("views") ?: 0L
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        _localMovies.value = movies
                        trySend(movies)
                    } else {
                        // Empty in Firestore: auto-seed initial movies if needed
                        trySend(_localMovies.value)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach movies snapshot listener", e)
            trySend(_localMovies.value)
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    suspend fun addMovie(movie: Movie): Result<String> {
        val db = firestore
        val movieWithTime = movie.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        return try {
            if (db != null) {
                val docRef = if (movie.id.isNotBlank()) {
                    db.collection(COLLECTION_MOVIES).document(movie.id)
                } else {
                    db.collection(COLLECTION_MOVIES).document()
                }
                val finalMovie = movieWithTime.copy(id = docRef.id)
                docRef.set(finalMovie.toMap()).await()
                
                // Update memory
                val updated = _localMovies.value.toMutableList().apply {
                    removeAll { it.id == finalMovie.id }
                    add(0, finalMovie)
                }
                _localMovies.value = updated
                Result.success(docRef.id)
            } else {
                val generatedId = if (movie.id.isNotBlank()) movie.id else "mov_${System.currentTimeMillis()}"
                val finalMovie = movieWithTime.copy(id = generatedId)
                val updated = _localMovies.value.toMutableList().apply {
                    removeAll { it.id == generatedId }
                    add(0, finalMovie)
                }
                _localMovies.value = updated
                Result.success(generatedId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding movie: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateMovie(movie: Movie): Result<Unit> {
        val db = firestore
        val movieWithTime = movie.copy(updatedAt = System.currentTimeMillis())

        return try {
            if (db != null && movie.id.isNotBlank()) {
                db.collection(COLLECTION_MOVIES).document(movie.id)
                    .set(movieWithTime.toMap())
                    .await()
            }
            val updated = _localMovies.value.map {
                if (it.id == movie.id) movieWithTime else it
            }
            _localMovies.value = updated
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating movie: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteMovie(movieId: String): Result<Unit> {
        val db = firestore
        return try {
            if (db != null && movieId.isNotBlank()) {
                db.collection(COLLECTION_MOVIES).document(movieId).delete().await()
            }
            _localMovies.value = _localMovies.value.filterNot { it.id == movieId }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting movie: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun togglePublishStatus(movieId: String, isPublished: Boolean): Result<Unit> {
        val db = firestore
        return try {
            if (db != null && movieId.isNotBlank()) {
                db.collection(COLLECTION_MOVIES).document(movieId)
                    .update("published", isPublished, "updatedAt", System.currentTimeMillis())
                    .await()
            }
            _localMovies.value = _localMovies.value.map {
                if (it.id == movieId) it.copy(published = isPublished, updatedAt = System.currentTimeMillis()) else it
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling publish: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun seedSampleMoviesIfRemoteEmpty() {
        val db = firestore ?: return
        try {
            val snapshot = db.collection(COLLECTION_MOVIES).limit(1).get().await()
            if (snapshot.isEmpty) {
                for (movie in getInitialSampleMovies()) {
                    db.collection(COLLECTION_MOVIES).document(movie.id).set(movie.toMap()).await()
                }
                Log.d(TAG, "Seeded initial movies into Firestore successfully.")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Seeding movies skipped or failed: ${e.message}")
        }
    }

    // =========================================================================
    // SOCIAL MEDIA FLOW & CRUD
    // =========================================================================

    fun getSocialMediaFlow(): Flow<List<SocialMedia>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(_localSocialMedia.value)
            awaitClose { }
            return@callbackFlow
        }

        var listenerRegistration: ListenerRegistration? = null
        try {
            listenerRegistration = db.collection(COLLECTION_SOCIAL)
                .orderBy("order", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(_localSocialMedia.value)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val links = snapshot.documents.mapNotNull { doc ->
                            try {
                                SocialMedia(
                                    id = doc.id,
                                    platformName = doc.getString("platformName") ?: "",
                                    url = doc.getString("url") ?: "",
                                    iconType = doc.getString("iconType") ?: "website",
                                    enabled = doc.getBoolean("enabled") ?: true,
                                    order = doc.getLong("order")?.toInt() ?: 0
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        _localSocialMedia.value = links
                        trySend(links)
                    } else {
                        trySend(_localSocialMedia.value)
                    }
                }
        } catch (e: Exception) {
            trySend(_localSocialMedia.value)
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    suspend fun saveSocialMedia(social: SocialMedia): Result<Unit> {
        val db = firestore
        val finalId = if (social.id.isNotBlank()) social.id else "sm_${System.currentTimeMillis()}"
        val finalSocial = social.copy(id = finalId)

        return try {
            if (db != null) {
                db.collection(COLLECTION_SOCIAL).document(finalId)
                    .set(finalSocial.toMap())
                    .await()
            }
            val updated = _localSocialMedia.value.toMutableList().apply {
                removeAll { it.id == finalId }
                add(finalSocial)
                sortBy { it.order }
            }
            _localSocialMedia.value = updated
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving social media: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteSocialMedia(id: String): Result<Unit> {
        val db = firestore
        return try {
            if (db != null && id.isNotBlank()) {
                db.collection(COLLECTION_SOCIAL).document(id).delete().await()
            }
            _localSocialMedia.value = _localSocialMedia.value.filterNot { it.id == id }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting social media: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // AD SETTINGS FLOW & CRUD
    // =========================================================================

    fun getAdSettingsFlow(): Flow<AdSettings> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(_localAdSettings.value)
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_APP_SETTINGS).document("adSettings")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(_localAdSettings.value)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val settings = AdSettings(
                            interstitialEnabled = snapshot.getBoolean("interstitialEnabled") ?: true,
                            minIntervalSeconds = snapshot.getLong("minIntervalSeconds") ?: 60L,
                            adsOnMovieDetails = snapshot.getBoolean("adsOnMovieDetails") ?: true,
                            adsBeforePlayback = snapshot.getBoolean("adsBeforePlayback") ?: true,
                            interstitialAdUnitId = snapshot.getString("interstitialAdUnitId") ?: AdSettings.DEFAULT_INTERSTITIAL_UNIT_ID,
                            testMode = snapshot.getBoolean("testMode") ?: false
                        )
                        _localAdSettings.value = settings
                        trySend(settings)
                    } else {
                        trySend(_localAdSettings.value)
                    }
                }
        } catch (e: Exception) {
            trySend(_localAdSettings.value)
        }

        awaitClose {
            listener?.remove()
        }
    }

    suspend fun saveAdSettings(settings: AdSettings): Result<Unit> {
        val db = firestore
        return try {
            if (db != null) {
                db.collection(COLLECTION_APP_SETTINGS).document("adSettings")
                    .set(settings.toMap())
                    .await()
            }
            _localAdSettings.value = settings
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving ad settings: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // ADMIN AUTHORIZATION & VERIFICATION
    // =========================================================================

    suspend fun verifyAdminCredentials(adminId: String, password: String): Boolean {
        // 1. Check development master credentials: Admin ID: "Karti", Password: "Karti@7878"
        if (adminId.equals("Karti", ignoreCase = true) && password == "Karti@7878") {
            return true
        }

        // 2. Check Firestore "admins/{adminId}" document if provisioned
        val db = firestore
        if (db != null) {
            try {
                val doc = db.collection(COLLECTION_ADMINS).document(adminId.lowercase()).get().await()
                if (doc.exists()) {
                    val storedPassword = doc.getString("password")
                    val isSuperAdmin = doc.getBoolean("isActive") ?: true
                    if (storedPassword == password && isSuperAdmin) {
                        return true
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error checking remote admin doc: ${e.message}")
            }
        }

        return false
    }

    // =========================================================================
    // USER SESSION SYNC
    // =========================================================================

    suspend fun syncUserSession(session: UserSession) {
        val db = firestore ?: return
        if (session.userId.isBlank()) return

        try {
            db.collection(COLLECTION_USERS).document(session.userId)
                .set(session.toMap())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "User session sync skipped or failed: ${e.message}")
        }
    }
}
