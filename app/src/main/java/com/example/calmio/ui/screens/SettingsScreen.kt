package com.example.calmio.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calmio.ui.theme.*
import com.example.calmio.viewmodel.SettingsViewModel

// ─── Avatares predeterminados ────────────────────────────────────────────────
data class AvatarOption(val emoji: String, val background: Color)

val AVATARS = listOf(
    AvatarOption("🌿", Color(0xFF7C9E87)),
    AvatarOption("🦋", Color(0xFF9BB5D6)),
    AvatarOption("🌸", Color(0xFFD49BB5)),
    AvatarOption("🐢", Color(0xFF6B9E6B)),
    AvatarOption("🌊", Color(0xFF5B9EC9)),
    AvatarOption("🍃", Color(0xFF89B89A)),
    AvatarOption("🦔", Color(0xFFC4956A)),
    AvatarOption("🌙", Color(0xFF8B7EC8)),
    AvatarOption("☀️", Color(0xFFD4A85A)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onCerrarSesion: () -> Unit,
    onBorrarCuenta: () -> Unit,
    onVolver: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val darkMode            by settingsViewModel.darkMode.collectAsState()
    val notificaciones      by settingsViewModel.notificationsEnabled.collectAsState()
    val avatarIndex         by settingsViewModel.avatarIndex.collectAsState()
    val reminderHour        by settingsViewModel.reminderHour.collectAsState()
    val reminderMinute      by settingsViewModel.reminderMinute.collectAsState()

    var mostrarDialogoCerrar   by remember { mutableStateOf(false) }
    var mostrarDialogoBorrar   by remember { mutableStateOf(false) }
    var mostrarSelectorAvatar  by remember { mutableStateOf(false) }
    var mostrarTimePicker      by remember { mutableStateOf(false) }

    // ── Diálogo: Cerrar sesión ───────────────────────────────────────────────
    if (mostrarDialogoCerrar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrar = false },
            icon  = { Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = Terracota) },
            title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold) },
            text  = { Text("¿Seguro que quieres salir de tu cuenta?") },
            confirmButton = {
                Button(
                    onClick = { mostrarDialogoCerrar = false; onCerrarSesion() },
                    colors  = ButtonDefaults.buttonColors(containerColor = Terracota)
                ) { Text("Sí, salir") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrar = false }) { Text("Cancelar") }
            }
        )
    }

    // ── Diálogo: Borrar cuenta ───────────────────────────────────────────────
    if (mostrarDialogoBorrar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrar = false },
            icon  = { Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = Error) },
            title = { Text("Borrar cuenta", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "Esta acción eliminará permanentemente tu cuenta y todos tus datos. No se puede deshacer.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { mostrarDialogoBorrar = false; onBorrarCuenta() },
                    colors  = ButtonDefaults.buttonColors(containerColor = Error)
                ) { Text("Borrar definitivamente") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoBorrar = false }) { Text("Cancelar") }
            }
        )
    }

    // ── Diálogo: TimePicker ──────────────────────────────────────────────────
    if (mostrarTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour   = reminderHour,
            initialMinute = reminderMinute,
            is24Hour      = true
        )
        AlertDialog(
            onDismissRequest = { mostrarTimePicker = false },
            title = {
                Text(
                    "Hora del recordatorio",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp
                )
            },
            text = {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(8.dp))
                    TimePicker(
                        state  = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor          = if (darkMode) Color(0xFF2C2C2C) else Color(0xFFF0EBE3),
                            selectorColor           = VerdeSalvia,
                            containerColor          = Color.Transparent,
                            periodSelectorBorderColor = VerdeSalvia,
                            timeSelectorSelectedContainerColor   = VerdeSalvia,
                            timeSelectorUnselectedContainerColor = if (darkMode) Color(0xFF2C2C2C) else Color(0xFFF0EBE3),
                            timeSelectorSelectedContentColor     = Color.White,
                            timeSelectorUnselectedContentColor   = if (darkMode) Color.White else TextoPrincipal,
                            clockDialSelectedContentColor        = Color.White,
                            clockDialUnselectedContentColor      = if (darkMode) Color.White else TextoPrincipal,
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.setReminderTime(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        mostrarTimePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeSalvia)
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarTimePicker = false }) { Text("Cancelar") }
            }
        )
    }

    // ── Hoja inferior: Selector de avatar ────────────────────────────────────
    if (mostrarSelectorAvatar) {
        ModalBottomSheet(
            onDismissRequest = { mostrarSelectorAvatar = false },
            containerColor   = if (darkMode) Color(0xFF1E1E1E) else Crema
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "Elige tu avatar",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                    color      = if (darkMode) Color.White else TextoPrincipal,
                    modifier   = Modifier.padding(bottom = 20.dp)
                )
                LazyVerticalGrid(
                    columns               = GridCells.Fixed(3),
                    verticalArrangement   = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier              = Modifier.height(220.dp)
                ) {
                    itemsIndexed(AVATARS) { index, avatar ->
                        val selected = index == avatarIndex
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(avatar.background.copy(alpha = if (selected) 1f else 0.6f))
                                .border(
                                    width = if (selected) 3.dp else 0.dp,
                                    color = if (selected) VerdeSalvia else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    settingsViewModel.setAvatarIndex(index)
                                    mostrarSelectorAvatar = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatar.emoji, fontSize = 32.sp)
                        }
                    }
                }
            }
        }
    }

    // ── Pantalla principal ───────────────────────────────────────────────────
    val bgColor = if (darkMode) Color(0xFF121212) else Crema

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ajustes",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 20.sp,
                        color      = if (darkMode) Color.White else TextoPrincipal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = if (darkMode) Color.White else VerdeSalvia
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Sección: Perfil ──────────────────────────────────────────────
            SettingsSection(title = "Perfil", darkMode = darkMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val avatar = AVATARS[avatarIndex]
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(avatar.background)
                                .clickable { mostrarSelectorAvatar = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatar.emoji, fontSize = 44.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { mostrarSelectorAvatar = true }) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint     = VerdeSalvia,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Cambiar avatar", color = VerdeSalvia, fontSize = 13.sp)
                        }
                    }
                }
            }

            // ── Sección: Apariencia ──────────────────────────────────────────
            SettingsSection(title = "Apariencia", darkMode = darkMode) {
                SettingsToggleRow(
                    icon      = if (darkMode) Icons.Filled.DarkMode else Icons.Outlined.LightMode,
                    iconTint  = if (darkMode) Color(0xFF8B7EC8) else Terracota,
                    title     = "Modo oscuro",
                    subtitle  = if (darkMode) "Tema oscuro activado" else "Tema claro activado",
                    checked   = darkMode,
                    onChecked = { settingsViewModel.setDarkMode(it) },
                    darkMode  = darkMode
                )
            }

            // ── Sección: Notificaciones ──────────────────────────────────────
            SettingsSection(title = "Notificaciones", darkMode = darkMode) {

                // Toggle de activar/desactivar
                SettingsToggleRow(
                    icon      = if (notificaciones) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                    iconTint  = if (notificaciones) VerdeSalvia else TextoSuave,
                    title     = "Recordatorio diario",
                    subtitle  = if (notificaciones) "Activado" else "Desactivado",
                    checked   = notificaciones,
                    onChecked = { settingsViewModel.setNotifications(it) },
                    darkMode  = darkMode
                )

                // Selector de hora
                AnimatedVisibility(
                    visible = notificaciones,
                    enter   = expandVertically() + fadeIn(),
                    exit    = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(
                            color    = if (darkMode) Color(0xFF2C2C2C) else Color(0xFFEAE5DF),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mostrarTimePicker = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VerdeSalvia.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint     = VerdeSalvia,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Hora del recordatorio",
                                    fontWeight = FontWeight.Medium,
                                    fontSize   = 15.sp,
                                    color      = if (darkMode) Color.White else TextoPrincipal
                                )
                                Text(
                                    "Toca para cambiar la hora",
                                    fontSize = 12.sp,
                                    color    = if (darkMode) Color(0xFF8C8C8C) else TextoSuave
                                )
                            }
                            // Chip con la hora actual
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = VerdeSalvia.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text     = "%02d:%02d".format(reminderHour, reminderMinute),
                                    color    = VerdeSalvia,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Sección: Información ─────────────────────────────────────────
            SettingsSection(title = "Información", darkMode = darkMode) {
                SettingsInfoRow(
                    icon     = Icons.Filled.Info,
                    iconTint = VerdeSalvia,
                    title    = "Versión de la app",
                    value    = "1.0.0",
                    darkMode = darkMode
                )
                HorizontalDivider(
                    color    = if (darkMode) Color(0xFF2C2C2C) else Color(0xFFEAE5DF),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                SettingsInfoRow(
                    icon      = Icons.Filled.Shield,
                    iconTint  = VerdeSalvia,
                    title     = "Política de privacidad",
                    value     = "",
                    darkMode  = darkMode,
                    clickable = true
                )
                HorizontalDivider(
                    color    = if (darkMode) Color(0xFF2C2C2C) else Color(0xFFEAE5DF),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                SettingsInfoRow(
                    icon      = Icons.Filled.HelpOutline,
                    iconTint  = VerdeSalvia,
                    title     = "Ayuda y soporte",
                    value     = "",
                    darkMode  = darkMode,
                    clickable = true
                )
            }

            // ── Sección: Cuenta ──────────────────────────────────────────────
            SettingsSection(title = "Cuenta", darkMode = darkMode) {
                SettingsActionRow(
                    icon     = Icons.Filled.ExitToApp,
                    iconTint = Terracota,
                    title    = "Cerrar sesión",
                    color    = Terracota,
                    darkMode = darkMode,
                    onClick  = { mostrarDialogoCerrar = true }
                )
                HorizontalDivider(
                    color    = if (darkMode) Color(0xFF2C2C2C) else Color(0xFFEAE5DF),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                SettingsActionRow(
                    icon     = Icons.Filled.DeleteForever,
                    iconTint = Error,
                    title    = "Borrar cuenta",
                    color    = Error,
                    darkMode = darkMode,
                    onClick  = { mostrarDialogoBorrar = true }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Componentes reutilizables ───────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    darkMode: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text          = title.uppercase(),
            fontSize      = 11.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = if (darkMode) Color(0xFF8C8C8C) else TextoSuave,
            letterSpacing = 1.sp,
            modifier      = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(18.dp),
            colors    = CardDefaults.cardColors(
                containerColor = if (darkMode) Color(0xFF1E1E1E) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
    darkMode: Boolean
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.Medium,
                fontSize   = 15.sp,
                color      = if (darkMode) Color.White else TextoPrincipal
            )
            Text(subtitle, fontSize = 12.sp, color = if (darkMode) Color(0xFF8C8C8C) else TextoSuave)
        }
        Switch(
            checked         = checked,
            onCheckedChange = onChecked,
            colors          = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = VerdeSalvia,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = if (darkMode) Color(0xFF3C3C3C) else Color(0xFFDDD8D2)
            )
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    darkMode: Boolean,
    clickable: Boolean = false
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .then(if (clickable) Modifier.clickable { } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            title,
            fontWeight = FontWeight.Medium,
            fontSize   = 15.sp,
            color      = if (darkMode) Color.White else TextoPrincipal,
            modifier   = Modifier.weight(1f)
        )
        if (value.isNotEmpty()) {
            Text(value, fontSize = 13.sp, color = if (darkMode) Color(0xFF8C8C8C) else TextoSuave)
        } else if (clickable) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint     = if (darkMode) Color(0xFF8C8C8C) else TextoSuave,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    color: Color,
    darkMode: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = color)
    }
}