package com.example.calmio.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calmio.data.SesionEstres
import com.example.calmio.ui.theme.*
import com.example.calmio.viewmodel.DiarioViewModel
import com.example.calmio.viewmodel.StressViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DiarioScreen(
    diarioViewModel: DiarioViewModel,
    stressViewModel: StressViewModel,
    modifier: Modifier = Modifier
) {
    val sesiones        by stressViewModel.todasLasSesiones.collectAsStateWithLifecycle()
    val entradasDeHoy  by diarioViewModel.entradasDeHoy.collectAsStateWithLifecycle(emptyList())

    var preocupacion   by remember { mutableStateOf("") }
    var fueronBien     by remember { mutableStateOf("") }
    var pensamiento    by remember { mutableStateOf("") }
    var guardado       by remember { mutableStateOf(false) }
    var visible        by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    // Sesiones de hoy para el resumen automático
    val sesionesHoy = remember(sesiones) { sesionesDeHoy(sesiones) }
    val promedioHoy = remember(sesionesHoy) {
        if (sesionesHoy.isEmpty()) null
        else sesionesHoy.map { it.estresDespues }.average().toInt()
    }
    val mejorSesion = remember(sesionesHoy) {
        sesionesHoy.minByOrNull { it.estresDespues }
    }

    val fechaHoy = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es")).format(Date())
            .replaceFirstChar { it.uppercase() }
    }

    val focusManager      = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Crema, VerdeMenta.copy(alpha = 0.25f))
                )
            )
            // Cierra teclado y quita foco al tocar fuera de cualquier campo
            .clickable(
                indication          = null,
                interactionSource   = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
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
                        text = "📓 Diario Emocional",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeSalvia
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = fechaHoy,
                        fontSize = 13.sp,
                        color = TextoSuave
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Resumen automático de estrés ──────────────────────────────────
            AnimatedVisibility(
                visible = visible && sesionesHoy.isNotEmpty(),
                enter = fadeIn(tween(400, delayMillis = 150)) +
                        slideInVertically(tween(400, delayMillis = 150)) { 20 }
            ) {
                ResumenEstresHoy(
                    promedioHoy  = promedioHoy,
                    mejorSesion  = mejorSesion,
                    totalSesiones = sesionesHoy.size
                )
            }

            if (sesionesHoy.isNotEmpty()) Spacer(modifier = Modifier.height(12.dp))

            // ── Preguntas guiadas ─────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 200)) +
                        slideInVertically(tween(400, delayMillis = 200)) { 20 }
            ) {
                SeccionPreguntasGuiadas(
                    preocupacion = preocupacion,
                    fueronBien   = fueronBien,
                    onPreocupacionChange = { preocupacion = it; guardado = false },
                    onFueronBienChange   = { fueronBien   = it; guardado = false }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Pensamiento libre ─────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 300)) +
                        slideInVertically(tween(400, delayMillis = 300)) { 20 }
            ) {
                SeccionPensamientoLibre(
                    pensamiento = pensamiento,
                    onChange    = { pensamiento = it; guardado = false }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Botón guardar ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 400))
            ) {
                BotonGuardar(
                    guardado = guardado,
                    habilitado = preocupacion.isNotBlank() ||
                            fueronBien.isNotBlank()   ||
                            pensamiento.isNotBlank(),
                    onClick = {
                        diarioViewModel.guardarEntrada(preocupacion, fueronBien, pensamiento)
                        guardado     = true
                        preocupacion = ""
                        fueronBien   = ""
                        pensamiento  = ""
                    }
                )
            }

            // ── Contador entradas de hoy ──────────────────────────────────────
            if (entradasDeHoy.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${entradasDeHoy.size} ${if (entradasDeHoy.size == 1) "entrada guardada" else "entradas guardadas"} hoy",
                    fontSize = 12.sp,
                    color = TextoSuave
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Resumen automático ─────────────────────────────────────────────────────────
@Composable
private fun ResumenEstresHoy(
    promedioHoy: Int?,
    mejorSesion: SesionEstres?,
    totalSesiones: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, VerdeSalvia.copy(alpha = 0.20f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Número de estrés promedio
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(VerdeSalvia.copy(alpha = 0.12f))
                    .border(1.dp, VerdeSalvia.copy(alpha = 0.20f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = promedioHoy?.toString() ?: "-",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeSalvia
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Estrés promedio hoy",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
                Spacer(modifier = Modifier.height(2.dp))
                val textoDetalle = if (mejorSesion != null &&
                    mejorSesion.estresDespues < mejorSesion.estresAntes) {
                    "Bajaste de ${mejorSesion.estresAntes} → ${mejorSesion.estresDespues} jugando 🎉"
                } else {
                    "$totalSesiones ${if (totalSesiones == 1) "sesión" else "sesiones"} completadas"
                }
                Text(
                    text = textoDetalle,
                    fontSize = 12.sp,
                    color = TextoSuave
                )
            }

            // Etiqueta "auto"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SuperficieCard)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = "auto", fontSize = 10.sp, color = TextoSuave)
            }
        }
    }
}

// ── Preguntas guiadas ──────────────────────────────────────────────────────────
@Composable
private fun SeccionPreguntasGuiadas(
    preocupacion: String,
    fueronBien: String,
    onPreocupacionChange: (String) -> Unit,
    onFueronBienChange: (String) -> Unit
) {
    val focusManager       = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusFueronBien    = remember { FocusRequester() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, VerdeSalvia.copy(alpha = 0.18f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PREGUNTAS GUIADAS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoSuave,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            PreguntaTextField(
                pregunta   = "¿Qué te preocupa hoy?",
                valor      = preocupacion,
                onChange   = onPreocupacionChange,
                colorBorde = VerdeSalvia,
                imeAction  = ImeAction.Next,
                onImeAction = { focusFueronBien.requestFocus() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            PreguntaTextField(
                pregunta    = "¿Qué ha ido bien hoy?",
                valor       = fueronBien,
                onChange    = onFueronBienChange,
                colorBorde  = VerdeMenta,
                imeAction   = ImeAction.Done,
                onImeAction = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                focusRequester = focusFueronBien
            )
        }
    }
}

// ── Pensamiento libre ──────────────────────────────────────────────────────────
@Composable
private fun SeccionPensamientoLibre(
    pensamiento: String,
    onChange: (String) -> Unit
) {
    val focusManager       = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, VerdeSalvia.copy(alpha = 0.18f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "MIS PENSAMIENTOS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoSuave,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = pensamiento,
                onValueChange = onChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 100.dp),
                placeholder = {
                    Text(
                        text = "Escribe libremente lo que sientes...",
                        fontSize = 13.sp,
                        color = TextoSuave.copy(alpha = 0.6f)
                    )
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = SuperficieCard,
                    unfocusedContainerColor = SuperficieCard,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor        = TextoPrincipal,
                    unfocusedTextColor      = TextoPrincipal
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, lineHeight = 20.sp)
            )
        }
    }
}

// ── Botón guardar ──────────────────────────────────────────────────────────────
@Composable
private fun BotonGuardar(
    guardado: Boolean,
    habilitado: Boolean,
    onClick: () -> Unit
) {
    val focusManager       = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Button(
        onClick = {
            focusManager.clearFocus()
            keyboardController?.hide()
            onClick()
        },
        enabled = habilitado && !guardado,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = VerdeSalvia,
            disabledContainerColor = if (guardado) VerdeMenta else VerdeSalvia.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text = if (guardado) "✓ Guardado" else "Guardar entrada",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Blanco
        )
    }
}

// ── Campo de pregunta guiada ───────────────────────────────────────────────────
@Composable
private fun PreguntaTextField(
    pregunta: String,
    valor: String,
    onChange: (String) -> Unit,
    colorBorde: Color,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    focusRequester: FocusRequester? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SuperficieCard)
            .border(
                width = 1.dp,
                color = colorBorde.copy(alpha = 0.30f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .defaultMinSize(minHeight = 48.dp)
                    .background(
                        colorBorde,
                        RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    )
            )
            Column(modifier = Modifier.padding(start = 12.dp, top = 10.dp,
                end = 12.dp, bottom = 4.dp)) {
                Text(
                    text = pregunta,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VerdeSalvia
                )
                TextField(
                    value = valor,
                    onValueChange = onChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (focusRequester != null)
                                Modifier.focusRequester(focusRequester)
                            else Modifier
                        ),
                    placeholder = {
                        Text(
                            text = "Escribe aquí...",
                            fontSize = 13.sp,
                            color = TextoSuave.copy(alpha = 0.5f)
                        )
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = imeAction
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onNext = { onImeAction() },
                        onDone = { onImeAction() }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor        = TextoPrincipal,
                        unfocusedTextColor      = TextoPrincipal
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
            }
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────
private fun sesionesDeHoy(sesiones: List<SesionEstres>): List<SesionEstres> {
    val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    return sesiones.filter { it.fecha == hoy }
}