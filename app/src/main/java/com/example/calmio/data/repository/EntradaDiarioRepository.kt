package com.example.calmio.data.repository

import com.example.calmio.data.model.EntradaDiarioMemoria
import kotlinx.coroutines.flow.Flow

interface EntradaDiarioRepository {
    fun obtenerEntradas(userId: String): Flow<List<EntradaDiarioMemoria>>
    suspend fun guardarEntrada(userId: String, entrada: EntradaDiarioMemoria): Result<Unit>
}