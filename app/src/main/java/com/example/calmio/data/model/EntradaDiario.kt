package com.example.calmio.data.model

// Modelo de datos de una entrada del diario emocional.
// Vive en la capa de datos para que el repositorio no dependa del ViewModel.
data class EntradaDiarioMemoria(
    val fechaTimestamp: Long = System.currentTimeMillis(),
    val preocupacion: String = "",
    val fueronBien: String = "",
    val pensamientoLibre: String = ""
)