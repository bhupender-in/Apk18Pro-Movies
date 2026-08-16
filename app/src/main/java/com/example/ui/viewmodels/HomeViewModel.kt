package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdManager
import com.example.auth.AuthRepository
import com.example.auth.SessionManager
import com.example.firebase.FirestoreRepository
import com.example.models.AdSettings
import com.example.models.Movie
import com.example.models.SocialMedia
import com.example.models.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredMovies: List<Movie> = emptyList(),
    val latestMovies: List<Movie> = emptyList(),
    val allMovies: List<Movie> = emptyList(),
    val filteredMovies: List<Movie> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val socialMediaLinks: List<SocialMedia> = emptyList(),
    val userSession: UserSession = UserSession(),
    val errorMessage: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreRepo = FirestoreRepository.getInstance()
    private val sessionManager = SessionManager.getInstance(application)
    private val authRepository = AuthRepository(application, sessionManager, firestoreRepo)
    private val adManager = AdManager.getInstance(application)

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("All")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        firestoreRepo.getMoviesFlow(),
        firestoreRepo.getSocialMediaFlow(),
        firestoreRepo.getAdSettingsFlow(),
        sessionManager.sessionState,
        _selectedCategory
    ) { movies, socialList, adSettings, session, category ->
        adManager.updateSettings(adSettings)

        // Only show published movies to normal users (admin sees all)
        val visibleMovies = if (session.isAdmin) {
            movies
        } else {
            movies.filter { it.published }
        }

        val featured = visibleMovies.filter { it.featured }
        val latest = visibleMovies.sortedByDescending { it.createdAt }.take(10)

        val distinctCategories = listOf("All") + visibleMovies
            .map { it.category.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val filtered = if (category == "All") {
            visibleMovies
        } else {
            visibleMovies.filter { it.category.equals(category, ignoreCase = true) }
        }

        HomeUiState(
            isLoading = false,
            featuredMovies = if (featured.isNotEmpty()) featured else visibleMovies.take(3),
            latestMovies = latest,
            allMovies = visibleMovies,
            filteredMovies = filtered,
            categories = distinctCategories,
            selectedCategory = category,
            socialMediaLinks = socialList.filter { it.enabled },
            userSession = session,
            errorMessage = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            // Seed sample movies into Firestore if remote database is empty
            firestoreRepo.seedSampleMoviesIfRemoteEmpty()
            // Sync guest session
            authRepository.syncGuestUser()
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectLanguage(language: String) {
        _selectedLanguage.value = language
    }

    fun logoutAdmin() {
        authRepository.logoutAdmin()
    }
}
