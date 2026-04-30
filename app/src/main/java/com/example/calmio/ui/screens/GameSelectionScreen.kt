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

// Paleta pastel por juego (fondos de card)
private val BgAros        = Color(0xFFE8F5EF)   // verde muy suave
private val BgBurbujas    = Color(0xFFE6F2F8)   // azul cielo suave
private val BgPesca       = Color(0xFFFFF0E6)   // melocotón suave
private val BgRespiracion = Color(0xFFEAF0FB)   // lavanda-azul suave

private val AzulSereno    = Color(0xFF7EB8C9)

// Datos completos de cada juego
data class JuegoInfo(
    val emoji: String,
    val nombre: String,
    val descripcion: String,
    val beneficio: String,
    val accentColor: Color,
    val bgColor: Color,
    val ruta: String
)

private val listaJuegos = listOf(
    JuegoInfo("⭕", "Juego de Aros",      "Ensarta los aros en los postes",    "Foco & concentración", VerdeSalvia, BgAros,        "aros"),
    JuegoInfo("🫧", "Explotar Burbujas",  "Toca las burbujas para explotar",   "Reduce ansiedad",      AzulSereno,  BgBurbujas,    "mochis"),
    JuegoInfo("🎣", "Juego de Pesca",     "Captura peces en los anzuelos",     "Relajación",           Terracota,   BgPesca,       "pesca"),
    JuegoInfo("🌬️", "Respiración Guiada","Sigue el círculo y respira",        "Calma & equilibrio",   Color(0xFF6A9FD4), BgRespiracion, "respiracion"),
)

@Composable
fun GameSelectionScreen(
    modifier: Modifier = Modifier,
    // Recibe el conteo de partidas por ruta para determinar el juego favorito.
    // Pasa un mapa vacío si todavía no tienes estadísticas; el bloque de
    // "Juego destacado" no se mostrará.
    partidasPorJuego: Map<String, Int> = emptyMap(),
    onJuegoSeleccionado: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Determina el juego más jugado (si hay datos)
    val rutaFavorita = partidasPorJuego
        .filter { it.value > 0 }
        .maxByOrNull { it.value }
        ?.key
    val juegoFavorito = listaJuegos.find { it.ruta == rutaFavorita }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Crema, VerdeMenta.copy(alpha = 0.20f))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // ── Header ────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -20 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Bienvenido",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeSalvia
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Elige un juego para relajarte",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Juego destacado (solo si hay estadísticas) ────────────────────
            if (juegoFavorito != null) {
                Spacer(modifier = Modifier.height(32.dp))
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400, delayMillis = 100)) +
                            slideInVertically(tween(400, delayMillis = 100)) { 20 }
                ) {
                    FeaturedGameCard(juego = juegoFavorito, onClick = { onJuegoSeleccionado(juegoFavorito.ruta) })
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Tarjetas ──────────────────────────────────────────────────────
            listaJuegos.forEachIndexed { index, juego ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400, delayMillis = 250 + index * 100)) +
                            slideInVertically(tween(400, delayMillis = 250 + index * 100)) { 30 }
                ) {
                    GameCard(
                        juego = juego,
                        isFavorite = juego.ruta == rutaFavorita,
                        onClick = { onJuegoSeleccionado(juego.ruta) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tarjeta de juego destacado (hero card)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FeaturedGameCard(juego: JuegoInfo, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "featuredScale"
    )

    Card(
        onClick = { pressed = true; onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                width = 1.5.dp,
                color = juego.accentColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = juego.bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Badge "Más jugado"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(juego.accentColor.copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⭐ Tu favorito",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = juego.accentColor,
                    letterSpacing = 0.3.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icono grande
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    juego.accentColor.copy(alpha = 0.25f),
                                    juego.accentColor.copy(alpha = 0.10f)
                                )
                            )
                        )
                        .border(1.dp, juego.accentColor.copy(alpha = 0.20f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = juego.emoji, fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = juego.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = juego.descripcion,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Beneficio
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(juego.accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = juego.beneficio,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = juego.accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón "Jugar ahora"
            Button(
                onClick = { pressed = true; onClick() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = juego.accentColor,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Jugar ahora",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tarjeta de juego estándar (mejorada)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GameCard(
    juego: JuegoInfo,
    isFavorite: Boolean = false,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Card(
        onClick = { pressed = true; onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                width = 1.5.dp,
                color = juego.accentColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        // Fondo pastel propio de cada juego
        colors = CardDefaults.cardColors(containerColor = juego.bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),           // más padding interno
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Icono más grande ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(68.dp)           // aumentado de 56 → 68 dp
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                juego.accentColor.copy(alpha = 0.28f),
                                juego.accentColor.copy(alpha = 0.10f)
                            )
                        )
                    )
                    .border(1.dp, juego.accentColor.copy(alpha = 0.20f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = juego.emoji, fontSize = 32.sp)  // emoji más grande
            }

            Spacer(modifier = Modifier.width(16.dp))

            // ── Texto ─────────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                // Nombre
                Text(
                    text = juego.nombre,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                // Descripción: fuente más grande (13→15 sp)
                Text(
                    text = juego.descripcion,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Tag de beneficio
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(juego.accentColor.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = juego.beneficio,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = juego.accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── Flecha ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(juego.accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "›",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = juego.accentColor
                )
            }
        }
    }
}