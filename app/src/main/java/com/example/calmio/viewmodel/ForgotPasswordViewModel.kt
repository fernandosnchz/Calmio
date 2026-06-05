package com.example.calmio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmio.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estados posibles de la pantalla de recuperar contraseña.
// La pantalla solo puede estar en uno de estos estados a la vez.
sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    object Success : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

class ForgotPasswordViewModel : ViewModel() {

    private val repo = FirebaseAuthRepository()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _uiState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val uiState: StateFlow<ForgotPasswordState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _email.value = value
    }

    fun onSendClick() {
        if (_email.value.isBlank()) {
            _uiState.value = ForgotPasswordState.Error("Introduce tu email")
            return
        }
        viewModelScope.launch {
            _uiState.value = ForgotPasswordState.Loading
            repo.sendPasswordReset(_email.value)
                .onSuccess { _uiState.value = ForgotPasswordState.Success }
                .onFailure { _uiState.value = ForgotPasswordState.Error("Email no encontrado") }
        }
    }
}