package com.example.calmio.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calmio.data.SesionEstres
import com.example.calmio.ui.theme.Crema
import com.example.calmio.ui.theme.VerdeMenta
import com.example.calmio.ui.theme.VerdeSalvia
import com.example.calmio.viewmodel.StressViewModel

// Colores de la gráfica y estados
private val ColorAntes   = Color(0xFF4CAF50)
private val ColorDespues = Color(0xFFF44336)

// Emojis por nombre de juego
private val emojiPorJuego = mapOf(
    "aros"     to "⭕",
    "mochis"   to "🫧",
    "pesca"    to "🎣",
    "burbujas" to "🫧"
)


@Composable
fun HistorialScreen(
    stressViewModel: StressViewModel = viewModel()
) {
    val sesiones by stressViewModel.todasLasSesiones.collectAsState()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Crema, VerdeMenta.copy(alpha = 0.25f))
                )
            )
    ) {
        if (sesiones.isEmpty()) {
            // -- Estado vacío centrado -----------------------------------------
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("😌", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aún no tienes sesiones.\nJuega para ver tu evolución.",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // -- Header ----------------------------------------------------
                item {
                    Spacer(modifier = Modifier.height(28.dp))

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -20 }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "🌿 Tu evolución",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdeSalvia
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Estrés antes y después de jugar",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // -- Chips de resumen ------------------------------------------
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 100))
                    ) {
                        val promedioAntes   = sesiones.map { it.estresAntes }.average().toFloat()
                        val promedioDespues = sesiones.map { it.estresDespues }.average().toFloat()
                        val mejoras         = sesiones.count { it.estresDespues < it.estresAntes }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ResumenChip(
                                modifier = Modifier.weight(1f),
                                etiqueta = "Media antes",
                                valor = "%.1f".format(promedioAntes),
                                color = ColorAntes
                            )
                            ResumenChip(
                                modifier = Modifier.weight(1f),
                                etiqueta = "Media después",
                                valor = "%.1f".format(promedioDespues),
                                color = ColorDespues
                            )
                            ResumenChip(
                                modifier = Modifier.weight(1f),
                                etiqueta = "+ positivas",
                                valor = "$mejoras",
                                color = VerdeSalvia
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // -- Gráfica ---------------------------------------------------
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500, delayMillis = 200))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.9f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    LeyendaPunto(color = ColorAntes,   texto = "Antes")
                                    LeyendaPunto(color = ColorDespues, texto = "Después")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                GraficaEstres(sesiones = sesiones.reversed())
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // -- Título de lista -------------------------------------------
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 300))
                    ) {
                        Text(
                            text = "Sesiones recientes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // -- Sesiones --------------------------------------------------
                itemsIndexed(sesiones) { index, sesion ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(350, delayMillis = 350 + index * 60)) +
                                slideInVertically(tween(350, delayMillis = 350 + index * 60)) { 20 }
                    ) {
                        TarjetaSesion(sesion = sesion)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

// -- Chip de resumen ------------------------------------------------------------
@Composable
private fun ResumenChip(
    modifier: Modifier = Modifier,
    etiqueta: String,
    valor: String,
    color: Color
) {
    Card(
        modifier = modifier
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = valor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = etiqueta,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// -- Gráfica (igual que el original, sin cambios en lógica) ---------------------
@Composable
fun GraficaEstres(sesiones: List<SesionEstres>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        if (sesiones.size < 2) return@Canvas

        val pasoX        = size.width / (sesiones.size - 1).toFloat()
        val altoUnitario = size.height / 10f

        val pathAntes   = Path()
        val pathDespues = Path()

        sesiones.forEachIndexed { index, sesion ->
            val x        = index * pasoX
            val yAntes   = size.height - (sesion.estresAntes   * altoUnitario)
            val yDespues = size.height - (sesion.estresDespues * altoUnitario)

            if (index == 0) {
                pathAntes.moveTo(x, yAntes)
                pathDespues.moveTo(x, yDespues)
            } else {
                pathAntes.lineTo(x, yAntes)
                pathDespues.lineTo(x, yDespues)
            }

            // Puntos rellenos con borde blanco
            drawCircle(color = Color.White,        radius = 13f, center = Offset(x, yAntes))
            drawCircle(color = ColorAntes,         radius = 10f, center = Offset(x, yAntes))
            drawCircle(color = Color.White,        radius = 13f, center = Offset(x, yDespues))
            drawCircle(color = ColorDespues,       radius = 10f, center = Offset(x, yDespues))
        }

        drawPath(path = pathAntes,   color = ColorAntes,   style = Stroke(width = 3.5f))
        drawPath(path = pathDespues, color = ColorDespues, style = Stroke(width = 3.5f))
    }
}

// -- Tarjeta de sesión ----------------------------------------------------------
@Composable
fun TarjetaSesion(sesion: SesionEstres) {
    val mejoro       = sesion.estresDespues < sesion.estresAntes
    val emoji        = emojiPorJuego[sesion.juego.lowercase()] ?: "🎮"
    val colorAntes   = colorSegunEstres(sesion.estresAntes)
    val colorDespues = colorSegunEstres(sesion.estresDespues)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (mejoro) ColorAntes.copy(alpha = 0.25f)
                else ColorDespues.copy(alpha = 0.20f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del juego
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (mejoro) ColorAntes.copy(alpha = 0.10f)
                        else ColorDespues.copy(alpha = 0.10f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre y fecha
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sesion.juego,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sesion.fecha,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Valores de estrés
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${sesion.estresAntes}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorAntes
                )
                Text(
                    text = " > ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${sesion.estresDespues}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorDespues
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Indicador de mejora/empeora
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (mejoro) ColorAntes.copy(alpha = 0.12f)
                            else ColorDespues.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (mejoro) "-" else "+",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (mejoro) ColorAntes else ColorDespues
                    )
                }
            }
        }
    }
}

// -- Leyenda de la gráfica (igual que el original) ------------------------------
@Composable
fun LeyendaPunto(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color, radius = size.minDimension / 2)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = texto, fontSize = 12.sp, color = Color.Gray)
    }
}