package com.example.calmio.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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

// Colores de la grafica
private val ColorAntes   = Color(0xFFF44336)
private val ColorDespues = Color(0xFF4CAF50)

// Emojis por nombre de juego
private val emojiPorJuego = mapOf(
    "aros"     to "⭕",
    "mochis"   to "🫧",
    "pesca"    to "🎣",
    "burbujas" to "🫧",
    "respiracion" to "🌬️"
)

private const val SESIONES_INICIALES = 3

// Nombres de meses en español
private val NOMBRES_MESES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

// Extrae "MM/yyyy" de una fecha "dd/MM/yyyy"
private fun clavesMes(fecha: String): String {
    return if (fecha.length >= 10) fecha.substring(3, 10) else fecha
}

// Nombre legible del mes desde clave "MM/yyyy"
private fun nombreMes(clave: String): String {
    return try {
        val partes = clave.split("/")
        val mes    = partes[0].toInt()
        val anio   = partes[1]
        "${NOMBRES_MESES[mes - 1]} $anio"
    } catch (e: Exception) { clave }
}

// Calcula racha de dias consecutivos jugados (fecha "dd/MM/yyyy")
private fun calcularRacha(sesiones: List<SesionEstres>): Int {
    if (sesiones.isEmpty()) return 0
    val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    val diasUnicos = sesiones
        .mapNotNull { runCatching { formatter.parse(it.fecha) }.getOrNull() }
        .map {
            val cal = java.util.Calendar.getInstance()
            cal.time = it
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.time
        }
        .distinct()
        .sortedDescending()

    if (diasUnicos.isEmpty()) return 0

    val hoy = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0);      set(java.util.Calendar.MILLISECOND, 0)
    }.time

    val unDia = 24L * 60 * 60 * 1000

    // Si el dia mas reciente no es hoy ni ayer, la racha es 0
    val diffPrimero = hoy.time - diasUnicos.first().time
    if (diffPrimero > unDia) return 0

    var racha = 1
    for (i in 0 until diasUnicos.size - 1) {
        val diff = diasUnicos[i].time - diasUnicos[i + 1].time
        if (diff == unDia) racha++ else break
    }
    return racha
}

@Composable
fun HistorialScreen(
    stressViewModel: StressViewModel = viewModel()
) {
    val sesiones by stressViewModel.todasLasSesiones.collectAsState()

    var mostrarTodas by remember { mutableStateOf(false) }
    var visible      by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Filtro por mes: lista de claves "MM/yyyy" disponibles + "Todos"
    val mesesDisponibles = remember(sesiones) {
        listOf("Todos") + sesiones.map { clavesMes(it.fecha) }.distinct().sortedDescending()
    }
    var mesFiltro by remember { mutableStateOf("Todos") }

    // Sesiones filtradas por mes seleccionado
    val sesionesFiltradas = remember(sesiones, mesFiltro) {
        if (mesFiltro == "Todos") sesiones
        else sesiones.filter { clavesMes(it.fecha) == mesFiltro }
    }

    val racha = remember(sesiones) { calcularRacha(sesiones) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Crema, VerdeMenta.copy(alpha = 0.25f))))
    ) {
        if (sesiones.isEmpty()) {
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
            val sesionesVisibles = if (mostrarTodas) sesionesFiltradas
            else sesionesFiltradas.take(SESIONES_INICIALES)
            val hayMas = sesionesFiltradas.size >= SESIONES_INICIALES

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ── Header ────────────────────────────────────────────────────
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
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ── Chips de resumen + racha ──────────────────────────────────
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 100))
                    ) {
                        val promedioAntes   = sesiones.map { it.estresAntes }.average().toFloat()
                        val promedioDespues = sesiones.map { it.estresDespues }.average().toFloat()
                        val positivas       = sesiones.count { it.estresDespues < it.estresAntes }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Fila 1: stats de estres
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

                            // Fila 2: racha de dias
                            RachaCard(racha = racha)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ── Selector de mes ───────────────────────────────────────────
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 150))
                    ) {
                        SelectorMes(
                            meses = mesesDisponibles,
                            seleccionado = mesFiltro,
                            onSeleccionar = {
                                mesFiltro = it
                                mostrarTodas = false  // reset al cambiar mes
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Grafica de barras ─────────────────────────────────────────
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
                                if (sesionesFiltradas.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Sin sesiones este mes",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 13.sp
                                        )
                                    }
                                } else {
                                    GraficaBarras(sesiones = sesionesFiltradas.takeLast(8))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ── Titulo lista ──────────────────────────────────────────────
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

                // ── Lista de sesiones ─────────────────────────────────────────
                if (sesionesFiltradas.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No jugaste este mes",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
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
                                    text = if (mostrarTodas) "Ver menos"
                                    else "Ver mas (${sesionesFiltradas.size - SESIONES_INICIALES} sesiones)",
                                    color = VerdeSalvia,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Tarjeta de racha ───────────────────────────────────────────────────────────
@Composable
private fun RachaCard(racha: Int) {
    val colorRacha = when {
        racha >= 7  -> Color(0xFFFF9800)  // naranja: racha larga
        racha >= 3  -> VerdeSalvia        // verde: racha media
        racha >= 1  -> ColorAntes         // verde claro: racha corta
        else        -> Color(0xFF9E9E9E)  // gris: sin racha
    }
    val emoji = when {
        racha >= 7  -> "🔥"
        racha >= 3  -> "⭐"
        racha >= 1  -> "🌱"
        else        -> "💤"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorRacha.copy(alpha = 0.30f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorRacha.copy(alpha = 0.07f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(emoji, fontSize = 24.sp)
                Column {
                    Text(
                        text = "Racha actual",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (racha == 0) "Sin racha activa"
                        else if (racha == 1) "1 dia seguido"
                        else "$racha dias seguidos",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorRacha
                    )
                }
            }
            // Barra visual de progreso hacia racha de 7
            val progreso = (racha / 7f).coerceIn(0f, 1f)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$racha / 7",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colorRacha.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progreso)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colorRacha)
                    )
                }
            }
        }
    }
}

// ── Selector de mes con chips horizontales ─────────────────────────────────────
@Composable
private fun SelectorMes(
    meses: List<String>,
    seleccionado: String,
    onSeleccionar: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        meses.forEach { clave ->
            val activo = clave == seleccionado
            val etiqueta = if (clave == "Todos") "Todos" else nombreMes(clave)
            Surface(
                shape = RoundedCornerShape(50),
                color = if (activo) VerdeSalvia else Color.White.copy(alpha = 0.85f),
                border = if (activo) null
                else BorderStroke(1.dp, VerdeSalvia.copy(alpha = 0.35f)),
                modifier = Modifier.clickable { onSeleccionar(clave) }
            ) {
                Text(
                    text = etiqueta,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    fontSize = 13.sp,
                    fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (activo) Color.White else VerdeSalvia
                )
            }
        }
    }
}

// ── Grafica de barras ──────────────────────────────────────────────────────────
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
        val altoUtil      = size.height * 0.60f
        val baseY         = size.height * 0.75f
        val anchoGrupo    = size.width / n
        val anchoBarraPar = anchoGrupo * 0.30f
        val separacion    = anchoGrupo * 0.05f

        sesiones.forEachIndexed { i, sesion ->
            val centroX  = anchoGrupo * i + anchoGrupo / 2f
            val xAntes   = centroX - anchoBarraPar - separacion / 2f
            val xDespues = centroX + separacion / 2f

            val altoAntes   = (sesion.estresAntes   / maxValor) * altoUtil
            val altoDespues = (sesion.estresDespues / maxValor) * altoUtil
            val radio = 8f

            drawRoundRect(
                color = ColorAntes.copy(alpha = 0.85f),
                topLeft = Offset(xAntes, baseY - altoAntes),
                size = Size(anchoBarraPar, altoAntes),
                cornerRadius = CornerRadius(radio, radio)
            )
            drawRoundRect(
                color = ColorDespues.copy(alpha = 0.85f),
                topLeft = Offset(xDespues, baseY - altoDespues),
                size = Size(anchoBarraPar, altoDespues),
                cornerRadius = CornerRadius(radio, radio)
            )

            // Numero encima barra Antes
            drawContext.canvas.nativeCanvas.apply {
                val p = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#4CAF50")
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
                drawText(sesion.estresAntes.toString(), xAntes + anchoBarraPar / 2f, baseY - altoAntes - 8f, p)
            }

            // Numero encima barra Despues
            drawContext.canvas.nativeCanvas.apply {
                val p = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#F44336")
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
                drawText(sesion.estresDespues.toString(), xDespues + anchoBarraPar / 2f, baseY - altoDespues - 8f, p)
            }
        }

        // Linea base
        drawLine(
            color = Color.LightGray.copy(alpha = 0.6f),
            start = Offset(0f, baseY),
            end   = Offset(size.width, baseY),
            strokeWidth = 1.5f
        )

        // Fechas bajo la linea base
        sesiones.forEachIndexed { i, sesion ->
            val centroX    = anchoGrupo * i + anchoGrupo / 2f
            val fechaCorta = if (sesion.fecha.length >= 5) sesion.fecha.substring(0, 5) else sesion.fecha
            drawContext.canvas.nativeCanvas.apply {
                val p = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#888888")
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(fechaCorta, centroX, baseY + 36f, p)
            }
        }
    }
}

// ── Chip de resumen ────────────────────────────────────────────────────────────
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
            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
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

// ── Tarjeta de sesion ──────────────────────────────────────────────────────────
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
                color = if (mejoro) ColorAntes.copy(alpha = 0.25f) else ColorDespues.copy(alpha = 0.20f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Column(modifier = Modifier.weight(1f)) {
                Text(text = sesion.juego, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(text = sesion.fecha, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${sesion.estresAntes}", fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, color = colorAntes)
                Text(text = " > ", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${sesion.estresDespues}", fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, color = colorDespues)
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

// ── Leyenda de la grafica ──────────────────────────────────────────────────────
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