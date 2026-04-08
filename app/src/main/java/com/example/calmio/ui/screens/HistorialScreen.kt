package com.example.calmio.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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

@Composable
fun HistorialScreen(
    stressViewModel: StressViewModel = viewModel()
) {
    val sesiones by stressViewModel.todasLasSesiones.collectAsState()

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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "🌿 Tu evolución",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeSalvia
            )

            Text(
                text = "Estrés antes y después de jugar",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (sesiones.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
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
                // Gráfica
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
                            LeyendaPunto(
                                color = Color(0xFF4CAF50),
                                texto = "Antes"
                            )
                            LeyendaPunto(
                                color = Color(0xFFF44336),
                                texto = "Después"
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        GraficaEstres(sesiones = sesiones.reversed())
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Sesiones recientes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeSalvia,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sesiones) { sesion ->
                        TarjetaSesion(sesion = sesion)
                    }
                }
            }
        }
    }
}

@Composable
fun GraficaEstres(sesiones: List<SesionEstres>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        if (sesiones.size < 2) return@Canvas

        val pasoX = size.width / (sesiones.size - 1).toFloat()
        val altoUnitario = size.height / 10f

        val pathAntes = Path()
        val pathDespues = Path()

        sesiones.forEachIndexed { index, sesion ->
            val x = index * pasoX
            val yAntes = size.height - (sesion.estresAntes * altoUnitario)
            val yDespues = size.height - (sesion.estresDespues * altoUnitario)

            if (index == 0) {
                pathAntes.moveTo(x, yAntes)
                pathDespues.moveTo(x, yDespues)
            } else {
                pathAntes.lineTo(x, yAntes)
                pathDespues.lineTo(x, yDespues)
            }

            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 10f,
                center = Offset(x, yAntes)
            )
            drawCircle(
                color = Color(0xFFF44336),
                radius = 10f,
                center = Offset(x, yDespues)
            )
        }

        drawPath(path = pathAntes, color = Color(0xFF4CAF50), style = Stroke(width = 4f))
        drawPath(path = pathDespues, color = Color(0xFFF44336), style = Stroke(width = 4f))
    }
}

@Composable
fun TarjetaSesion(sesion: SesionEstres) {
    val mejoro = sesion.estresDespues < sesion.estresAntes

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${sesion.estresAntes}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorSegunEstres(sesion.estresAntes)
                )
                Text(
                    text = " → ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${sesion.estresDespues}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorSegunEstres(sesion.estresDespues)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (mejoro) "↓" else "↑",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (mejoro) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }
}

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