package com.example.calmio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmio.data.CalmioDatabase
import com.example.calmio.data.EntradaDiario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class DiarioViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = CalmioDatabase.getInstance(application).entradaDiarioDao()

    // Todas las entradas (para historial futuro)
    val todasLasEntradas = dao.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Entradas de hoy
    val entradasDeHoy: Flow<List<EntradaDiario>> = dao.obtenerDeHoy(
        inicioDia = inicioDiaTimestamp(),
        finDia    = finDiaTimestamp()
    )

    fun guardarEntrada(
        preocupacion: String,
        fueronBien: String,
        pensamiento: String
    ) {
        if (preocupacion.isBlank() && fueronBien.isBlank() && pensamiento.isBlank()) return
        viewModelScope.launch {
            dao.insertar(
                EntradaDiario(
                    preocupacion     = preocupacion.trim(),
                    fueronBien       = fueronBien.trim(),
                    pensamientoLibre = pensamiento.trim()
                )
            )
        }
    }

    // ── Helpers de timestamp ─────────────────────────────────────────────────
    private fun inicioDiaTimestamp(): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun finDiaTimestamp(): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
}