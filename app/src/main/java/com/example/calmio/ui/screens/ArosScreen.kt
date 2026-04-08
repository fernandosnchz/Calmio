package com.example.calmio.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.viewinterop.AndroidView
import com.example.calmio.game.JuegoArosView

// Esta pantalla es el puente entre Jetpack Compose y el juego Canvas tradicional.
// AndroidView permite incrustar una vista clásica de Android dentro de Compose.
//
// onVolver: callback que se ejecuta cuando el usuario pulsa el botón 🔙 del juego.
// Así el juego no sabe nada de navegación — solo avisa "quiero salir".
@Composable
fun ArosScreen(onVolver: () -> Unit) {
    AndroidView(
        factory = { context ->
            // Aquí se crea la vista del juego (Canvas tradicional)
            JuegoArosView(context).also { juegoView ->
                // Conectamos el callback de volver al menú
                // que definimos en Base.kt como onVolverAlMenu
                juegoView.onVolverAlMenu = onVolver
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}