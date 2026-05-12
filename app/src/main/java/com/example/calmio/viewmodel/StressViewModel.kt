package com.example.calmio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Modelo de datos en memoria (hasta migrar a Firestore en Semana 3)
data class SesionEstresMemoria(
    val fecha: String,
    val juego: String,
    val estresAntes: Int,
    val estresDespues: Int
)

class StressViewModel : ViewModel() {

    // Lista en memoria — se reemplazará por Firestore más adelante
    private val _todasLasSesiones = MutableStateFlow<List<SesionEstresMemoria>>(emptyList())
    val todasLasSesiones: StateFlow<List<SesionEstresMemoria>> = _todasLasSesiones

    val partidasPorJuego: StateFlow<Map<String, Int>> = _todasLasSesiones
        .map { sesiones -> sesiones.groupingBy { it.juego }.eachCount() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun yaRegistroHoy(): Boolean {
        val hoy = fechaHoy()
        return _todasLasSesiones.value.any { it.fecha == hoy }
    }

    fun guardarSesion(juego: String, estresAntes: Int, estresDespues: Int) {
        val nuevaSesion = SesionEstresMemoria(
            fecha         = fechaHoy(),
            juego         = juego,
            estresAntes   = estresAntes,
            estresDespues = estresDespues
        )
        _todasLasSesiones.value = _todasLasSesiones.value + nuevaSesion
    }

    fun yaJugoHoy(): Boolean {
        val hoy = fechaHoy()
        return _todasLasSesiones.value.any { it.fecha == hoy }
    }

    private fun fechaHoy(): String {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formato.format(Date())
    }
}