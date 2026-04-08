package com.example.calmio.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        _errorMessage.value = null

        when {
            _email.value.isBlank() -> {
                _errorMessage.value = "El email no puede estar vacío"
            }
            _password.value.length < 6 -> {
                _errorMessage.value = "La contraseña debe tener al menos 6 caracteres"
            }
            _email.value == "admin@calmio.com" && _password.value == "123456" -> {
                onSuccess()
            }
            else -> {
                _errorMessage.value = "Email o contraseña incorrectos"
            }
        }
    }
}