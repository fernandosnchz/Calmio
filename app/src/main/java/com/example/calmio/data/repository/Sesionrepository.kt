package com.example.calmio.data.repository

import com.example.calmio.viewmodel.SesionEstresMemoria
import kotlinx.coroutines.flow.Flow

interface SesionRepository {
    fun obtenerSesiones(userId: String): Flow<List<SesionEstresMemoria>>
    suspend fun guardarSesion(userId: String, sesion: SesionEstresMemoria): Result<Unit>
}