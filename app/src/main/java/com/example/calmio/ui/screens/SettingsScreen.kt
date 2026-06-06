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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calmio.ui.theme.*
import com.example.calmio.viewmodel.SettingsViewModel
import android.widget.Toast

data class AvatarOption(val emoji: String, val background: Color)

val AVATARS = listOf(
    AvatarOption("\uD83C\uDF3F", Color(0xFF7C9E87)),
    AvatarOption("\uD83E\uDD8B", Color(0xFF9BB5D6)),
    AvatarOption("\uD83C\uDF38", Color(0xFFD49BB5)),
    AvatarOption("\uD83D\uDC22", Color(0xFF6B9E6B)),
    AvatarOption("\uD83C\uDF0A", Color(0xFF5B9EC9)),
    AvatarOption("\uD83C\uDF43", Color(0xFF89B89A)),
    AvatarOption("\uD83E\uDD94", Color(0xFFC4956A)),
    AvatarOption("\uD83C\uDF19", Color(0xFF8B7EC8)),
    AvatarOption("\u2600\uFE0F", Color(0xFFD4A85A)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onCerrarSesion: () -> Unit,
    onBorrarCuenta: () -> Unit,
    onVolver: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val darkMode       = uiState.darkMode
    val notificaciones = uiState.notificationsEnabled
    val avatarIndex    = uiState.avatarIndex
    val reminderHour   = uiState.reminderHour
    val reminderMinute = uiState.reminderMinute
    val nombre         = uiState.nombreUsuario
    val email          = uiState.emailUsuario

    // NUEVO: contexto para mostrar mensajes (Toast) de error.
    val context = LocalContext.current

    var mostrarDialogoCerrar  by remember { mutableStateOf(false) }
    var mostrarDialogoBorrar  by remember { mutableStateOf(false) }
    var mostrarSelectorAvatar by remember { mutableStateOf(false) }
    var mostrarTimePicker     by remember { mutableStateOf(false) }

    // NUEVO: cuando la cuenta se borra con exito, navegamos al login.
    LaunchedEffect(uiState.cuentaBorrada) {
        if (uiState.cuentaBorrada) {
            onBorrarCuenta()
        }
    }

    // NUEVO: si hay un error al borrar, lo mostramos y lo limpiamos.
    LaunchedEffect(uiState.errorBorrado) {
        uiState.errorBorrado?.let { mensaje ->
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
            settingsViewModel.limpiarErrorBorrado()
        }
    }

    // -- Dialogo: Cerrar sesion ------------------------------------------------
    if (mostrarDialogoCerrar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrar = false },
            icon  = { Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = Terracota) },
            title = { Text("Cerrar sesion", fontWeight = FontWeight.Bold) },
            text  = { Text("\u00bfSeguro que quieres salir de tu cuenta?") },
            confirmButton = {
                Button(
                    onClick = { mostrarDialogoCerrar = false; onCerrarSesion() },
                    colors  = ButtonDefaults.buttonColors(containerColor = Terracota)
                ) { Text("Si, salir") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrar = false }) { Text("Cancelar") }
            }
        )
    }

    // -- Dialogo: Borrar cuenta ------------------------------------------------
    if (mostrarDialogoBorrar) {
        AlertDialog(
            onDismissRequest = { if (!uiState.borrandoCuenta) mostrarDialogoBorrar = false },
            icon  = { Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = Error) },
            title = { Text("Borrar cuenta", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "Esta accion eliminara permanentemente tu cuenta y todos tus datos. No se puede deshacer.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    // NUEVO: ahora llama al borrado real del ViewModel.
                    // No cerramos el dialogo aqui: se cerrara solo al navegar
                    // (exito) o se quedara para reintentar (error).
                    onClick = { settingsViewModel.eliminarCuenta() },
                    enabled = !uiState.borrandoCuenta,
                    colors  = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    if (uiState.borrandoCuenta) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text("Borrar definitivamente")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoBorrar = false },
                    enabled = !uiState.borrandoCuenta
                ) { Text("Cancelar") }
            }
        )
    }

    // -- Dialogo: TimePicker ---------------------------------------------------
    if (mostrarTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour   = reminderHour,
            initialMinute = reminderMinute,
            is24Hour      = true
        )
        AlertDialog(
            onDismissRequest = { mostrarTimePicker = false },
            title = { Text("Hora del recordatorio", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text  = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
                        settingsViewModel.setReminderTime(timePickerState.hour, timePickerState.minute)
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

    // -- Hoja inferior: Selector de avatar -------------------------------------
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

    val bgColor = if (darkMode) Color(0xFF121212) else Crema

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text("Ajustes", fontWeight = FontWeight.Bold, fontSize = 20.sp,
                        color = if (darkMode) Color.White else TextoPrincipal)
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver",
                            tint = if (darkMode) Color.White else VerdeSalvia)
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

            // -- Seccion: Perfil --------------------------------------------
            SettingsSection(title = "Perfil", darkMode = darkMode) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
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
                        Spacer(Modifier.height(10.dp))

                        if (nombre.isNotBlank()) {
                            Text(
                                text       = nombre,
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (darkMode) Color.White else TextoPrincipal
                            )
                        }
                        if (email.isNotBlank()) {
                            Text(
                                text     = email,
                                fontSize = 13.sp,
                                color    = if (darkMode) Color(0xFF8C8C8C) else TextoSuave
                            )
                        }

                        Spacer(Modifier.height(4.dp))
                        TextButton(onClick = { mostrarSelectorAvatar = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = null,
                                tint = VerdeSalvia, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Cambiar avatar", color = VerdeSalvia, fontSize = 13.sp)
                        }
                    }
                }
            }

            // -- Seccion: Apariencia ----------------------------------------
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

            // -- Seccion: Notificaciones ------------------------------------
            SettingsSection(title = "Notificaciones", darkMode = darkMode) {
                SettingsToggleRow(
                    icon      = if (notificaciones) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                    iconTint  = if (notificaciones) VerdeSalvia else TextoSuave,
                    title     = "Recordatorio diario",
                    subtitle  = if (notificaciones) "Activado" else "Desactivado",
                    checked   = notificaciones,
                    onChecked = { settingsViewModel.setNotifications(it) },
                    darkMode  = darkMode
                )
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
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                                    .background(VerdeSalvia.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Schedule, contentDescription = null,
                                    tint = VerdeSalvia, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Hora del recordatorio", fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp, color = if (darkMode) Color.White else TextoPrincipal)
                                Text("Toca para cambiar la hora", fontSize = 12.sp,
                                    color = if (darkMode) Color(0xFF8C8C8C) else TextoSuave)
                            }
                            Surface(shape = RoundedCornerShape(20.dp), color = VerdeSalvia.copy(alpha = 0.15f)) {
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

            // -- Seccion: Informacion ---------------------------------------
            SettingsSection(title = "Informacion", darkMode = darkMode) {
                SettingsInfoRow(icon = Icons.Filled.Info, iconTint = VerdeSalvia,
                    title = "Version de la app", value = "1.0.0", darkMode = darkMode)
                HorizontalDivider(color = if (darkMode) Color(0xFF2C2C2C) else Color(0xFFEAE5DF),
                    modifier = Modifier.padding(horizontal = 8.dp))
                SettingsInfoRow(icon = Icons.Filled.Shield, iconTint = VerdeSalvia,
                    title = "Politica de privacidad", value = "", darkMode = darkMode, clickable = true)
                HorizontalDivider(color = if (darkMode) Color(0xFF2C2C2C) else Color(0xFFEAE5DF),
                    modifier = Modifier.padding(horizontal = 8.dp))
                SettingsInfoRow(icon = Icons.Filled.HelpOutline, iconTint = VerdeSalvia,
                    title = "Ayuda y soporte", value = "", darkMode = darkMode, clickable = true)
            }

            // -- Seccion: Cuenta --------------------------------------------
            SettingsSection(title = "Cuenta", darkMode = darkMode) {
                SettingsActionRow(icon = Icons.Filled.ExitToApp, iconTint = Terracota,
                    title = "Cerrar sesion", color = Terracota, darkMode = darkMode,
                    onClick = { mostrarDialogoCerrar = true })
                HorizontalDivider(color = if (darkMode) Color(0xFF2C2C2C) else Color(0xFFEAE5DF),
                    modifier = Modifier.padding(horizontal = 8.dp))
                SettingsActionRow(icon = Icons.Filled.DeleteForever, iconTint = Error,
                    title = "Borrar cuenta", color = Error, darkMode = darkMode,
                    onClick = { mostrarDialogoBorrar = true })
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// --- Componentes reutilizables ----------------------------------------------

@Composable
private fun SettingsSection(title: String, darkMode: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            color = if (darkMode) Color(0xFF8C8C8C) else TextoSuave,
            letterSpacing = 1.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(18.dp),
            colors    = CardDefaults.cardColors(
                containerColor = if (darkMode) Color(0xFF1E1E1E) else Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) { Column(content = content) }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector, iconTint: Color, title: String, subtitle: String,
    checked: Boolean, onChecked: (Boolean) -> Unit, darkMode: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
            .background(iconTint.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp,
                color = if (darkMode) Color.White else TextoPrincipal)
            Text(subtitle, fontSize = 12.sp, color = if (darkMode) Color(0xFF8C8C8C) else TextoSuave)
        }
        Switch(checked = checked, onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = Color.White, checkedTrackColor = VerdeSalvia,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = if (darkMode) Color(0xFF3C3C3C) else Color(0xFFDDD8D2)
            ))
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector, iconTint: Color, title: String, value: String,
    darkMode: Boolean, clickable: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()
        .then(if (clickable) Modifier.clickable { } else Modifier)
        .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
            .background(iconTint.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp,
            color = if (darkMode) Color.White else TextoPrincipal, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) {
            Text(value, fontSize = 13.sp, color = if (darkMode) Color(0xFF8C8C8C) else TextoSuave)
        } else if (clickable) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null,
                tint = if (darkMode) Color(0xFF8C8C8C) else TextoSuave, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector, iconTint: Color, title: String,
    color: Color, darkMode: Boolean, onClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
            .background(iconTint.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = color)
    }
}