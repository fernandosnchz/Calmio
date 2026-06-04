package com.example.calmio.data.repository

import com.example.calmio.data.model.EntradaDiarioMemoria
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreEntradaDiarioRepository : EntradaDiarioRepository {

    private val db = FirebaseFirestore.getInstance()

    override fun obtenerEntradas(userId: String): Flow<List<EntradaDiarioMemoria>> = callbackFlow {
        val listener = db.collection("users")
            .document(userId)
            .collection("entradas_diario")
            .orderBy("fechaTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val entradas = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        EntradaDiarioMemoria(
                            fechaTimestamp   = doc.getLong("fechaTimestamp") ?: System.currentTimeMillis(),
                            preocupacion     = doc.getString("preocupacion") ?: "",
                            fueronBien       = doc.getString("fueronBien") ?: "",
                            pensamientoLibre = doc.getString("pensamientoLibre") ?: ""
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(entradas)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun guardarEntrada(userId: String, entrada: EntradaDiarioMemoria): Result<Unit> {
        return try {
            val data = hashMapOf(
                "fechaTimestamp"   to entrada.fechaTimestamp,
                "preocupacion"     to entrada.preocupacion,
                "fueronBien"       to entrada.fueronBien,
                "pensamientoLibre" to entrada.pensamientoLibre,
                "timestamp"        to com.google.firebase.Timestamp.now()
            )
            db.collection("users")
                .document(userId)
                .collection("entradas_diario")
                .add(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}