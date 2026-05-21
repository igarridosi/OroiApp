package com.example.oroiapp.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.oroiapp.R
import com.example.oroiapp.model.BillingCycle
import com.example.oroiapp.viewmodel.AddEditViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import com.example.oroiapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    viewModel: AddEditViewModel,
    onNavigateBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_subscription_title)) },
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
            modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ServiceNameInput(
                viewModel = viewModel,
                currentName = formState.name,
                onNameChange = viewModel::onNameChange
            )
            OutlinedTextField(
                value = formState.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text(stringResource(R.string.amount_hint)) },
                modifier = Modifier.fillMaxWidth()
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
                    if (!isSaving) {
                        scope.launch {
                            isSaving = true
                            focusManager.clearFocus()
                            viewModel.saveSubscription()
                            onNavigateBack()
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.save_subscription),
                    color = MaterialTheme.colorScheme.surface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceNameInput(
    viewModel: AddEditViewModel,
    currentName: String,
    onNameChange: (String) -> Unit
) {
    val predefinedNames by viewModel.predefinedServiceNames.collectAsState()
    val otherOption = stringResource(R.string.other_option)
    val dropdownOptions = predefinedNames + otherOption

    var expanded by remember { mutableStateOf(false) }
    var isManualInputVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = if (isManualInputVisible) otherOption else currentName,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.service_name_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                dropdownOptions.forEach { serviceName ->
                    DropdownMenuItem(
                        text = { Text(serviceName) },
                        onClick = {
                            if (serviceName == otherOption) {
                                isManualInputVisible = true
                                onNameChange("")
                            } else {
                                isManualInputVisible = false
                                onNameChange(serviceName)
                            }
                            expanded = false
                        }
                    )
                }
            }
        }

        AnimatedVisibility(visible = isManualInputVisible) {
            OutlinedTextField(
                value = currentName,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.manual_name_label)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingCycleSelector(
    selectedCycle: BillingCycle,
    onCycleSelected: (BillingCycle) -> Unit,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    var expanded by remember { mutableStateOf(false) }
    val cycleOptions = BillingCycle.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = when (selectedCycle) {
                BillingCycle.WEEKLY -> stringResource(R.string.billing_weekly)
                BillingCycle.MONTHLY -> stringResource(R.string.billing_monthly)
                BillingCycle.ANNUAL -> stringResource(R.string.billing_annual)
            },
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.billing_cycle_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            cycleOptions.forEach { cycle ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (cycle) {
                                BillingCycle.WEEKLY -> stringResource(R.string.billing_weekly)
                                BillingCycle.MONTHLY -> stringResource(R.string.billing_monthly)
                                BillingCycle.ANNUAL -> stringResource(R.string.billing_annual)
                            },
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    onClick = {
                        onCycleSelected(cycle)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DatePickerField(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.time = selectedDate

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val pickDateDesc = stringResource(R.string.pick_date_description)
    val firstPaymentLabel = stringResource(R.string.first_payment_date_label)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            val newCalendar = Calendar.getInstance()
            newCalendar.set(selectedYear, selectedMonth, selectedDay)
            onDateSelected(newCalendar.time)
        }, year, month, day
    )

    OutlinedTextField(
        value = dateFormat.format(selectedDate),
        onValueChange = {},
        readOnly = true,
        label = { Text(firstPaymentLabel) },
        trailingIcon = {
            Icon(Icons.Default.DateRange, pickDateDesc, Modifier.clickable { datePickerDialog.show() })
        },
        modifier = Modifier.fillMaxWidth(),
    )
}
