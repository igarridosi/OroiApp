package com.example.oroiapp.ui

import android.graphics.Paint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oroiapp.viewmodel.ChartData
import com.example.oroiapp.viewmodel.MainViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val chartData by viewModel.topExpensesChartData.collectAsState(initial = emptyList())

    val chartColors = listOf(
        Color(0xFF7A40F2), Color(0xFF26C6DA), Color(0xFFFFA726),
        Color(0xFFEF5350), Color(0xFF66BB6A), Color(0xFF8D6E63)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gastu Nagusiak") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atzera")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(36.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (chartData.isNotEmpty()) {
                // GRAFIKO INTERAKTIBOA
                InteractivePieChart(
                    data = chartData,
                    colors = chartColors,
                    modifier = Modifier.size(300.dp) // Tamaina apur bat handitu dugu
                )

                Spacer(modifier = Modifier.height(32.dp))

                ChartLegend(data = chartData, colors = chartColors)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ez dago daturik erakusteko.", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun InteractivePieChart(
    data: List<ChartData>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    chartBarWidth: Dp = 35.dp // Oinarrizko lodiera
) {
    val totalSum = data.sumOf { it.value.toDouble() }.toFloat()
    var selectedIndex by remember { mutableStateOf(-1) }

    val animatedStrokeWidths = data.mapIndexed { index, _ ->
        val isSelected = index == selectedIndex
        animateDpAsState(
            targetValue = if (isSelected) chartBarWidth * 1.6f else chartBarWidth, // Hautatua %60 lodiagoa
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing), // Animazio leuna eta naturala
            label = "strokeWidth_$index"
        )
    }

    val animatedColors = data.mapIndexed { index, _ ->
        val isSelected = index == selectedIndex
        val isAnySelected = selectedIndex != -1
        val originalColor = colors.getOrElse(index) { Color.Gray }

        // Kolore "itzalia" (gris argi gardena)
        val dimmedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

        val targetColor = if (isAnySelected && !isSelected) {
            dimmedColor
        } else {
            originalColor
        }

        animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(durationMillis = 400),
            label = "color_$index"
        )
    }

    val sweepAngles = remember(data) {
        data.map { (it.value / totalSum) * 360f }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val vec = tapOffset - center
                        val dist = vec.getDistance()

                        // Erradioak kalkulatu detekziorako (gutxi gorabeherakoak)
                        val outerRadius = size.width / 2f
                        val innerRadius = outerRadius - (chartBarWidth.toPx() * 2f) // Marjina handiagoa eman klik egiteko erraztasunerako

                        if (dist < innerRadius || dist > outerRadius) {
                            selectedIndex = -1
                            return@detectTapGestures
                        }

                        var angle = Math.toDegrees(atan2(vec.y.toDouble(), vec.x.toDouble())).toFloat()
                        angle += 90f
                        if (angle < 0) angle += 360f

                        var currentAngle = 0f
                        data.forEachIndexed { index, _ ->
                            val sweep = sweepAngles[index]
                            if (angle >= currentAngle && angle <= currentAngle + sweep) {
                                selectedIndex = if (selectedIndex == index) -1 else index
                                return@detectTapGestures
                            }
                            currentAngle += sweep
                        }
                    }
                }
        ) {
            var startAngle = -90f

            data.forEachIndexed { index, _ ->
                val sweepAngle = sweepAngles[index]

                // Animazio balioak irakurri
                val strokeWidth = animatedStrokeWidths[index].value.toPx()
                val color = animatedColors[index].value

                // Marraztu
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle, // Angelua apur bat murriztu daiteke tarteak uzteko (-2f), baina horrela ere ondo dago
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweepAngle
            }
        }

        if (selectedIndex != -1) {
            val item = data[selectedIndex]
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${"%.2f".format(item.value)}€",
                    style = MaterialTheme.typography.headlineMedium, // Handiagoa
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hilean",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Zure 5 Harpidetz Garestienak",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Gray
                )
                Text(
                    text = "${"%.2f".format(data.sumOf { it.value.toDouble() })}€",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun ChartLegend(data: List<ChartData>, colors: List<Color>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
    ) {
        data.forEachIndexed { index, item ->
            val color = colors.getOrElse(index) { Color.Gray }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = item.label, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(text = "${"%.2f".format(item.value)}€", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}