package com.example.calmio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calmio.ui.components.CalmioTextField
import com.example.calmio.ui.theme.*
import com.example.calmio.viewmodel.ForgotPasswordState
import com.example.calmio.viewmodel.ForgotPasswordViewModel

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    forgotViewModel: ForgotPasswordViewModel = viewModel()
) {
    val email by forgotViewModel.email.collectAsState()
    val uiState by forgotViewModel.uiState.collectAsState()

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
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
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
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Recuperar contraseña",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Te enviaremos un enlace a tu email",
                        fontSize = 12.sp,
                        color = TextoSuave,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    if (uiState is ForgotPasswordState.Success) {
                        Text(
                            "✅ Email enviado. Revisa tu bandeja de entrada.",
                            color = VerdeSalvia,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeSalvia)
                        ) {
                            Text("Volver al login")
                        }
                    } else {
                        CalmioTextField(
                            value = email,
                            onValueChange = forgotViewModel::onEmailChange,
                            label = "Tu correo electrónico",
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState is ForgotPasswordState.Error) {
                            Text(
                                (uiState as ForgotPasswordState.Error).message,
                                color = Error,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = forgotViewModel::onSendClick,
                            enabled = uiState !is ForgotPasswordState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeSalvia)
                        ) {
                            if (uiState is ForgotPasswordState.Loading)
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            else
                                Text("Enviar enlace", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}