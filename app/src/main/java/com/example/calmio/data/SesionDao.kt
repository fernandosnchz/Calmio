package com.example.calmio.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// @Dao (Data Access Object): define las operaciones que puedes hacer con la tabla.
// Room genera automáticamente el código SQL por debajo.
@Dao
interface SesionDao {

    // Inserta una sesión nueva en la base de datos
    @Insert
    suspend fun insertar(sesion: SesionEstres)

    // Devuelve todas las sesiones ordenadas de más reciente a más antigua.
    // Flow: se actualiza automáticamente cuando cambian los datos.
    @Query("SELECT * FROM sesiones_estres ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<SesionEstres>>

    // Devuelve solo las sesiones del día de hoy para saber si ya jugó
    @Query("SELECT * FROM sesiones_estres WHERE fecha = :fecha")
    suspend fun obtenerPorFecha(fecha: String): List<SesionEstres>
}