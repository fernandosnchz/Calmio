package com.example.calmio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmio.data.repository.FirestoreSesionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SesionEstresMemoria(
    val fecha: String = "",
    val juego: String = "",
    val estresAntes: Int = 0,
    val estresDespues: Int = 0
)

class StressViewModel : ViewModel() {

    private val repo = FirestoreSesionRepository()

    private val _userId = MutableStateFlow(FirebaseAuth.getInstance().currentUser?.uid)

    val todasLasSesiones: StateFlow<List<SesionEstresMemoria>> = _userId
        .flatMapLatest { uid ->
            if (uid != null) repo.obtenerSesiones(uid)
            else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val partidasPorJuego: StateFlow<Map<String, Int>> = todasLasSesiones
        .map { sesiones -> sesiones.groupingBy { it.juego }.eachCount() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Cuando Firestore ya responde al menos una vez no repite login
    val cargado: StateFlow<Boolean> = todasLasSesiones
        .map { true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun yaRegistroHoy(): Boolean {
        val hoy = fechaHoy()
        return todasLasSesiones.value.any { it.fecha == hoy }
    }

    fun guardarSesion(juego: String, estresAntes: Int, estresDespues: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val sesion = SesionEstresMemoria(
            fecha         = fechaHoy(),
            juego         = juego,
            estresAntes   = estresAntes,
            estresDespues = estresDespues
        )
        viewModelScope.launch {
            repo.guardarSesion(userId, sesion)
        }
    }

    fun yaJugoHoy(): Boolean {
        val hoy = fechaHoy()
        return todasLasSesiones.value.any { it.fecha == hoy }
    }

    fun recargarUsuario() {
        _userId.value = FirebaseAuth.getInstance().currentUser?.uid
    }

    private fun fechaHoy(): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
}