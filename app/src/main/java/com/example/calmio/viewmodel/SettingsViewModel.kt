package com.example.calmio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.calmio.data.UserPreferences
import com.example.calmio.data.repository.FirestoreUserRepository
import com.example.calmio.worker.ReminderWorker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

// Toda la información que necesita la pantalla de ajustes, en una sola caja.
data class SettingsUiState(
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val avatarIndex: Int = 0,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val nombreUsuario: String = "",
    val emailUsuario: String = ""
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs       = UserPreferences(application)
    private val workManager = WorkManager.getInstance(application)
    private val userRepo    = FirestoreUserRepository()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    darkMode             = prefs.darkModeFlow.first(),
                    notificationsEnabled = prefs.notificationsFlow.first(),
                    avatarIndex          = prefs.avatarIndexFlow.first(),
                    reminderHour         = prefs.reminderHourFlow.first(),
                    reminderMinute       = prefs.reminderMinuteFlow.first()
                )
            }
        }
        cargarPerfil()
    }

    fun cargarPerfil() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            userRepo.obtenerPerfil(userId)
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            nombreUsuario = data["nombre"] as? String ?: "",
                            emailUsuario  = data["email"]  as? String ?: ""
                        )
                    }
                }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(darkMode = enabled) }
            prefs.setDarkMode(enabled)
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(notificationsEnabled = enabled) }
            prefs.setNotifications(enabled)
            val state = _uiState.value
            if (enabled) scheduleReminder(state.reminderHour, state.reminderMinute)
            else cancelReminder()
        }
    }

    fun setAvatarIndex(index: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(avatarIndex = index) }
            prefs.setAvatarIndex(index)
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(reminderHour = hour, reminderMinute = minute) }
            prefs.setReminderTime(hour, minute)
            if (_uiState.value.notificationsEnabled) scheduleReminder(hour, minute)
        }
    }

    private fun scheduleReminder(hour: Int, minute: Int) {
        cancelReminder()
        val delay = calcularDelay(hour, minute)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(REMINDER_TAG)
            .build()
        workManager.enqueueUniquePeriodicWork(
            REMINDER_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    private fun cancelReminder() {
        workManager.cancelAllWorkByTag(REMINDER_TAG)
    }

    private fun calcularDelay(hour: Int, minute: Int): Long {
        val ahora   = Calendar.getInstance()
        val objetivo = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (objetivo.before(ahora)) objetivo.add(Calendar.DAY_OF_YEAR, 1)
        return objetivo.timeInMillis - ahora.timeInMillis
    }

    companion object {
        const val REMINDER_TAG = "calmio_daily_reminder"
    }
}