package com.example.calmio.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
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

// Colores de la grafica y estados
private val ColorAntes   = Color(0xFF4CAF50)
private val ColorDespues = Color(0xFFF44336)

// Emojis por nombre de juego
private val emojiPorJuego = mapOf(
    "aros"     to "⭕",
    "mochis"   to "🫧",
    "pesca"    to "🎣",
    "burbujas" to "🫧"
)

// Cuantas sesiones mostrar antes del "ver mas"
private const val SESIONES_INICIALES = 5

@Composable
fun HistorialScreen(
    stressViewModel: StressViewModel = viewModel()
) {
    val sesiones by stressViewModel.todasLasSesiones.collectAsState()

    // Controla si se muestran todas o solo las primeras
    var mostrarTodas by remember { mutableStateOf(false) }

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
            // Estado vacio centrado
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("😌", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aun no tienes sesiones.\nJuega para ver tu evolucion.",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val sesionesVisibles = if (mostrarTodas) sesiones else sesiones.take(SESIONES_INICIALES)
            val hayMas = sesiones.size > SESIONES_INICIALES

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Header
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -20 }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🌿 Tu evolucion",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdeSalvia
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Estres antes y despues de jugar",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Chips de resumen
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 100))
                    ) {
                        val promedioAntes   = sesiones.map { it.estresAntes }.average().toFloat()
                        val promedioDespues = sesiones.map { it.estresDespues }.average().toFloat()
                        val positivas       = sesiones.count { it.estresDespues < it.estresAntes }

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
                                etiqueta = "Media despues",
                                valor = "%.1f".format(promedioDespues),
                                color = ColorDespues
                            )
                            ResumenChip(
                                modifier = Modifier.weight(1f),
                                etiqueta = "+ positivas",
                                valor = "$positivas",
                                color = VerdeSalvia
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Grafica de barras
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
                                    LeyendaPunto(color = ColorDespues, texto = "Despues")
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                GraficaBarras(sesiones = sesiones.takeLast(8))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Titulo lista
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

                // Sesiones (limitadas o todas)
                itemsIndexed(sesionesVisibles) { index, sesion ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(350, delayMillis = 350 + index * 60)) +
                                slideInVertically(tween(350, delayMillis = 350 + index * 60)) { 20 }
                    ) {
                        TarjetaSesion(sesion = sesion)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Boton ver mas / ver menos
                if (hayMas) {
                    item {
                        TextButton(
                            onClick = { mostrarTodas = !mostrarTodas },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (mostrarTodas) "Ver menos" else "Ver mas (${sesiones.size - SESIONES_INICIALES} sesiones)",
                                color = VerdeSalvia,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// Grafica de barras con etiquetas de numero
@Composable
fun GraficaBarras(sesiones: List<SesionEstres>) {
    if (sesiones.isEmpty()) return

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        val n             = sesiones.size
        val maxValor      = 10f
        val altoUtil      = size.height * 0.60f  // espacio util para barras
        val baseY         = size.height * 0.75f  // linea base, deja espacio para fechas abajo
        val anchoGrupo    = size.width / n
        val anchoBarraPar = anchoGrupo * 0.30f
        val separacion    = anchoGrupo * 0.05f

        sesiones.forEachIndexed { i, sesion ->
            val centroX  = anchoGrupo * i + anchoGrupo / 2f

            val xAntes   = centroX - anchoBarraPar - separacion / 2f
            val xDespues = centroX + separacion / 2f

            val altoAntes   = (sesion.estresAntes   / maxValor) * altoUtil
            val altoDespues = (sesion.estresDespues / maxValor) * altoUtil

            val radioEsquina = 8f

            // Barra "Antes"
            drawRoundRect(
                color = ColorAntes.copy(alpha = 0.85f),
                topLeft = Offset(xAntes, baseY - altoAntes),
                size = Size(anchoBarraPar, altoAntes),
                cornerRadius = CornerRadius(radioEsquina, radioEsquina)
            )

            // Barra "Despues"
            drawRoundRect(
                color = ColorDespues.copy(alpha = 0.85f),
                topLeft = Offset(xDespues, baseY - altoDespues),
                size = Size(anchoBarraPar, altoDespues),
                cornerRadius = CornerRadius(radioEsquina, radioEsquina)
            )

            // Etiqueta numero encima barra Antes
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#4CAF50")
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
                drawText(
                    sesion.estresAntes.toString(),
                    xAntes + anchoBarraPar / 2f,
                    baseY - altoAntes - 8f,
                    paint
                )
            }

            // Etiqueta numero encima barra Despues
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#F44336")
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
                drawText(
                    sesion.estresDespues.toString(),
                    xDespues + anchoBarraPar / 2f,
                    baseY - altoDespues - 8f,
                    paint
                )
            }
        }

        // Linea base
        drawLine(
            color = Color.LightGray.copy(alpha = 0.6f),
            start = Offset(0f, baseY),
            end   = Offset(size.width, baseY),
            strokeWidth = 1.5f
        )

        // Etiquetas de fecha bajo cada grupo (dia/mes)
        sesiones.forEachIndexed { i, sesion ->
            val centroX = anchoGrupo * i + anchoGrupo / 2f
            // sesion.fecha viene como "dd/MM/yyyy", mostramos solo "dd/MM"
            val fechaCorta = if (sesion.fecha.length >= 5) sesion.fecha.substring(0, 5) else sesion.fecha
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#888888")
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(fechaCorta, centroX, baseY + 36f, paint)
            }
        }
    }
}

// Chip de resumen
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

// Tarjeta de sesion
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

            // Valores de estres
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

// Leyenda de la grafica
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