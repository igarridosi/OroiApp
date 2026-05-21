package com.example.oroiapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.oroiapp.R
import com.example.oroiapp.viewmodel.EditSubscriptionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubscriptionScreen(
    viewModel: EditSubscriptionViewModel,
    onNavigateBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var isSaving by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    val nameError = attemptedSave && formState.name.isBlank()
    val amountError = attemptedSave && (formState.amount.isBlank() || (formState.amount.toDoubleOrNull() ?: 0.0) <= 0)

    val errorSaveMessage = stringResource(R.string.error_save_failed)

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            isSaving = true
                            try {
                                viewModel.deleteSubscription()
                                onNavigateBack()
                            } catch (e: Exception) {
                                isSaving = false
                                snackbarHostState.showSnackbar(errorSaveMessage)
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_subscription_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = formState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.name_field_label)) },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                supportingText = if (nameError) {
                    { Text(stringResource(R.string.error_name_required), color = MaterialTheme.colorScheme.error) }
                } else null
            )
            OutlinedTextField(
                value = formState.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text(stringResource(R.string.amount_field_label)) },
                modifier = Modifier.fillMaxWidth(),
                isError = amountError,
                supportingText = if (amountError) {
                    { Text(stringResource(R.string.error_amount_invalid), color = MaterialTheme.colorScheme.error) }
                } else null
            )
            BillingCycleSelector(
                selectedCycle = formState.billingCycle,
                onCycleSelected = viewModel::onBillingCycleChange
            )
            DatePickerField(
                selectedDate = formState.firstPaymentDate,
                onDateSelected = viewModel::onDateChange
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    attemptedSave = true
                    val hasErrors = formState.name.isBlank() ||
                        formState.amount.isBlank() ||
                        (formState.amount.toDoubleOrNull() ?: 0.0) <= 0
                    if (!hasErrors && !isSaving) {
                        scope.launch {
                            isSaving = true
                            focusManager.clearFocus()
                            try {
                                viewModel.saveSubscription()
                                onNavigateBack()
                            } catch (e: Exception) {
                                isSaving = false
                                snackbarHostState.showSnackbar(errorSaveMessage)
                            }
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.save_changes),
                    color = MaterialTheme.colorScheme.surface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = { if (!isSaving) showDeleteDialog = true },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.error)
            ) {
                Text(
                    stringResource(R.string.delete_subscription),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
