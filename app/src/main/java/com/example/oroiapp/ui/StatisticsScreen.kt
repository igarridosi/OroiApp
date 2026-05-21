package com.example.oroiapp.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.oroiapp.R
import com.example.oroiapp.viewmodel.ChartData
import com.example.oroiapp.viewmodel.MainViewModel
import kotlin.math.atan2

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
                title = { Text(stringResource(R.string.top_expenses_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
                InteractivePieChart(
                    data = chartData,
                    colors = chartColors,
                    modifier = Modifier.size(300.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                ChartLegend(data = chartData, colors = chartColors)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_data), color = Color.Gray)
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
    chartBarWidth: Dp = 35.dp
) {
    val totalSum = data.sumOf { it.value.toDouble() }.toFloat()
    var selectedIndex by remember { mutableStateOf(-1) }

    val animatedStrokeWidths = data.mapIndexed { index, _ ->
        val isSelected = index == selectedIndex
        animateDpAsState(
            targetValue = if (isSelected) chartBarWidth * 1.6f else chartBarWidth,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "strokeWidth_$index"
        )
    }

    val animatedColors = data.mapIndexed { index, _ ->
        val isSelected = index == selectedIndex
        val isAnySelected = selectedIndex != -1
        val originalColor = colors.getOrElse(index) { Color.Gray }
        val dimmedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        val targetColor = if (isAnySelected && !isSelected) dimmedColor else originalColor
        animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(durationMillis = 400),
            label = "color_$index"
        )
    }

    val sweepAngles = remember(data) {
        data.map { (it.value / totalSum) * 360f }
    }

    val perMonthLabel = stringResource(R.string.per_month)
    val top5Label = stringResource(R.string.top_5_subscriptions)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val vec = tapOffset - center
                        val dist = vec.getDistance()
                        val outerRadius = size.width / 2f
                        val innerRadius = outerRadius - (chartBarWidth.toPx() * 2f)

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
                val strokeWidth = animatedStrokeWidths[index].value.toPx()
                val color = animatedColors[index].value
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
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
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = perMonthLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = top5Label,
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
