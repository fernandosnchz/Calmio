package com.example.calmio.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.media.MediaPlayer
import com.example.calmio.R
import com.example.calmio.ui.theme.Crema
import com.example.calmio.ui.theme.VerdeMenta
import com.example.calmio.ui.theme.VerdeSalvia
import kotlinx.coroutines.delay

// Fases del ciclo de respiración
private enum class BreathPhase(val label: String, val durationMs: Long) {
    INHALE("Inhala", 4000L),
    HOLD("Sostén", 4000L),
    EXHALE("Exhala", 6000L);

    fun next(): BreathPhase = when (this) {
        INHALE -> HOLD
        HOLD   -> EXHALE
        EXHALE -> INHALE
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingGameScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var phase by remember { mutableStateOf(BreathPhase.INHALE) }
    var cycleCount by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.breathing_game)
        mediaPlayer.isLooping = true
        mediaPlayer.setVolume(0.9f, 0.9f)
        mediaPlayer.start()

        // onDispose se ejecuta cuando el usuario sale de la pantalla
        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    LaunchedEffect(phase) {
        delay(phase.durationMs)
        if (phase == BreathPhase.EXHALE) cycleCount++
        phase = phase.next()
    }

    // Escala del círculo
    val targetScale = when (phase) {
        BreathPhase.INHALE -> 1f
        BreathPhase.HOLD   -> 1f
        BreathPhase.EXHALE -> 0.45f
    }
    val animSpec: AnimationSpec<Float> = when (phase) {
        BreathPhase.INHALE -> tween(
            durationMillis = BreathPhase.INHALE.durationMs.toInt(),
            easing = EaseInOutCubic
        )
        BreathPhase.HOLD   -> snap()   // sin animación durante la retención
        BreathPhase.EXHALE -> tween(
            durationMillis = BreathPhase.EXHALE.durationMs.toInt(),
            easing = EaseInOutCubic
        )
    }
    val circleScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = animSpec,
        label = "breathCircle"
    )

    // Opacidad del halo exterior
    val haloAlpha by rememberInfiniteTransition(label = "halo").animateFloat(
        initialValue = 0.15f,
        targetValue  = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    // Color del texto de fase
    val phaseColor = when (phase) {
        BreathPhase.INHALE -> VerdeSalvia
        BreathPhase.HOLD   -> Color(0xFF7A9E87)
        BreathPhase.EXHALE -> VerdeMenta.copy(alpha = 0.85f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🌬️ Respiración",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VerdeSalvia
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = VerdeSalvia
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Crema
                )
            )
        },
        containerColor = Crema
    ) { innerPadding ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Crema, VerdeMenta.copy(alpha = 0.25f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // ── Contador de ciclos ────────────────────────────────────────
                if (cycleCount > 0) {
                    Text(
                        text = "Ciclos completados: $cycleCount",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(45.dp))
                }

                // ── Círculo animado ───────────────────────────────────────────
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(260.dp)
                ) {
                    // Halo exterior
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(CircleShape)
                            .background(VerdeSalvia.copy(alpha = haloAlpha))
                    )

                    // Círculo principal que se expande/contrae
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(circleScale)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        VerdeMenta.copy(alpha = 0.80f),
                                        VerdeSalvia.copy(alpha = 0.90f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Círculo interior decorativo
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // ── Etiqueta de fase ──────────────────────────────────────────
                Text(
                    text = phase.label,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = phaseColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Instrucción secundaria ────────────────────────────────────
                Text(
                    text = when (phase) {
                        BreathPhase.INHALE -> "Respira lentamente por la nariz"
                        BreathPhase.HOLD   -> "Mantén el aire con calma"
                        BreathPhase.EXHALE -> "Suelta el aire por la boca"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(56.dp))
            }
        }
    }
}