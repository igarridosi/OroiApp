package com.example.oroiapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.oroiapp.R
import com.example.oroiapp.data.ThemeSetting
import com.example.oroiapp.viewmodel.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    uiState: MainUiState,
    currentLanguageTag: String,
    onThemeSelected: (ThemeSetting) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onUsernameUpdated: (String) -> Unit,
    onExportCsv: () -> Unit,
    onTestNotification: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nameInput by remember(uiState.username) { mutableStateOf(uiState.username) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── NAME SECTION ──────────────────────────────────────────────
            SectionLabel(stringResource(R.string.settings_name_section))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Button(
                    onClick = {
                        onUsernameUpdated(nameInput)
                        onDismiss()
                    },
                    enabled = nameInput.isNotBlank() && nameInput != uiState.username
                ) {
                    Text(stringResource(R.string.settings_name_update))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            // ── THEME SECTION ─────────────────────────────────────────────
            SectionLabel(stringResource(R.string.settings_theme_section))
            Spacer(modifier = Modifier.height(4.dp))
            ThemeSetting.entries.forEach { theme ->
                val label = when (theme) {
                    ThemeSetting.SYSTEM -> stringResource(R.string.theme_system)
                    ThemeSetting.LIGHT -> stringResource(R.string.theme_light)
                    ThemeSetting.DARK -> stringResource(R.string.theme_dark)
                }
                SettingsRadioRow(
                    label = label,
                    selected = uiState.currentTheme == theme,
                    onClick = { onThemeSelected(theme) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            // ── LANGUAGE SECTION ──────────────────────────────────────────
            SectionLabel(stringResource(R.string.settings_language_section))
            Spacer(modifier = Modifier.height(4.dp))
            val languages = listOf(
                "en" to stringResource(R.string.language_english),
                "es" to stringResource(R.string.language_spanish),
                "eu" to stringResource(R.string.language_basque)
            )
            languages.forEach { (tag, label) ->
                SettingsRadioRow(
                    label = label,
                    selected = currentLanguageTag == tag,
                    onClick = { onLanguageSelected(tag) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            // ── DATA SECTION ──────────────────────────────────────────────
            SectionLabel(stringResource(R.string.settings_data_section))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onExportCsv,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.export_csv),
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            // ── DEBUG / TEST SECTION ──────────────────────────────────────
            // TODO: Remove this section before final Play Store release
            SectionLabel("🧪 Debug")
            Spacer(modifier = Modifier.height(8.dp))
            var testSent by remember { mutableStateOf(false) }
            OutlinedButton(
                onClick = {
                    onTestNotification()
                    testSent = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (testSent) "Notification sent! Wait 15 s…" else "Test notification (15 s)",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsRadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
