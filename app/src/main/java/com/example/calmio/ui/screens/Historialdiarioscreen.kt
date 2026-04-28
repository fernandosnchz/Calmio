package com.example.calmio.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calmio.data.EntradaDiario
import com.example.calmio.ui.theme.*
import com.example.calmio.viewmodel.DiarioViewModel
import java.text.SimpleDateFormat
import java.util.*

// ── Modelo agrupado ────────────────────────────────────────────────────────────
private data class DiaConEntradas(
    val etiqueta: String,       // "Hoy", "Ayer", "Lunes 21 de abril"…
    val fechaCorta: String,     // "21 abr"
    val entradas: List<EntradaDiario>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialDiarioScreen(
    diarioViewModel: DiarioViewModel,
    onVolver: () -> Unit
) {
    val todas by diarioViewModel.todasLasEntradas.collectAsStateWithLifecycle()
    val dias  = remember(todas) { agruparPorDia(todas) }

    Scaffold(
        containerColor = Crema,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "📓 Historial del diario",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = VerdeSalvia
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint               = VerdeSalvia
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Crema)
            )
        }
    ) { paddingValues ->

        if (dias.isEmpty()) {
            // ── Estado vacío ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(listOf(Crema, VerdeMenta.copy(alpha = 0.2f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text       = "Aún no hay entradas",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = VerdeSalvia
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text      = "Escribe tu primera entrada\nen el diario emocional",
                        fontSize  = 14.sp,
                        color     = TextoSuave,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // ── Lista agrupada por día ────────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(listOf(Crema, VerdeMenta.copy(alpha = 0.2f)))
                    ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                itemsIndexed(dias, key = { _, dia -> dia.etiqueta }) { index, dia ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { visible = true }

                    AnimatedVisibility(
                        visible = visible,
                        enter   = fadeIn(tween(350, delayMillis = index * 60)) +
                                slideInVertically(tween(350, delayMillis = index * 60)) { 20 }
                    ) {
                        DiaAgendaItem(dia = dia)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// ── Bloque de un día ───────────────────────────────────────────────────────────
@Composable
private fun DiaAgendaItem(dia: DiaConEntradas) {
    Column {
        // Cabecera del día
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            // Pastilla con fecha corta
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(VerdeSalvia.copy(alpha = 0.15f))
                    .border(1.dp, VerdeSalvia.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text       = dia.fechaCorta,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = VerdeSalvia
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text       = dia.etiqueta,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = TextoPrincipal
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text     = "${dia.entradas.size} ${if (dia.entradas.size == 1) "entrada" else "entradas"}",
                fontSize = 11.sp,
                color    = TextoSuave
            )
        }

        // Línea vertical + entradas del día
        Row {
            // Línea de timeline
            Box(
                modifier = Modifier
                    .padding(start = 14.dp, end = 16.dp)
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(VerdeMenta.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                dia.entradas.forEach { entrada ->
                    EntradaCard(entrada = entrada)
                }
            }
        }
    }
}

// ── Tarjeta de una entrada ─────────────────────────────────────────────────────
@Composable
private fun EntradaCard(entrada: EntradaDiario) {
    val hora = remember(entrada.fechaTimestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(entrada.fechaTimestamp))
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .border(1.5.dp, VerdeSalvia.copy(alpha = 0.13f), RoundedCornerShape(16.dp)),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Hora de la entrada
            Text(
                text       = hora,
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextoSuave
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ¿Qué te preocupa?
            if (entrada.preocupacion.isNotBlank()) {
                CampoEntrada(
                    etiqueta   = "¿Qué te preocupaba?",
                    contenido  = entrada.preocupacion,
                    colorAccent = VerdeSalvia
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ¿Qué ha ido bien?
            if (entrada.fueronBien.isNotBlank()) {
                CampoEntrada(
                    etiqueta   = "¿Qué fue bien?",
                    contenido  = entrada.fueronBien,
                    colorAccent = VerdeMenta
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Pensamiento libre
            if (entrada.pensamientoLibre.isNotBlank()) {
                CampoEntrada(
                    etiqueta   = "Mis pensamientos",
                    contenido  = entrada.pensamientoLibre,
                    colorAccent = Terracota
                )
            }
        }
    }
}

// ── Campo individual dentro de la tarjeta ─────────────────────────────────────
@Composable
private fun CampoEntrada(
    etiqueta: String,
    contenido: String,
    colorAccent: androidx.compose.ui.graphics.Color
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Línea de acento izquierda
        Box(
            modifier = Modifier
                .width(3.dp)
                .defaultMinSize(minHeight = 16.dp)
                .background(colorAccent, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text       = etiqueta,
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color      = colorAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text     = contenido,
                fontSize = 13.sp,
                color    = TextoPrincipal,
                lineHeight = 18.sp
            )
        }
    }
}

// ── Lógica de agrupación ───────────────────────────────────────────────────────
private fun agruparPorDia(entradas: List<EntradaDiario>): List<DiaConEntradas> {
    if (entradas.isEmpty()) return emptyList()

    val fmtClave  = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fmtCorta  = SimpleDateFormat("d MMM", Locale("es"))
    val fmtLarga  = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es"))

    val hoy   = fmtClave.format(Date())
    val ayer  = fmtClave.format(Date(System.currentTimeMillis() - 86_400_000L))

    return entradas
        .groupBy { fmtClave.format(Date(it.fechaTimestamp)) }
        .entries
        .sortedByDescending { it.key }
        .map { (clave, lista) ->
            val fecha = fmtClave.parse(clave) ?: Date()
            val etiqueta = when (clave) {
                hoy  -> "Hoy"
                ayer -> "Ayer"
                else -> fmtLarga.format(fecha).replaceFirstChar { it.uppercase() }
            }
            DiaConEntradas(
                etiqueta   = etiqueta,
                fechaCorta = fmtCorta.format(fecha),
                entradas   = lista.sortedByDescending { it.fechaTimestamp }
            )
        }
}