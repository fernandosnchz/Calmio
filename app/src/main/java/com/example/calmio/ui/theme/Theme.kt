package com.example.calmio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CalmioLightColorScheme = lightColorScheme(
    primary              = VerdeSalvia,
    onPrimary            = Blanco,
    primaryContainer     = VerdeMenta,
    onPrimaryContainer   = TextoPrincipal,
    secondary            = Terracota,
    onSecondary          = Blanco,
    secondaryContainer   = Color(0xFFEDD9C5),
    onSecondaryContainer = TextoPrincipal,
    background           = Crema,
    onBackground         = TextoPrincipal,
    surface              = Blanco,
    onSurface            = TextoPrincipal,
    surfaceVariant       = SuperficieCard,
    onSurfaceVariant     = TextoSuave,
    error                = Error,
    onError              = Blanco,
)

private val CalmioDarkColorScheme = darkColorScheme(
    primary              = VerdeMenta,
    onPrimary            = Color(0xFF1B3526),
    primaryContainer     = Color(0xFF2D5C40),
    onPrimaryContainer   = VerdeMenta,
    secondary            = Terracota,
    onSecondary          = Color(0xFF3E1F00),
    secondaryContainer   = Color(0xFF5C3A1A),
    onSecondaryContainer = Color(0xFFFFD8B4),
    background           = Color(0xFF121212),
    onBackground         = Color(0xFFE8E3DE),
    surface              = Color(0xFF1E1E1E),
    onSurface            = Color(0xFFE8E3DE),
    surfaceVariant       = Color(0xFF2C2C2C),
    onSurfaceVariant     = Color(0xFF9E9E9E),
    error                = Color(0xFFEF9A9A),
    onError              = Color(0xFF3E0000),
)

@Composable
fun CalmioTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) CalmioDarkColorScheme else CalmioLightColorScheme,
        typography  = CalmioTypography,
        content     = content
    )
}