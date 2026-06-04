package com.example.calmio.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calmio.ui.components.CalmioTextField
import com.example.calmio.ui.theme.*
import com.example.calmio.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    registerViewModel: RegisterViewModel = viewModel()
) {
    val uiState by registerViewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    // Cuando el registro va bien, la pantalla reacciona y navega (una sola vez).
    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) {
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Crema,
                        VerdeMenta.copy(alpha = 0.45f),
                        VerdeSalvia.copy(alpha = 0.3f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(400f, 900f)
                )
            )
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = TextoPrincipal
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.88f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Text(
                        text = "Crear cuenta",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Empieza tu espacio de calma",
                        fontSize = 12.sp,
                        color = TextoSuave,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 22.dp)
                    )

                    RegisterLabeledField(label = "NOMBRE") {
                        CalmioTextField(
                            value = uiState.name,
                            onValueChange = registerViewModel::onNameChange,
                            label = "Tu nombre",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    RegisterLabeledField(label = "EMAIL") {
                        CalmioTextField(
                            value = uiState.email,
                            onValueChange = registerViewModel::onEmailChange,
                            label = "Correo electrónico",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    RegisterLabeledField(label = "CONTRASEÑA") {
                        CalmioTextField(
                            value = uiState.password,
                            onValueChange = registerViewModel::onPasswordChange,
                            label = "Mínimo 6 caracteres",
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible)
                                            Icons.Filled.Visibility
                                        else Icons.Filled.VisibilityOff,
                                        contentDescription = "Ver contraseña",
                                        tint = VerdeSalvia
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    AnimatedVisibility(visible = uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = Error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { registerViewModel.onRegisterClick() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeSalvia)
                    ) {
                        if (uiState.isLoading)
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        else
                            Text("Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterLabeledField(
    label: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Terracota,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        content()
    }
}