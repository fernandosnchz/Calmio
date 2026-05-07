package com.example.calmio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.calmio.data.UserPreferences
import com.example.calmio.worker.ReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application)
    private val workManager = WorkManager.getInstance(application)

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode

    private val _notificationsEnabled = MutableStateFlow(false)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _avatarIndex = MutableStateFlow(0)
    val avatarIndex: StateFlow<Int> = _avatarIndex

    private val _reminderHour = MutableStateFlow(20)
    val reminderHour: StateFlow<Int> = _reminderHour

    private val _reminderMinute = MutableStateFlow(0)
    val reminderMinute: StateFlow<Int> = _reminderMinute

    init {
        viewModelScope.launch {
            _darkMode.value             = prefs.darkModeFlow.first()
            _notificationsEnabled.value = prefs.notificationsFlow.first()
            _avatarIndex.value          = prefs.avatarIndexFlow.first()
            _reminderHour.value         = prefs.reminderHourFlow.first()
            _reminderMinute.value       = prefs.reminderMinuteFlow.first()
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _darkMode.value = enabled
            prefs.setDarkMode(enabled)
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _notificationsEnabled.value = enabled
            prefs.setNotifications(enabled)
            if (enabled) {
                scheduleReminder(_reminderHour.value, _reminderMinute.value)
            } else {
                cancelReminder()
            }
        }
    }

    fun setAvatarIndex(index: Int) {
        viewModelScope.launch {
            _avatarIndex.value = index
            prefs.setAvatarIndex(index)
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            _reminderHour.value   = hour
            _reminderMinute.value = minute
            prefs.setReminderTime(hour, minute)
            // Reprograma solo si las notificaciones están activas
            if (_notificationsEnabled.value) {
                scheduleReminder(hour, minute)
            }
        }
    }

    // ── WorkManager: programa tarea diaria a la hora elegida ────────────────
    private fun scheduleReminder(hour: Int, minute: Int) {
        cancelReminder() // cancela cualquier tarea previa antes de crear una nueva

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

    // Calcula cuántos ms faltan hasta la próxima ocurrencia de hora:minuto
    private fun calcularDelay(hour: Int, minute: Int): Long {
        val ahora = Calendar.getInstance()
        val objetivo = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Si la hora ya pasó hoy, programar para mañana
        if (objetivo.before(ahora)) {
            objetivo.add(Calendar.DAY_OF_YEAR, 1)
        }
        return objetivo.timeInMillis - ahora.timeInMillis
    }

    companion object {
        const val REMINDER_TAG = "calmio_daily_reminder"
    }
}