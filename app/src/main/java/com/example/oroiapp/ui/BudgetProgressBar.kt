package com.example.oroiapp.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BudgetProgressBar(
    currentMonthlyCost: Double,
    budgetLimit: Double,
    onBudgetChange: (Double) -> Unit
) {
    // Dialogoa erakusteko egoera
    var showDialog by remember { mutableStateOf(false) }

    // Aurrekontua 0 bada, ez dugu barra erakusten, bakarrik konfiguratzeko botoia
    if (budgetLimit <= 0.0) {
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text("Ezarri Hileko Aurrekontua", color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    } else {
        // Aurrekontua badago, barra erakutsi
        val progress = (currentMonthlyCost / budgetLimit).toFloat().coerceIn(0f, 1f)
        val animatedProgress by animateFloatAsState(targetValue = progress, label = "BudgetProgress")

        // Kolorea aldatu gastuaren arabera:
        // <80% = Berdea, 80-100% = Laranja, >100% = Gorria
        val progressColor = when {
            currentMonthlyCost > budgetLimit -> Color(0xFFFF3C2A)
            currentMonthlyCost > budgetLimit * 0.8 -> Color(0xFFFF8B26) // Laranja
            else -> MaterialTheme.colorScheme.primary
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { showDialog = true } // Klik egitean editatzeko
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row (verticalAlignment = Alignment.CenterVertically){
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editatu",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryFixed
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hileko Aurrekontua",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryFixed
                    )
                }
                Row{
                    Card(
                        modifier = Modifier,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(24.dp)
                    ){
                        Text(
                            modifier = Modifier
                                .padding(6.dp),
                            text = "${"%.2f".format(currentMonthlyCost)}€ / ${"%.2f".format(budgetLimit)}€",
                            style = MaterialTheme.typography.labelMedium,
                            color = progressColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
            )
            if (currentMonthlyCost > budgetLimit) {
                Text(
                    text = "Aurrekontua gainditu duzu!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    // Aurrekontua aldatzeko leihoa
    if (showDialog) {
        BudgetEditDialog(
            currentBudget = budgetLimit,
            onDismiss = { showDialog = false },
            onConfirm = { newBudget ->
                onBudgetChange(newBudget)
                showDialog = false
            }
        )
    }
}

@Composable
fun BudgetEditDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var text by remember { mutableStateOf(if (currentBudget > 0) currentBudget.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ezarri Hileko Muga") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*\$"))) text = it },
                label = { Text("Zenbatekoa (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val budget = text.toDoubleOrNull() ?: 0.0
                    onConfirm(budget)
                }
            ) { Text("Gorde") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Ezeztatu") }
        }
    )
}