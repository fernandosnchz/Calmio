package com.example.calmio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calmio.ui.theme.Crema
import com.example.calmio.ui.theme.VerdeMenta
import com.example.calmio.ui.theme.VerdeSalvia
import kotlin.math.roundToInt

@Composable
fun StressScreen(
    titulo: String,
    subtitulo: String,
    estresAnterior: Int? = null,
    onConfirmar: (Int) -> Unit
) {
    var sliderValue by remember { mutableStateOf(5f) }
    val nivelActual = sliderValue.roundToInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Crema, VerdeMenta.copy(alpha = 0.2f))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🌿", fontSize = 40.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = titulo,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeSalvia,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitulo,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Card principal con el slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Número grande con color
                    Text(
                        text = nivelActual.toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorSegunEstres(nivelActual)
                    )

                    Text(
                        text = nivelTexto(nivelActual),
                        fontSize = 15.sp,
                        color = colorSegunEstres(nivelActual),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = colorSegunEstres(nivelActual),
                            activeTrackColor = colorSegunEstres(nivelActual)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "😌 Relajado",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "😰 Estresado",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Card del valor anterior (solo en pantalla "después")
            if (estresAnterior != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VerdeMenta.copy(alpha = 0.2f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tu estrés antes de jugar",
                            fontSize = 14.sp,
                            color = VerdeSalvia
                        )
                        Text(
                            text = estresAnterior.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorSegunEstres(estresAnterior)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onConfirmar(nivelActual) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeSalvia
                )
            ) {
                Text(
                    "Confirmar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun nivelTexto(nivel: Int): String {
    return when (nivel) {
        1, 2 -> "Muy relajado 😌"
        3, 4 -> "Tranquilo 🙂"
        5, 6 -> "Algo tenso 😐"
        7, 8 -> "Estresado 😟"
        else -> "Muy estresado 😰"
    }
}