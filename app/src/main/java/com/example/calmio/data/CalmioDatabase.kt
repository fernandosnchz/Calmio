package com.example.calmio.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities     = [SesionEstres::class, EntradaDiario::class],
    version      = 2,
    exportSchema = false
)
abstract class CalmioDatabase : RoomDatabase() {

    abstract fun sesionDao(): SesionDao
    abstract fun entradaDiarioDao(): EntradaDiarioDao

    companion object {
        @Volatile
        private var INSTANCE: CalmioDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS entradas_diario (
                        id               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        fechaTimestamp   INTEGER NOT NULL,
                        preocupacion     TEXT    NOT NULL DEFAULT '',
                        fueronBien       TEXT    NOT NULL DEFAULT '',
                        pensamientoLibre TEXT    NOT NULL DEFAULT ''
                    )
                """)
            }
        }

        fun getInstance(context: Context): CalmioDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    CalmioDatabase::class.java,
                    "calmio_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}