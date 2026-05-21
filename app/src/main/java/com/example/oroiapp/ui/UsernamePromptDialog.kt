package com.example.oroiapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.oroiapp.R

@Composable
fun UsernamePromptDialog(
    currentInput: String,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(R.string.welcome_title), color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                Text(stringResource(R.string.username_prompt_text), color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = currentInput,
                    onValueChange = onInputChange,
                    label = { Text(stringResource(R.string.your_name_label)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = currentInput.isNotBlank(),
            ) {
                Text(stringResource(R.string.save), color = MaterialTheme.colorScheme.surface)
            }
        },
        dismissButton = null
    )
}
