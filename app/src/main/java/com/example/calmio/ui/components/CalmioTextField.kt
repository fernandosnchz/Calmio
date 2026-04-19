package com.example.calmio.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.calmio.ui.theme.Crema
import com.example.calmio.ui.theme.TextoSuave
import com.example.calmio.ui.theme.VerdeSalvia
import androidx.compose.ui.graphics.Color

@Composable
fun CalmioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
                shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Crema,
            focusedContainerColor = Crema,
            focusedBorderColor = VerdeSalvia,
            unfocusedBorderColor = Color(0xFFE0DAD4),
            focusedLabelColor = VerdeSalvia,
            unfocusedLabelColor = TextoSuave,
            cursorColor = VerdeSalvia
        )
    )
}