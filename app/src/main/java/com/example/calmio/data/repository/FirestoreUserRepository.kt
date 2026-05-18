package com.example.calmio.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreUserRepository : UserRepository {

    private val db = FirebaseFirestore.getInstance()

    override suspend fun crearPerfil(
        userId: String,
        nombre: String,
        email: String
    ): Result<Unit> {
        return try {
            val perfil = hashMapOf(
                "nombre"         to nombre,
                "email"          to email,
                "fechaRegistro"  to com.google.firebase.Timestamp.now()
            )
            db.collection("users")
                .document(userId)
                .set(perfil)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerPerfil(userId: String): Result<Map<String, Any>> {
        return try {
            val doc = db.collection("users")
                .document(userId)
                .get()
                .await()
            if (doc.exists()) {
                Result.success(doc.data ?: emptyMap())
            } else {
                Result.failure(Exception("Perfil no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}