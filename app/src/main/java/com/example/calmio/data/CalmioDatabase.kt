package com.example.calmio.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SesionEstres::class], version = 1)
abstract class CalmioDatabase : RoomDatabase() {

    abstract fun sesionDao(): SesionDao

    companion object {
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