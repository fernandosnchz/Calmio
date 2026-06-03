package com.example.calmio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calmio.ui.screens.*
import com.example.calmio.ui.theme.CalmioTheme
import com.example.calmio.ui.theme.Crema
import com.example.calmio.ui.theme.VerdeSalvia
import com.example.calmio.viewmodel.DiarioViewModel
import com.example.calmio.viewmodel.SettingsViewModel
import com.example.calmio.viewmodel.StressViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val darkMode by settingsViewModel.darkMode.collectAsState()

            CalmioTheme(darkTheme = darkMode) {
                CalmioApp(settingsViewModel = settingsViewModel)
            }
        }
    }
}

@Composable
fun CalmioApp(settingsViewModel: SettingsViewModel) {
    val navController   = rememberNavController()
    val stressViewModel: StressViewModel = viewModel()
    val diarioViewModel: DiarioViewModel = viewModel()

    var estresAntes by remember { mutableStateOf(0) }
    var juegoActual by remember { mutableStateOf("") }

    // Si ya hay sesión activa hace que salte el login
    val usuarioActual = FirebaseAuth.getInstance().currentUser
    val startDestination = if (usuarioActual != null) "stress_antes" else "login"

    NavHost(navController = navController, startDestination = startDestination) {

        // ── Auth ────────────────────────────────────────────────────────────
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    stressViewModel.recargarUsuario()
                    diarioViewModel.recargarUsuario()
                    navController.navigate("stress_antes") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                },
                onForgotPassword = {
                    navController.navigate("forgot_password")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    stressViewModel.recargarUsuario()
                    diarioViewModel.recargarUsuario()
                    navController.navigate("stress_antes") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── App ─────────────────────────────────────────────────────────────
        composable("stress_antes") {
            // Reaccionamos directamente a la lista de sesiones, no solo a un booleano.
            // Asi solo decidimos cuando Firestore ya ha respondido de verdad.
            val sesiones by stressViewModel.todasLasSesiones.collectAsState()
            val cargado  by stressViewModel.cargado.collectAsState()

            LaunchedEffect(sesiones, cargado) {
                if (cargado && stressViewModel.yaRegistroHoy()) {
                    navController.navigate("main") {
                        popUpTo("stress_antes") { inclusive = true }
                    }
                }
            }

            if (!cargado) {
                // Mientras Firestore responde, mostramos una pantalla de carga
                // (asi nunca se ve la pantalla de estres "de relleno")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VerdeSalvia)
                }
            } else if (!stressViewModel.yaRegistroHoy()) {
                // Ya cargo y NO registro hoy: mostramos la pantalla de estres
                StressScreen(
                    titulo    = "¿Cómo estás ahora mismo?",
                    subtitulo = "Indica tu nivel de estrés antes de jugar",
                    onConfirmar = { nivel ->
                        estresAntes = nivel
                        navController.navigate("main") {
                            popUpTo("stress_antes") { inclusive = true }
                        }
                    }
                )
            }
            // Si ya cargo Y registro hoy, no dibujamos nada porque el
            // LaunchedEffect de arriba ya navega a "main"
        }

        composable("main") {
            LaunchedEffect(Unit) {
                settingsViewModel.cargarPerfil()
            }
            MainScreen(
                stressViewModel      = stressViewModel,
                diarioViewModel      = diarioViewModel,
                settingsViewModel    = settingsViewModel,
                onJuegoSeleccionado  = { juego ->
                    juegoActual = juego
                    navController.navigate(juego)
                },
                onVerHistorialDiario = {
                    navController.navigate("historial_diario")
                },
                onCerrarSesion = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onAbrirAjustes = {
                    navController.navigate("settings")
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onCerrarSesion = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBorrarCuenta = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVolver          = { navController.popBackStack() },
                settingsViewModel = settingsViewModel
            )
        }

        composable("historial_diario") {
            HistorialDiarioScreen(
                diarioViewModel = diarioViewModel,
                onVolver        = { navController.popBackStack() }
            )
        }

        composable("aros") {
            val scope = rememberCoroutineScope()
            ArosScreen(onVolver = {
                scope.launch {
                    val dest = if (stressViewModel.yaJugoHoy()) "main" else "stress_despues"
                    navController.navigate(dest) { popUpTo("aros") { inclusive = true } }
                }
            })
        }

        composable("mochis") {
            val scope = rememberCoroutineScope()
            MochisScreen(onVolver = {
                scope.launch {
                    val dest = if (stressViewModel.yaJugoHoy()) "main" else "stress_despues"
                    navController.navigate(dest) { popUpTo("mochis") { inclusive = true } }
                }
            })
        }

        composable("pesca") {
            val scope = rememberCoroutineScope()
            PescaScreen(onVolver = {
                scope.launch {
                    val dest = if (stressViewModel.yaJugoHoy()) "main" else "stress_despues"
                    navController.navigate(dest) { popUpTo("pesca") { inclusive = true } }
                }
            })
        }

        composable("respiracion") {
            val scope = rememberCoroutineScope()
            BreathingGameScreen(
                onBack = {
                    scope.launch {
                        val dest = if (stressViewModel.yaJugoHoy()) "main" else "stress_despues"
                        navController.navigate(dest) { popUpTo("respiracion") { inclusive = true } }
                    }
                }
            )
        }

        composable("stress_despues") {
            StressScreen(
                titulo         = "¿Cómo te sientes ahora?",
                subtitulo      = "Indica tu nivel de estrés después de jugar",
                estresAnterior = estresAntes,
                onConfirmar    = { nivel ->
                    stressViewModel.guardarSesion(
                        juego         = juegoActual,
                        estresAntes   = estresAntes,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    stressViewModel: StressViewModel,
    diarioViewModel: DiarioViewModel,
    settingsViewModel: SettingsViewModel,
    onJuegoSeleccionado: (String) -> Unit,
    onVerHistorialDiario: () -> Unit,
    onCerrarSesion: () -> Unit,
    onAbrirAjustes: () -> Unit
) {
    var tabSeleccionada by remember { mutableStateOf(0) }
    val partidasPorJuego by stressViewModel.partidasPorJuego.collectAsState()
    val darkMode by settingsViewModel.darkMode.collectAsState()
    val avatarIndex by settingsViewModel.avatarIndex.collectAsState()

    val bgColor = if (darkMode)
        MaterialTheme.colorScheme.background
    else
        Crema

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "🌿 Calmio",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 20.sp,
                        color      = VerdeSalvia
                    )
                },
                actions = {
                    val avatar = AVATARS[avatarIndex]
                    IconButton(onClick = onAbrirAjustes) {
                        Text(avatar.emoji, fontSize = 22.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tabSeleccionada == 0,
                    onClick  = { tabSeleccionada = 0 },
                    icon     = { Icon(Icons.Filled.SportsEsports, contentDescription = "Juegos") },
                    label    = { Text("Juegos") }
                )
                NavigationBarItem(
                    selected = tabSeleccionada == 1,
                    onClick  = { tabSeleccionada = 1 },
                    icon     = { Icon(Icons.Filled.Book, contentDescription = "Diario") },
                    label    = { Text("Diario") }
                )
                NavigationBarItem(
                    selected = tabSeleccionada == 2,
                    onClick  = { tabSeleccionada = 2 },
                    icon     = { Icon(Icons.Filled.History, contentDescription = "Historial") },
                    label    = { Text("Historial") }
                )
            }
        }
    ) { paddingValues ->
        when (tabSeleccionada) {
            0 -> GameSelectionScreen(
                modifier            = Modifier.padding(paddingValues),
                partidasPorJuego    = partidasPorJuego,
                onJuegoSeleccionado = onJuegoSeleccionado
            )
            1 -> DiarioScreen(
                modifier        = Modifier.padding(paddingValues),
                diarioViewModel = diarioViewModel,
                stressViewModel = stressViewModel,
                onVerHistorial  = onVerHistorialDiario
            )
            2 -> HistorialScreen(
                stressViewModel = stressViewModel
            )
        }
    }
}