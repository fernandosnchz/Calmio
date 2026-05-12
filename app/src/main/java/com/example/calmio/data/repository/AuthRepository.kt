package com.example.calmio.data.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String, name: String): Result<Unit>
    suspend fun sendPasswordReset(email: String): Result<Unit>
}