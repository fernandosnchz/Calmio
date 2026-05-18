package com.example.calmio.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.viewinterop.AndroidView
import com.example.calmio.game.JuegoArosView

// Esta pantalla es el puente entre Jetpack Compose y el juego Canvas tradicional.
@Composable
fun ArosScreen(onVolver: () -> Unit) {
    AndroidView(
        factory = { context ->
            JuegoArosView(context).also { juegoView ->
                juegoView.onVolverAlMenu = onVolver
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}