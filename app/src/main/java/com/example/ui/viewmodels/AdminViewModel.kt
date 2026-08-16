package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdManager
import com.example.auth.AuthRepository
import com.example.auth.AuthResult
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

data class AdminUiState(
    val isAuthenticated: Boolean = false,
    val isAuthenticating: Boolean = false,
    val authError: String? = null,
    val movies: List<Movie> = emptyList(),
    val socialMediaLinks: List<SocialMedia> = emptyList(),
    val adSettings: AdSettings = AdSettings(),
    val userSession: UserSession = UserSession(),
    val actionMessage: String? = null,
    val isProcessing: Boolean = false
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreRepo = FirestoreRepository.getInstance()
    private val sessionManager = SessionManager.getInstance(application)
    private val authRepository = AuthRepository(application, sessionManager, firestoreRepo)
    private val adManager = AdManager.getInstance(application)

    private val _isAuthenticating = MutableStateFlow(false)
    private val _authError = MutableStateFlow<String?>(null)
    private val _actionMessage = MutableStateFlow<String?>(null)
    private val _isProcessing = MutableStateFlow(false)

    val isAuthenticating = _isAuthenticating.asStateFlow()
    val authError = _authError.asStateFlow()
    val actionMessage = _actionMessage.asStateFlow()
    val isProcessing = _isProcessing.asStateFlow()

    val uiState: StateFlow<AdminUiState> = combine(
        sessionManager.sessionState,
        firestoreRepo.getMoviesFlow(),
        firestoreRepo.getSocialMediaFlow(),
        firestoreRepo.getAdSettingsFlow(),
        _isAuthenticating,
        _authError,
        _actionMessage,
        _isProcessing
    ) { params ->
        val session = params[0] as UserSession
        @Suppress("UNCHECKED_CAST")
        val movies = params[1] as List<Movie>
        @Suppress("UNCHECKED_CAST")
        val social = params[2] as List<SocialMedia>
        val adSettings = params[3] as AdSettings
        val isAuth = params[4] as Boolean
        val authErr = params[5] as String?
        val actMsg = params[6] as String?
        val isProc = params[7] as Boolean

        AdminUiState(
            isAuthenticated = session.isAdmin,
            isAuthenticating = isAuth,
            authError = authErr,
            movies = movies,
            socialMediaLinks = social,
            adSettings = adSettings,
            userSession = session,
            actionMessage = actMsg,
            isProcessing = isProc
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminUiState()
    )

    fun loginAdmin(adminId: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authError.value = null

            when (val result = authRepository.loginAdmin(adminId, password)) {
                is AuthResult.Success -> {
                    _isAuthenticating.value = false
                    _actionMessage.value = "Welcome Admin ${result.userSession.adminName}!"
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _isAuthenticating.value = false
                    _authError.value = result.message
                }
            }
        }
    }

    fun logout() {
        authRepository.logoutAdmin()
        _actionMessage.value = "Admin logged out successfully."
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun clearAuthError() {
        _authError.value = null
    }

    // =========================================================================
    // MOVIE CRUD
    // =========================================================================

    fun saveMovie(movie: Movie, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = if (movie.id.isBlank()) {
                firestoreRepo.addMovie(movie)
            } else {
                firestoreRepo.updateMovie(movie).map { movie.id }
            }

            _isProcessing.value = false
            if (result.isSuccess) {
                _actionMessage.value = "Movie '${movie.name}' saved successfully!"
                onComplete(true)
            } else {
                _actionMessage.value = "Error saving movie: ${result.exceptionOrNull()?.message}"
                onComplete(false)
            }
        }
    }

    fun deleteMovie(movieId: String, movieName: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = firestoreRepo.deleteMovie(movieId)
            _isProcessing.value = false

            if (result.isSuccess) {
                _actionMessage.value = "Movie '$movieName' deleted successfully."
            } else {
                _actionMessage.value = "Failed to delete movie: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun togglePublishStatus(movieId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            val newStatus = !currentStatus
            val result = firestoreRepo.togglePublishStatus(movieId, newStatus)
            if (result.isSuccess) {
                _actionMessage.value = if (newStatus) "Movie published!" else "Movie unpublished."
            } else {
                _actionMessage.value = "Failed to update publish status."
            }
        }
    }

    // =========================================================================
    // SOCIAL MEDIA CRUD
    // =========================================================================

    fun saveSocialMedia(social: SocialMedia) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = firestoreRepo.saveSocialMedia(social)
            _isProcessing.value = false

            if (result.isSuccess) {
                _actionMessage.value = "Social link '${social.platformName}' updated."
            } else {
                _actionMessage.value = "Failed to save social media link."
            }
        }
    }

    fun deleteSocialMedia(id: String, name: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = firestoreRepo.deleteSocialMedia(id)
            _isProcessing.value = false

            if (result.isSuccess) {
                _actionMessage.value = "Social link '$name' removed."
            } else {
                _actionMessage.value = "Failed to delete social link."
            }
        }
    }

    // =========================================================================
    // AD SETTINGS
    // =========================================================================

    fun saveAdSettings(settings: AdSettings) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = firestoreRepo.saveAdSettings(settings)
            adManager.updateSettings(settings)
            _isProcessing.value = false

            if (result.isSuccess) {
                _actionMessage.value = "Advertisement settings updated successfully!"
            } else {
                _actionMessage.value = "Failed to save advertisement settings."
            }
        }
    }
}
