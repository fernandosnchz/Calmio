package com.example.calmio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmio.data.repository.FirebaseAuthRepository
import com.example.calmio.data.repository.FirestoreUserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Toda la información que necesita la pantalla de registro, en una sola caja.
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val registerSuccess: Boolean = false
)

class RegisterViewModel : ViewModel() {

    private val authRepo = FirebaseAuthRepository()
    private val userRepo = FirestoreUserRepository()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    fun onRegisterClick() {
        val state = _uiState.value

        when {
            state.name.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "El nombre no puede estar vacío") }
                return
            }
            state.email.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "El email no puede estar vacío") }
                return
            }
            state.password.length < 6 -> {
                _uiState.update {
                    it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres")
                }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepo.register(state.email, state.password, state.name)
                .onSuccess {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        userRepo.crearPerfil(userId, state.name, state.email)
                    }
                    _uiState.update { it.copy(isLoading = false, registerSuccess = true) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Este email ya está registrado")
                    }
                }
        }
    }
}