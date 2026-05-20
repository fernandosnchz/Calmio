package com.example.calmio.data.repository

import com.example.calmio.viewmodel.SesionEstresMemoria
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreSesionRepository : SesionRepository {

    private val db = FirebaseFirestore.getInstance()

    override fun obtenerSesiones(userId: String): Flow<List<SesionEstresMemoria>> = callbackFlow {
        val listener = db.collection("users")
            .document(userId)
            .collection("sesiones_estres")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val sesiones = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        SesionEstresMemoria(
                            fecha         = doc.getString("fecha") ?: "",
                            juego         = doc.getString("juego") ?: "",
                            estresAntes   = (doc.getLong("estresAntes") ?: 0).toInt(),
                            estresDespues = (doc.getLong("estresDespues") ?: 0).toInt()
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(sesiones)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun guardarSesion(userId: String, sesion: SesionEstresMemoria): Result<Unit> {
        return try {
            val data = hashMapOf(
                "fecha"         to sesion.fecha,
                "juego"         to sesion.juego,
                "estresAntes"   to sesion.estresAntes,
                "estresDespues" to sesion.estresDespues,
                "timestamp"     to com.google.firebase.Timestamp.now()
            )
            db.collection("users")
                .document(userId)
                .collection("sesiones_estres")
                .add(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}