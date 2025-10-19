package com.example.migarage.ui.signin

import androidx.lifecycle.ViewModel




import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SignInUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val isSignedIn: Boolean = false
)

class SignInViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _state = MutableStateFlow(SignInUiState())
    val state = _state.asStateFlow()

    fun checkSession() {
        if (auth.currentUser != null) {
            _state.value = _state.value.copy(isSignedIn = true)
        }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { _state.value = SignInUiState(isSignedIn = true) }
            .addOnFailureListener { e -> _state.value = SignInUiState(error = e.message) }
    }
}
