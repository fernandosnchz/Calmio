package com.example.calmio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.calmio.ui.theme.Crema
import com.example.calmio.ui.theme.VerdeMenta
import com.example.calmio.ui.theme.VerdeSalvia
import com.example.calmio.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    val email by loginViewModel.email.collectAsState()
    val password by loginViewModel.password.collectAsState()
    val errorMessage by loginViewModel.errorMessage.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Crema, VerdeMenta.copy(alpha = 0.3f))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo / Título
            Text(
                text = "🌿",
                fontSize = 56.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Calmio",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeSalvia
            )

            Text(
                text = "Tu espacio de calma",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(52.dp))

            // Card con los campos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Bienvenido",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    CalmioTextField(
                        value = email,
                        onValueChange = { loginViewModel.onEmailChange(it) },
                        label = "Email"
                    )

                    CalmioTextField(
                        value = password,
                        onValueChange = { loginViewModel.onPasswordChange(it) },
                        label = "Contraseña",
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Filled.Visibility
                                    else
                                        Icons.Filled.VisibilityOff,
                                    contentDescription = "Ver contraseña",
                                    tint = VerdeSalvia
                                )
                            }
                        }
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { loginViewModel.onLoginClick(onLoginSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeSalvia
                        )
                    ) {
                        Text(
                            "Entrar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}