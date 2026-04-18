package com.example.calmio.ui.screens

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calmio.R
import com.example.calmio.game.MotorMochis

// onVolver: igual que en ArosScreen, el juego avisa "quiero salir"
// y MainActivity decide qué hacer (ir a stress_despues o al menú)
@Composable
fun MochisScreen(onVolver: () -> Unit) {

    val context = LocalContext.current // NUEVO: necesitamos el contexto para los sonidos
    val motor = remember { MotorMochis() }
    var tamanyoPantalla by remember { mutableStateOf(IntSize.Zero) }
    var contadorFotogramas by remember { mutableStateOf(0) }
    var mostrarDialogoLimpiar by remember { mutableStateOf(false) }
    val medidorDeTexto = rememberTextMeasurer()
    val animacion = rememberInfiniteTransition()

    // NUEVO: Preparamos el SoundPool para el sonido de explosión
    // (igual que motorEfectos en Base.kt)
    val soundPool = remember {
        val atributos = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder().setMaxStreams(5).setAudioAttributes(atributos).build()
    }

    // NUEVO: Cargamos el sonido de explosión desde res/raw/
    val idSonidoExplosion = remember {
        soundPool.load(context, R.raw.sonido_explosion, 1)
    }

    // NUEVO: Conectamos el motor con el sonido de explosión
    LaunchedEffect(Unit) {
        motor.onExplotar = {
            soundPool.play(idSonidoExplosion, 1f, 1f, 0, 0, 1f)
        }
    }

    // NUEVO: Música de fondo con MediaPlayer (igual que en Base.kt)
    DisposableEffect(Unit) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.musica_mochis)
        mediaPlayer.isLooping = true
        mediaPlayer.setVolume(0.9f, 0.9f)
        mediaPlayer.start()

        // onDispose se ejecuta cuando el usuario sale de la pantalla
        // Es importante parar la música al salir, igual que hace surfaceDestroyed en Base.kt
        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
            soundPool.release()
        }
    }

    // Fondo animado
    val faseOla by animacion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val azulClaro = Color(0xFFE3F2FD)
    val azulOscuro = Color(0xFF64B5F6)
    val colorArriba = lerp(azulClaro, azulOscuro, faseOla)
    val colorAbajo = lerp(azulOscuro, azulClaro, faseOla)

    // Game loop (igual que el del profesor)
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                if (tamanyoPantalla != IntSize.Zero) {
                    motor.actualizarFisicas(
                        anchoPantalla = tamanyoPantalla.width.toFloat(),
                        altoPantalla = tamanyoPantalla.height.toFloat()
                    )
                    contadorFotogramas++
                }
            }
        }
    }

    if (mostrarDialogoLimpiar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoLimpiar = false },
            title = { Text("¿Borrar todos los emojis?") },
            confirmButton = {
                TextButton(onClick = {
                    motor.limpiarPantalla()
                    mostrarDialogoLimpiar = false
                }) { Text("Sí", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoLimpiar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(colorArriba, colorAbajo)
            ))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // NUEVO respecto al original: botón para volver al menú de Calmio
            TextButton(onClick = onVolver) {
                Text("🔙 Volver", fontSize = 16.sp, color = Color(0xFFE65100))
            }

            Text(
                text = "Explosiones: ${motor.puntuacion}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFFE65100)
            )

            IconButton(onClick = {
                if (motor.mochis.isNotEmpty()) mostrarDialogoLimpiar = true
            }) {
                Text("🧹", fontSize = 30.sp)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { nuevoTamanyo -> tamanyoPantalla = nuevoTamanyo }
                    .pointerInput(Unit) {
                        detectTapGestures { toque ->
                            motor.tocar(toque.x, toque.y)
                        }
                    }
            ) {
                val tiempoActual = System.currentTimeMillis()
                val frameActual = contadorFotogramas

                for (mochi in motor.mochis) {
                    if (mochi.explotado && mochi.y > 0) {
                        val milisegundosExplotado = tiempoActual - mochi.tiempoExplotado
                        val factorTamanyo = (milisegundosExplotado / 250f).coerceIn(0f, 1.25f)
                        val estiloTexto = TextStyle(fontSize = mochi.radio.sp * factorTamanyo)
                        val medidas = medidorDeTexto.measure("✨", style = estiloTexto)
                        drawText(
                            textLayoutResult = medidas,
                            topLeft = Offset(
                                x = mochi.x - (medidas.size.width / 2f),
                                y = mochi.y - (medidas.size.height / 2f)
                            )
                        )
                        if (factorTamanyo >= 1.25f) mochi.y = -250f
                    } else {
                        val estiloTexto = TextStyle(fontSize = mochi.radio.sp)
                        val medidas = medidorDeTexto.measure(mochi.emoji, style = estiloTexto)
                        drawText(
                            textLayoutResult = medidas,
                            topLeft = Offset(
                                x = mochi.x - (medidas.size.width / 2f),
                                y = mochi.y - (medidas.size.height / 2f)
                            )
                        )
                    }
                }
            }
        }
    }
}