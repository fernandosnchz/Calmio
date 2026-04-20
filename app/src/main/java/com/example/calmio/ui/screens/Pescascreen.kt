/**
 * =========================================================================================
 * ARCHIVO: PescaScreen.kt
 * Proyecto: Calmio
 * Ubicación: app/src/main/java/com/example/calmio/ui/screens/PescaScreen.kt
 * =========================================================================================
 * PROPÓSITO:
 * Pantalla Jetpack Compose que incrusta el minijuego de Pesca dentro de la app Calmio,
 * siguiendo el mismo patrón que el resto de pantallas de juego del proyecto (ArosScreen, etc.)
 *
 * CÓMO FUNCIONA:
 * Usa AndroidView para envolver el JuegoPescarView (Canvas tradicional) dentro
 * de la interfaz moderna de Compose, igual que hace el MainActivity del profesor.
 * =========================================================================================
 */
package com.example.calmio.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.calmio.game.JuegoPescarView

/**
 * Pantalla que muestra el minijuego de Pesca.
 *
 * @param onVolver Callback que se ejecuta cuando el usuario pulsa atrás,
 *                 para que la navegación de Calmio vuelva a GameSelectionScreen.
 */
@Composable
fun PescaScreen(onVolver: () -> Unit) {

    // Interceptamos el botón/gesto "Atrás" del sistema para no salir de la app
    BackHandler { onVolver() }

    AndroidView(
        factory = { context ->
            JuegoPescarView(context).also { vista ->
                // Conectamos el botón 🔙 del juego con la navegación de Calmio
                vista.onVolverAlMenu = { onVolver() }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}