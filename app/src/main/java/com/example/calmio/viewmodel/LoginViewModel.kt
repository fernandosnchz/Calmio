package com.example.calmio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmio.data.repository.AuthRepository
import com.example.calmio.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Toda la información que necesita la pantalla de login, en una sola caja.
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    // Versión privada: solo el ViewModel puede modificarla.
    private val _uiState = MutableStateFlow(LoginUiState())
    // Versión pública: la pantalla solo puede leerla.
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    fun onLoginClick() {
        val state = _uiState.value

        if (state.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El email no puede estar vacío") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update {
                it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.login(state.email, state.password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Email o contraseña incorrectos"
                        )
                    }
                }
        }
    }
}