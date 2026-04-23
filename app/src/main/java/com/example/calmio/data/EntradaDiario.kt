package com.example.calmio.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entradas_diario")
data class EntradaDiario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fechaTimestamp: Long = System.currentTimeMillis(),
    val preocupacion: String = "",
    val fueronBien: String = "",
    val pensamientoLibre: String = ""
)