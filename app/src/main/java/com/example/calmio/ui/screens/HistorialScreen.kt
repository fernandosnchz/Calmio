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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
    "aros"        to "⭕",
    "mochis"      to "🫧",
    "pesca"       to "🎣",
    "burbujas"    to "🫧",
    "respiracion" to "🌬️"
)

private const val SESIONES_INICIALES = 5
private const val SESIONES_MAXIMO    = 15

// Nombres de meses
private val NOMBRES_MESES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

// Extrae "MM/yyyy" de una fecha "dd/MM/yyyy"
private fun clavesMes(fecha: String): String =
    if (fecha.length >= 10) fecha.substring(3, 10) else fecha

// Nombre legible del mes desde clave "MM/yyyy"
private fun nombreMes(clave: String): String {
    return try {
        val partes = clave.split("/")
        val mes  = partes[0].toInt()
        val anio = partes[1]
        "${NOMBRES_MESES[mes - 1]} $anio"
    } catch (e: Exception) { clave }
}

// Clave del mes actual en formato "MM/yyyy"
private fun clavesMesActual(): String {
    val cal = java.util.Calendar.getInstance()
    val mes  = String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1)
    val anio = cal.get(java.util.Calendar.YEAR).toString()
    return "$mes/$anio"
}

// Calcula la racha de dias consecutivos jugados (fecha "dd/MM/yyyy")
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
    val diffPrimero = hoy.time - diasUnicos.first().time
    if (diffPrimero > unDia) return 0

    var racha = 1
    for (i in 0 until diasUnicos.size - 1) {
        val diff = diasUnicos[i].time - diasUnicos[i + 1].time
        if (diff == unDia) racha++ else break
    }
    return racha
}

// ─────────────────────────────────────────────────────────────────────────────
// Pantalla principal
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HistorialScreen(
    stressViewModel: StressViewModel = viewModel()
) {
    val sesiones by stressViewModel.todasLasSesiones.collectAsState()

    var mostrarTodas by remember { mutableStateOf(false) }
    var visible      by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // CAMBIO: primer filtro es el mes actual si existe, si no "Todos"
    val mesActual = clavesMesActual()
    val mesesDisponibles = remember(sesiones) {
        sesiones.map { clavesMes(it.fecha) }.distinct().sortedDescending()
    }
    var mesFiltro by remember(mesesDisponibles) {
        mutableStateOf(if (mesesDisponibles.contains(mesActual)) mesActual else "Todos")
    }

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
            val sesionesVisibles = if (mostrarTodas)
                sesionesFiltradas.take(SESIONES_MAXIMO)
            else
                sesionesFiltradas.take(SESIONES_INICIALES)
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ResumenChip(
                                    modifier  = Modifier.weight(1f),
                                    etiqueta  = "Estrés al entrar",
                                    valor     = "%.1f".format(promedioAntes),
                                    color     = ColorAntes
                                )
                                ResumenChip(
                                    modifier  = Modifier.weight(1f),
                                    etiqueta  = "Estrés al salir",
                                    valor     = "%.1f".format(promedioDespues),
                                    color     = ColorDespues
                                )
                                ResumenChip(
                                    modifier  = Modifier.weight(1f),
                                    etiqueta  = "Sesiones que te relajaron",
                                    valor     = "$positivas",
                                    color     = VerdeSalvia
                                )
                            }
                            RachaCard(racha = racha)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ── Selector de mes — Añadido dropdown en lugar de chips para que sea más estético ──────
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 150))
                    ) {
                        SelectorMesDropdown(
                            meses        = mesesDisponibles,
                            seleccionado = mesFiltro,
                            mesActual    = mesActual,
                            onSeleccionar = {
                                mesFiltro    = it
                                mostrarTodas = false
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Grafica de barras  ──
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500, delayMillis = 200))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(20.dp),
                            colors   = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.9f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val tituloGrafica = if (mesFiltro == "Todos") "Todas las sesiones"
                                else nombreMes(mesFiltro)
                                Text(
                                    text       = tituloGrafica,
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier   = Modifier.padding(bottom = 8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    LeyendaPunto(color = ColorAntes,   texto = "Antes")
                                    LeyendaPunto(color = ColorDespues, texto = "Después")
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                if (sesionesFiltradas.isEmpty()) {
                                    Box(
                                        modifier           = Modifier.fillMaxWidth().height(80.dp),
                                        contentAlignment   = Alignment.Center
                                    ) {
                                        Text(
                                            "Sin sesiones este mes",
                                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 13.sp
                                        )
                                    }
                                } else {
                                    // Añadido un scroll horizontal para que no se junten las fechas
                                    Box(
                                        modifier = Modifier
                                            .horizontalScroll(rememberScrollState())
                                    ) {
                                        GraficaBarras(sesiones = sesionesFiltradas)
                                    }
                                }

                                // Bloque de medias del mes
                                if (sesionesFiltradas.isNotEmpty()) {
                                    val mediaAntes   = sesionesFiltradas.map { it.estresAntes }.average()
                                    val mediaDespues = sesionesFiltradas.map { it.estresDespues }.average()
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        MediaBadge(
                                            label = "Media antes",
                                            valor = "%.1f".format(mediaAntes),
                                            color = ColorAntes
                                        )
                                        MediaBadge(
                                            label = "Media después",
                                            valor = "%.1f".format(mediaDespues),
                                            color = ColorDespues
                                        )
                                    }
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
                            text          = "Sesiones recientes",
                            fontSize      = 14.sp,
                            fontWeight    = FontWeight.SemiBold,
                            color         = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // ── Lista de sesiones ─────────────────────────────────────────
                if (sesionesFiltradas.isEmpty()) {
                    item {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No jugaste este mes",
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    itemsIndexed(sesionesVisibles) { index, sesion ->
                        AnimatedVisibility(
                            visible = visible,
                            enter   = fadeIn(tween(350, delayMillis = 350 + index * 60)) +
                                    slideInVertically(tween(350, delayMillis = 350 + index * 60)) { 20 }
                        ) {
                            TarjetaSesion(sesion = sesion)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (hayMas) {
                        item {
                            TextButton(
                                onClick  = { mostrarTodas = !mostrarTodas },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when {
                                        mostrarTodas -> "Ver menos"
                                        sesionesFiltradas.size > SESIONES_MAXIMO ->
                                            "Ver más (mostrando $SESIONES_MAXIMO de ${sesionesFiltradas.size})"
                                        else ->
                                            "Ver más (${sesionesFiltradas.size - SESIONES_INICIALES} sesiones)"
                                    },
                                    color      = VerdeSalvia,
                                    fontSize   = 14.sp,
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

// ─────────────────────────────────────────────────────────────────────────────
// Selector de mes con dropdown
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SelectorMesDropdown(
    meses: List<String>,
    seleccionado: String,
    mesActual: String,
    onSeleccionar: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    val etiquetaSeleccionada = when (seleccionado) {
        "Todos"   -> "Todas las sesiones"
        mesActual -> "Este mes (${nombreMes(mesActual)})"
        else      -> nombreMes(seleccionado)
    }

    // Lista completa: "Este mes" primero si existe, luego el resto, luego "Todos"
    val opcionesTodas = buildList {
        if (meses.contains(mesActual)) add(mesActual)
        addAll(meses.filter { it != mesActual })
        add("Todos")
    }

    Box {
        Surface(
            shape  = RoundedCornerShape(14.dp),
            color  = Color.White.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, VerdeSalvia.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandido = true }
        ) {
            Row(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = etiquetaSeleccionada,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = VerdeSalvia
                )
                Icon(
                    imageVector        = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Seleccionar mes",
                    tint               = VerdeSalvia
                )
            }
        }

        DropdownMenu(
            expanded        = expandido,
            onDismissRequest = { expandido = false },
            modifier        = Modifier.fillMaxWidth(0.9f)
        ) {
            opcionesTodas.forEach { clave ->
                val activo = clave == seleccionado
                val etiqueta = when (clave) {
                    "Todos"   -> "Todas las sesiones"
                    mesActual -> "Este mes (${nombreMes(clave)})"
                    else      -> nombreMes(clave)
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            text       = etiqueta,
                            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
                            color      = if (activo) VerdeSalvia
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onSeleccionar(clave)
                        expandido = false
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Badge de media debajo del gráfico
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MediaBadge(label: String, valor: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = valor, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Tarjeta de racha ───────────────────────────────────────────────────────────
@Composable
private fun RachaCard(racha: Int) {
    val colorRacha = when {
        racha >= 7 -> Color(0xFFFF9800)
        racha >= 3 -> VerdeSalvia
        racha >= 1 -> ColorAntes
        else       -> Color(0xFF9E9E9E)
    }
    val emoji = when {
        racha >= 7 -> "🔥"
        racha >= 3 -> "⭐"
        racha >= 1 -> "🌱"
        else       -> "💤"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorRacha.copy(alpha = 0.30f), RoundedCornerShape(14.dp)),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colorRacha.copy(alpha = 0.07f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(emoji, fontSize = 24.sp)
                Column {
                    Text(
                        text     = "Racha actual",
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text       = if (racha == 0) "Sin racha activa"
                        else if (racha == 1) "1 dia seguido"
                        else "$racha dias seguidos",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = colorRacha
                    )
                }
            }
            val progreso = (racha / 7f).coerceIn(0f, 1f)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text     = "$racha / 7",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp).height(6.dp)
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

// ── Chip de resumen ────────────────────────────────────────────────────────────
@Composable
private fun ResumenChip(
    modifier: Modifier = Modifier,
    etiqueta: String,
    valor: String,
    color: Color
) {
    Card(
        modifier  = modifier.border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier              = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = valor,    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text      = etiqueta,
                fontSize  = 10.sp,
                lineHeight = 12.sp,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
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
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(16.dp),
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
                Text(
                    text       = sesion.juego,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text     = sesion.fecha,
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${sesion.estresAntes}",   fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorAntes)
                Text(text = " > ",                     fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${sesion.estresDespues}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorDespues)
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
                        text       = if (mejoro) "-" else "+",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (mejoro) ColorAntes else ColorDespues
                    )
                }
            }
        }
    }
}

// ── Leyenda de punto ───────────────────────────────────────────────────────────
@Composable
fun LeyendaPunto(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color, radius = size.minDimension / 2)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = texto, fontSize = 11.sp, color = Color.Gray)
    }
}

// ── Grafica de barras  ──────────────────────────────────────
@Composable
fun GraficaBarras(sesiones: List<SesionEstres>) {
    if (sesiones.isEmpty()) return

    Canvas(
        modifier = Modifier
            .width((sesiones.size * 70).dp)
            .height(220.dp)
    ) {
        val n = sesiones.size
        val maxValor = 10f
        val topArea = 50f
        val bottomArea = 50f
        val chartHeight = size.height - topArea - bottomArea
        val baseY = topArea + chartHeight

        val groupWidth = size.width / n
        val barWidth = groupWidth * 0.25f
        val spaceBetween = groupWidth * 0.1f

        sesiones.forEachIndexed { i, sesion ->

            val centerX = groupWidth * i + groupWidth / 2f

            val xAntes = centerX - barWidth - spaceBetween / 2
            val xDespues = centerX + spaceBetween / 2

            val hAntes = (sesion.estresAntes / maxValor) * chartHeight
            val hDespues = (sesion.estresDespues / maxValor) * chartHeight

            val yAntes = baseY - hAntes
            val yDespues = baseY - hDespues

            // 🟩 Barras
            drawRoundRect(
                color = ColorAntes,
                topLeft = Offset(xAntes, yAntes),
                size = Size(barWidth, hAntes),
                cornerRadius = CornerRadius(10f, 10f)
            )

            drawRoundRect(
                color = ColorDespues,
                topLeft = Offset(xDespues, yDespues),
                size = Size(barWidth, hDespues),
                cornerRadius = CornerRadius(10f, 10f)
            )

            val paintAntes = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#E53935")
                textSize = 26f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }

            val paintDespues = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#43A047")
                textSize = 26f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }

            drawContext.canvas.nativeCanvas.drawText(
                sesion.estresAntes.toString(),
                xAntes + barWidth / 2,
                yAntes - 10f, //
                paintAntes
            )

            drawContext.canvas.nativeCanvas.drawText(
                sesion.estresDespues.toString(),
                xDespues + barWidth / 2,
                yDespues - 10f,
                paintDespues
            )

            val paintFecha = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#888888")
                textSize = 22f
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val fechaCorta =
                if (sesion.fecha.length >= 5) sesion.fecha.substring(0, 5)
                else sesion.fecha

            drawContext.canvas.nativeCanvas.drawText(
                fechaCorta,
                centerX,
                baseY + 30f,
                paintFecha
            )
        }
    }
}