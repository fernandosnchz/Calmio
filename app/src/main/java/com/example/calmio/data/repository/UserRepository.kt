package com.example.calmio.data.repository

interface UserRepository {
    suspend fun crearPerfil(userId: String, nombre: String, email: String): Result<Unit>
    suspend fun obtenerPerfil(userId: String): Result<Map<String, Any>>
    suspend fun eliminarPerfil(userId: String): Result<Unit>
}