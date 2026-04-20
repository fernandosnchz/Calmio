package com.example.calmio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calmio.ui.theme.Crema
import com.example.calmio.ui.theme.Terracota
import com.example.calmio.ui.theme.VerdeMenta
import com.example.calmio.ui.theme.VerdeSalvia

@Composable
fun GameSelectionScreen(
    modifier: Modifier = Modifier,
    onJuegoSeleccionado: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "🌿 Calmio",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = VerdeSalvia
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Elige un juego para relajarte",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Juegos disponibles",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        GameCard(
            emoji = "⭕",
            nombre = "Juego de Aros",
            descripcion = "Ensarta los aros en los postes",
            color = VerdeSalvia,
            onClick = { onJuegoSeleccionado("aros") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        GameCard(
            emoji = "🫧",
            nombre = "Explotar Burbujas",
            descripcion = "Toca las burbujas para explotar",
            color = VerdeMenta,
            onClick = { onJuegoSeleccionado("mochis") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        GameCard(
            emoji = "🎣",
            nombre = "Juego de Pesca",
            descripcion = "Captura peces en los anzuelos",
            color = Terracota,   // ya lo tienes definido en tu tema
            onClick = { onJuegoSeleccionado("pesca") }
        )
    }
}

@Composable
fun GameCard(
    emoji: String,
    nombre: String,
    descripcion: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con fondo de color
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = nombre,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = descripcion,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "›",
                fontSize = 24.sp,
                color = color
            )
        }
    }
}