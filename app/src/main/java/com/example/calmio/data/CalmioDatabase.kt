package com.example.calmio.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// @Database: declara qué tablas tiene la base de datos y su versión.
// Si añades tablas nuevas, incrementas la versión.
@Database(entities = [SesionEstres::class], version = 1)
abstract class CalmioDatabase : RoomDatabase() {

    abstract fun sesionDao(): SesionDao

    companion object {
        // Singleton: garantiza que solo existe UNA instancia de la base de datos.
        // @Volatile: cualquier cambio es visible inmediatamente a todos los hilos.
        @Volatile
        private var INSTANCE: CalmioDatabase? = null

        fun getInstance(context: Context): CalmioDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    CalmioDatabase::class.java,
                    "calmio_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}