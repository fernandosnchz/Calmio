package com.example.calmio.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.Canvas
import com.example.calmio.ui.components.CalmioTextField
import com.example.calmio.ui.theme.*
import com.example.calmio.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    loginViewModel: LoginViewModel = viewModel()
) {
    val email by loginViewModel.email.collectAsState()
    val password by loginViewModel.password.collectAsState()
    val errorMessage by loginViewModel.errorMessage.collectAsState()
    val isLoading by loginViewModel.isLoading.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo circular
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(VerdeSalvia),
                contentAlignment = Alignment.Center
            ) {
                LeafDecoration(
                    modifier = Modifier.size(44.dp),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Calmio",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = TextoPrincipal,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Tu espacio de calma",
                fontSize = 13.sp,
                color = TextoSuave,
                fontWeight = FontWeight.Light,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Card principal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.88f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        text = "Bienvenido",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Inicia sesión para continuar",
                        fontSize = 12.sp,
                        color = TextoSuave,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 22.dp)
                    )

                    // Campo Email con label superior
                    LabeledField(label = "EMAIL") {
                        CalmioTextField(
                            value = email,
                            onValueChange = { loginViewModel.onEmailChange(it) },
                            label = "Correo del usuario",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Campo Contraseña con label superior
                    LabeledField(label = "CONTRASEÑA") {
                        CalmioTextField(
                            value = password,
                            onValueChange = { loginViewModel.onPasswordChange(it) },
                            label = "••••••••",
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible)
                                            Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = "Ver contraseña",
                                        tint = VerdeSalvia
                                    )
                                }
                            }
                        )
                    }

                    // ¿Olvidaste tu contraseña? ← conectado
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            fontSize = 12.sp,
                            color = Terracota,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onForgotPassword() } // ← conectado
                        )
                    }

                    // Error message
                    AnimatedVisibility(visible = errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Botón principal
                    Button(
                        onClick = { loginViewModel.onLoginClick(onLoginSuccess) },
                        enabled = !isLoading, // ← añadido
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeSalvia
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        // Muestra spinner mientras carga ← añadido
                        if (isLoading)
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        else
                            Text(
                                "Entrar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.2.sp
                            )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Separador
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE8E3DE)
                        )
                        Text(
                            text = "  o continúa con  ",
                            fontSize = 11.sp,
                            color = TextoSuave,
                            fontWeight = FontWeight.Light
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE8E3DE)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón secundario crear cuenta
                    OutlinedButton(
                        onClick = onRegisterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp, Color(0xFFE0DAD4)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = VerdeSalvia
                        )
                    ) {
                        Text(
                            "Crear cuenta nueva",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer
            Row {
                Text(
                    "¿Problemas para acceder? ",
                    fontSize = 12.sp,
                    color = TextoSuave,
                    fontWeight = FontWeight.Light
                )
                Text(
                    "Ayuda",
                    fontSize = 12.sp,
                    color = Terracota,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { /* abrir ayuda */ }
                )
            }
        }
    }
}

// Componente helper para label + field
@Composable
fun LabeledField(
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

// Hoja decorativa con Canvas
@Composable
private fun LeafDecoration(
    modifier: Modifier = Modifier,
    color: Color = VerdeSalvia
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w / 2f, 0f)
            cubicTo(0f, h * 0.15f, 0f * 0.1f, h * 0.6f, w / 2f, h)
            cubicTo(w, h * 0.6f, w, h * 0.15f, w / 2f, 0f)
            close()
        }
        drawPath(path = path, color = color)
    }
}