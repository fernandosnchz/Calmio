package com.example.calmio.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calmio.ui.theme.Crema
import com.example.calmio.ui.theme.Terracota
import com.example.calmio.ui.theme.VerdeMenta
import com.example.calmio.ui.theme.VerdeSalvia

// Color adicional para el juego de respiración (azul sereno)
private val AzulSereno = Color(0xFF7EB8C9)

@Composable
fun GameSelectionScreen(
    modifier: Modifier = Modifier,
    onJuegoSeleccionado: (String) -> Unit
) {
    // Controla la animación de entrada escalonada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Crema, VerdeMenta.copy(alpha = 0.25f))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── Header ────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -20 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Bienvenido",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeSalvia
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Elige un juego para relajarte",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Etiqueta de sección ───────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 150))
            ) {
                Text(
                    text = "Juegos disponibles",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Tarjetas ──────────────────────────────────────────────────────
            val juegos = listOf(
                Triple("⭕", "Juego de Aros",       "Ensarta los aros en los postes"  ) to Pair(VerdeSalvia, "aros"),
                Triple("🫧", "Explotar Burbujas",   "Toca las burbujas para explotar" ) to Pair(VerdeMenta,  "mochis"),
                Triple("🎣", "Juego de Pesca",      "Captura peces en los anzuelos"   ) to Pair(Terracota,   "pesca"),
                Triple("🌬️", "Respiración Guiada", "Sigue el círculo y respira"      ) to Pair(AzulSereno,  "respiracion"),
            )

            juegos.forEachIndexed { index, (info, action) ->
                val (emoji, nombre, descripcion) = info
                val (color, ruta) = action

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400, delayMillis = 250 + index * 100)) +
                            slideInVertically(tween(400, delayMillis = 250 + index * 100)) { 30 }
                ) {
                    GameCard(
                        emoji = emoji,
                        nombre = nombre,
                        descripcion = descripcion,
                        color = color,
                        onClick = { onJuegoSeleccionado(ruta) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GameCard(
    emoji: String,
    nombre: String,
    descripcion: String,
    color: Color,
    onClick: () -> Unit
) {
    // Animación de escala al pulsar
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Card(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            // Borde sutil del color del juego
            .border(
                width = 1.5.dp,
                color = color.copy(alpha = 0.30f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Icono ─────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                color.copy(alpha = 0.20f),
                                color.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .border(1.dp, color.copy(alpha = 0.20f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // ── Texto ─────────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = descripcion,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Flecha con fondo circular ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "›",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}