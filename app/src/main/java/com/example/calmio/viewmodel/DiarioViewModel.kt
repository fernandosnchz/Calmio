package com.example.calmio.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

// Modelo en memoria
data class EntradaDiarioMemoria(
    val fechaTimestamp: Long = System.currentTimeMillis(),
    val preocupacion: String = "",
    val fueronBien: String = "",
    val pensamientoLibre: String = ""
)

class DiarioViewModel : ViewModel() {

    private val _todasLasEntradas = MutableStateFlow<List<EntradaDiarioMemoria>>(emptyList())
    val todasLasEntradas: StateFlow<List<EntradaDiarioMemoria>> = _todasLasEntradas

    val entradasDeHoy: StateFlow<List<EntradaDiarioMemoria>>
        get() {
            val inicio = inicioDiaTimestamp()
            val fin = finDiaTimestamp()
            val filtradas = _todasLasEntradas.value.filter {
                it.fechaTimestamp in inicio..fin
            }
            return MutableStateFlow(filtradas)
        }

    fun guardarEntrada(
        preocupacion: String,
        fueronBien: String,
        pensamiento: String
    ) {
        if (preocupacion.isBlank() && fueronBien.isBlank() && pensamiento.isBlank()) return
        val nueva = EntradaDiarioMemoria(
            preocupacion     = preocupacion.trim(),
            fueronBien       = fueronBien.trim(),
            pensamientoLibre = pensamiento.trim()
        )
        _todasLasEntradas.value = _todasLasEntradas.value + nueva
    }

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