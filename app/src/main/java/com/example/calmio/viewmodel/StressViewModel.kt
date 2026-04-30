package com.example.calmio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmio.data.CalmioDatabase
import com.example.calmio.data.SesionEstres
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StressViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = CalmioDatabase.getInstance(application).sesionDao()

    // Todas las sesiones como StateFlow — la UI se actualiza sola cuando cambian
    val todasLasSesiones: StateFlow<List<SesionEstres>> = dao.obtenerTodas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derivado de todasLasSesiones — un solo observer en Room
    val partidasPorJuego: StateFlow<Map<String, Int>> = todasLasSesiones
        .map { sesiones -> sesiones.groupingBy { it.juego }.eachCount() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Devuelve true si el usuario ya registró su estrés hoy
    suspend fun yaRegistroHoy(): Boolean {
        val hoy = fechaHoy()
        return dao.obtenerPorFecha(hoy).isNotEmpty()
    }

    // Guarda una sesión completa (antes + después + juego)
    fun guardarSesion(juego: String, estresAntes: Int, estresDespues: Int) {
        viewModelScope.launch {
            dao.insertar(
                SesionEstres(
                    fecha         = fechaHoy(),
                    juego         = juego,
                    estresAntes   = estresAntes,
                    estresDespues = estresDespues
                )
            )
        }
    }

    suspend fun yaJugoHoy(): Boolean {
        val hoy = fechaHoy()
        return dao.obtenerPorFecha(hoy).isNotEmpty()
    }

    private fun fechaHoy(): String {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formato.format(Date())
    }
}