package com.example.calmio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calmio.ui.screens.*
import com.example.calmio.ui.theme.CalmioTheme
import com.example.calmio.viewmodel.StressViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.calmio.ui.theme.Crema
import com.example.calmio.ui.theme.VerdeSalvia
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.fillMaxSize
import com.example.calmio.ui.theme.VerdeMenta

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalmioTheme {
                CalmioApp()
            }
        }
    }
}

@Composable
fun CalmioApp() {
    val navController = rememberNavController()
    val stressViewModel: StressViewModel = viewModel()

    // Guardamos temporalmente el estrés antes de jugar
    var estresAntes by remember { mutableStateOf(0) }
    // Guardamos qué juego eligió el usuario
    var juegoActual by remember { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // ── LOGIN ──────────────────────────────────────────────────────────
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("stress_antes") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // ── ESTRÉS ANTES ───────────────────────────────────────────────────
        // Solo se muestra la primera vez del día.
        // Si ya registró hoy, saltamos directamente al menú principal.
        composable("stress_antes") {
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                scope.launch {
                    val yaRegistro = stressViewModel.yaRegistroHoy()
                    if (yaRegistro) {
                        navController.navigate("main") {
                            popUpTo("stress_antes") { inclusive = true }
                        }
                    }
                }
            }

            StressScreen(
                titulo = "¿Cómo estás ahora mismo?",
                subtitulo = "Indica tu nivel de estrés antes de jugar",
                onConfirmar = { nivel ->
                    estresAntes = nivel
                    navController.navigate("main") {
                        popUpTo("stress_antes") { inclusive = true }
                    }
                }
            )
        }

        // ── MENÚ PRINCIPAL (con BottomBar) ─────────────────────────────────
        composable("main") {
            MainScreen(
                onJuegoSeleccionado = { juego ->
                    juegoActual = juego
                    navController.navigate(juego)
                },
                onCerrarSesion = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── JUEGO AROS ─────────────────────────────────────────────────────
        composable("aros") {
            val scope = rememberCoroutineScope()
            ArosScreen(
                onVolver = {
                    scope.launch {
                        val yaJugo = stressViewModel.yaJugoHoy()
                        if (yaJugo) {
                            navController.navigate("main") {
                                popUpTo("aros") { inclusive = true }
                            }
                        } else {
                            navController.navigate("stress_despues") {
                                popUpTo("aros") { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        // ── JUEGO MOCHIS ───────────────────────────────────────────────────
        composable("mochis") {
            val scope = rememberCoroutineScope()
            MochisScreen(
                onVolver = {
                    scope.launch {
                        val yaJugo = stressViewModel.yaJugoHoy()
                        if (yaJugo) {
                            navController.navigate("main") {
                                popUpTo("mochis") { inclusive = true }
                            }
                        } else {
                            navController.navigate("stress_despues") {
                                popUpTo("mochis") { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        // ── JUEGO PESCA ────────────────────────────────────────────────────
        composable("pesca") {
            val scope = rememberCoroutineScope()
            PescaScreen(
                onVolver = {
                    scope.launch {
                        val yaJugo = stressViewModel.yaJugoHoy()
                        if (yaJugo) {
                            navController.navigate("main") {
                                popUpTo("pesca") { inclusive = true }
                            }
                        } else {
                            navController.navigate("stress_despues") {
                                popUpTo("pesca") { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        // ── ESTRÉS DESPUÉS ─────────────────────────────────────────────────
        composable("stress_despues") {
            StressScreen(
                titulo = "¿Cómo te sientes ahora?",
                subtitulo = "Indica tu nivel de estrés después de jugar",
                estresAnterior = estresAntes,
                onConfirmar = { nivel ->
                    stressViewModel.guardarSesion(
                        juego = juegoActual,
                        estresAntes = estresAntes,
                        estresDespues = nivel
                    )
                    navController.navigate("main") {
                        popUpTo("stress_despues") { inclusive = true }
                    }
                }
            )
        }
    }
}

// ── PANTALLA PRINCIPAL CON BOTTOM NAV BAR ──────────────────────────────────────
// Esta pantalla contiene el menú inferior y gestiona la navegación entre
// Juegos e Historial sin salir del contexto del menú principal.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onJuegoSeleccionado: (String) -> Unit, onCerrarSesion: () -> Unit) {
    var tabSeleccionada by remember { mutableStateOf(0) }
    var menuExpandido by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Crema,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🌿 Calmio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = VerdeSalvia
                    )
                },
                actions = {
                    // Icono de perfil que abre el menú
                    IconButton(onClick = { menuExpandido = true }) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Menú",
                            tint = VerdeSalvia
                        )
                    }

                    // Menú desplegable
                    DropdownMenu(
                        expanded = menuExpandido,
                        onDismissRequest = { menuExpandido = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.ExitToApp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Cerrar sesión",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            onClick = {
                                menuExpandido = false
                                onCerrarSesion()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Crema
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tabSeleccionada == 0,
                    onClick = { tabSeleccionada = 0 },
                    icon = { Icon(Icons.Filled.SportsEsports, contentDescription = "Juegos") },
                    label = { Text("Juegos") }
                )
                NavigationBarItem(
                    selected = tabSeleccionada == 1,
                    onClick = { tabSeleccionada = 1 },
                    icon = { Icon(Icons.Filled.History, contentDescription = "Historial") },
                    label = { Text("Historial") }
                )
            }
        }
    ) { paddingValues ->
        when (tabSeleccionada) {
            0 -> GameSelectionScreen(
                modifier = Modifier.padding(paddingValues),
                onJuegoSeleccionado = onJuegoSeleccionado
            )
            1 -> HistorialScreen()
        }
    }
}