package com.example.auth

import android.content.Context
import com.example.firebase.FirestoreRepository
import com.example.models.UserSession
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val userSession: UserSession) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val context: Context,
    private val sessionManager: SessionManager = SessionManager.getInstance(context),
    private val firestoreRepo: FirestoreRepository = FirestoreRepository.getInstance()
) {
    private val firebaseAuth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    /**
     * Authenticate Admin using Admin ID & Password.
     * Checks Firestore "admins/{adminId}" document or initial development credentials securely,
     * and attempts Firebase Auth if configured.
     */
    suspend fun loginAdmin(adminId: String, password: String): AuthResult {
        val cleanAdminId = adminId.trim()
        val cleanPassword = password.trim()

        if (cleanAdminId.isBlank() || cleanPassword.isBlank()) {
            return AuthResult.Error("Please enter both Admin ID and Password.")
        }

        try {
            // Check against Firestore admins collection or fallback verification
            val isAuthorized = firestoreRepo.verifyAdminCredentials(cleanAdminId, cleanPassword)

            if (isAuthorized) {
                // If Firebase Auth is active, attempt silent/admin sign-in or custom token if available
                try {
                    val emailFormat = if (cleanAdminId.contains("@")) cleanAdminId else "${cleanAdminId.lowercase()}@apk18pro.com"
                    firebaseAuth?.signInWithEmailAndPassword(emailFormat, cleanPassword)?.await()
                } catch (ignored: Exception) {
                    // Firebase Auth might not have this email registered yet; authorization granted via Firestore/secure verification
                }

                sessionManager.setAdminLoggedIn(cleanAdminId)
                val updatedSession = sessionManager.loadSession()

                // Sync session record to Firestore
                try {
                    firestoreRepo.syncUserSession(updatedSession)
                } catch (ignored: Exception) {}

                return AuthResult.Success(updatedSession)
            } else {
                return AuthResult.Error("Invalid Admin ID or Password. Access denied.")
            }
        } catch (e: Exception) {
            return AuthResult.Error("Login error: ${e.localizedMessage ?: "Unknown authentication error"}")
        }
    }

    fun logoutAdmin(): UserSession {
        try {
            firebaseAuth?.signOut()
        } catch (ignored: Exception) {}

        sessionManager.logoutAdmin()
        return sessionManager.loadSession()
    }

    suspend fun syncGuestUser(): UserSession {
        val session = sessionManager.loadSession()
        try {
            firestoreRepo.syncUserSession(session)
        } catch (ignored: Exception) {}
        return session
    }
}
