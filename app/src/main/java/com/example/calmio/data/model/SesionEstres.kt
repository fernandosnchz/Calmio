package com.example.calmio.data.model

// Modelo de datos de una sesión de estrés (antes y después de jugar).
// Vive en la capa de datos para que el repositorio no dependa del ViewModel.
data class SesionEstresMemoria(
    val fecha: String = "",
    val juego: String = "",
    val estresAntes: Int = 0,
    val estresDespues: Int = 0
)