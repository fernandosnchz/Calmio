package com.example.calmio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmio.data.model.EntradaDiarioMemoria
import com.example.calmio.data.repository.FirestoreEntradaDiarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class DiarioViewModel : ViewModel() {

    private val repo = FirestoreEntradaDiarioRepository()

    private val _userId = MutableStateFlow(FirebaseAuth.getInstance().currentUser?.uid)

    val todasLasEntradas: StateFlow<List<EntradaDiarioMemoria>> = _userId
        .flatMapLatest { uid ->
            if (uid != null) repo.obtenerEntradas(uid)
            else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val entradasDeHoy: StateFlow<List<EntradaDiarioMemoria>> = todasLasEntradas
        .map { entradas ->
            val inicio = inicioDiaTimestamp()
            val fin    = finDiaTimestamp()
            entradas.filter { it.fechaTimestamp in inicio..fin }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun guardarEntrada(preocupacion: String, fueronBien: String, pensamiento: String) {
        if (preocupacion.isBlank() && fueronBien.isBlank() && pensamiento.isBlank()) return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val entrada = EntradaDiarioMemoria(
            preocupacion     = preocupacion.trim(),
            fueronBien       = fueronBien.trim(),
            pensamientoLibre = pensamiento.trim()
        )
        viewModelScope.launch {
            repo.guardarEntrada(userId, entrada)
        }
    }

    fun recargarUsuario() {
        _userId.value = FirebaseAuth.getInstance().currentUser?.uid
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