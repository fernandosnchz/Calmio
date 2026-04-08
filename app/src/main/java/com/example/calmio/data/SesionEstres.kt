package com.example.calmio.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity le dice a Room que esta clase es una tabla en la base de datos.
// Cada propiedad es una columna.
@Entity(tableName = "sesiones_estres")
data class SesionEstres(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fecha: String,        // "05/04/2026"
    val juego: String,        // "Aros"
    val estresAntes: Int,     // 1-10
    val estresDespues: Int    // 1-10
)