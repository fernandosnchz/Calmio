package com.example.calmio.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EntradaDiarioDao {

    @Insert
    suspend fun insertar(entrada: EntradaDiario)

    @Query("SELECT * FROM entradas_diario ORDER BY fechaTimestamp DESC")
    fun obtenerTodas(): Flow<List<EntradaDiario>>

    // Entradas de hoy (para el resumen automático en DiarioScreen)
    @Query("""
        SELECT * FROM entradas_diario
        WHERE fechaTimestamp >= :inicioDia AND fechaTimestamp < :finDia
        ORDER BY fechaTimestamp DESC
    """)
    fun obtenerDeHoy(inicioDia: Long, finDia: Long): Flow<List<EntradaDiario>>
}