package com.example.oroiapp.ui


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.oroiapp.viewmodel.ChartData
import com.example.oroiapp.viewmodel.MainViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    // Datuak behatu
    val chartData by viewModel.topExpensesChartData.collectAsState(initial = emptyList())

    // Grafikorako kolore biziak definitu
    val chartColors = listOf(
        Color(0xFF7A40F2), // Zure Morea
        Color(0xFF26C6DA), // Zian
        Color(0xFFFFA726), // Laranja
        Color(0xFFEF5350), // Gorria
        Color(0xFF66BB6A), // Berdea
        Color(0xFF8D6E63)  // Marroia (badaezpada)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gastu Nagusiak") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atzera")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Top 5 Harpidetza Garestienak",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (chartData.isNotEmpty()) {
                // 1. GAZTA GRAFIKOA MARRAZTU
                PieChart(
                    data = chartData,
                    colors = chartColors,
                    modifier = Modifier.size(250.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 2. LEIENDA (Koloreen azalpena)
                ChartLegend(
                    data = chartData,
                    colors = chartColors
                )

            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ez dago daturik erakusteko.", color = Color.Gray)
                }
            }
        }
    }
}

// --- OSAGAI PERTSONALIZATUAK ---

@Composable
fun PieChart(
    data: List<ChartData>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    chartBarWidth: Dp = 35.dp // Eraztunaren lodiera
) {
    val totalSum = data.sumOf { it.value.toDouble() }.toFloat()

    Canvas(modifier = modifier) {
        // Zirkuluan non hasi marrazten (-90 gradu goian hasteko da)
        var startAngle = -90f

        data.forEachIndexed { index, item ->
            // Kalkulatu zenbat okupatzen duen zati honek (360 gradutik)
            val sweepAngle = (item.value / totalSum) * 360f
            val color = colors.getOrElse(index) { Color.Gray }

            // Arkua marraztu
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false, // 'false' eraztun bat egiteko (donut), 'true' gazta betea egiteko
                style = Stroke(width = chartBarWidth.toPx())
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
fun ChartLegend(
    data: List<ChartData>,
    colors: List<Color>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        data.forEachIndexed { index, item ->
            val color = colors.getOrElse(index) { Color.Gray }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Kolore bolatxoa
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Izena
                Text(
                    text = item.label, // Adib: "NET"
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Prezioa
                Text(
                    text = "${"%.2f".format(item.value)}€",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}