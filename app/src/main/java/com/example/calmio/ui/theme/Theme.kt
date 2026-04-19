package com.example.calmio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CalmioColorScheme = lightColorScheme(
    primary = VerdeSalvia,
    onPrimary = Blanco,
    primaryContainer = VerdeMenta,
    onPrimaryContainer = TextoPrincipal,
    secondary = Terracota,
    onSecondary = Blanco,
    secondaryContainer = Color(0xFFEDD9C5),
    onSecondaryContainer = TextoPrincipal,
    background = Crema,
    onBackground = TextoPrincipal,
    surface = Blanco,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieCard,
    onSurfaceVariant = TextoSuave,
    error = Error,
    onError = Blanco,
)

@Composable
fun CalmioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CalmioColorScheme,
        typography = CalmioTypography,
        content = content
    )
}