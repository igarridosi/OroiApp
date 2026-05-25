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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oroiapp.R
import com.example.oroiapp.ui.theme.ChartPalette
import com.example.oroiapp.viewmodel.ChartData
import com.example.oroiapp.viewmodel.MainViewModel
import kotlin.math.atan2

/** Gap in degrees between each donut segment. */
private const val SEGMENT_GAP_DEG = 2.5f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val chartData by viewModel.allExpensesChartData.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.top_expenses_title),
                        fontWeight = FontWeight.Bold
                    )
                },
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
        if (chartData.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                InteractivePieChart(
                    data = chartData,
                    modifier = Modifier.size(280.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ChartLegend(
                    data = chartData,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.no_data),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InteractivePieChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    chartBarWidth: Dp = 32.dp
) {
    val totalSum = data.sumOf { it.value.toDouble() }.toFloat()
    var selectedIndex by remember { mutableStateOf(-1) }

    // Total gap taken by all segment spacers
    val totalGap = SEGMENT_GAP_DEG * data.size
    val usableAngle = 360f - totalGap

    val sweepAngles = remember(data) {
        data.map { (it.value / totalSum) * usableAngle }
    }

    val animatedStrokeWidths = data.mapIndexed { index, _ ->
        animateDpAsState(
            targetValue = if (index == selectedIndex) chartBarWidth * 1.5f else chartBarWidth,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "strokeWidth_$index"
        )
    }

    val animatedColors = data.mapIndexed { index, _ ->
        val isSelected = index == selectedIndex
        val isAnySelected = selectedIndex != -1
        val originalColor = ChartPalette[index % ChartPalette.size]
        val dimmedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        val targetColor = if (isAnySelected && !isSelected) dimmedColor else originalColor
        animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(durationMillis = 350),
            label = "color_$index"
        )
    }

    val perMonthLabel = stringResource(R.string.per_month)
    val allSubsLabel = stringResource(R.string.all_subscriptions_label)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
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
                            val sweep = sweepAngles[index] + SEGMENT_GAP_DEG
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
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle + SEGMENT_GAP_DEG
            }
        }

        // Center text
        if (selectedIndex != -1 && selectedIndex < data.size) {
            val item = data[selectedIndex]
            val pct = (item.value / totalSum * 100f)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${"%.2f".format(item.value)}€",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${"%.1f".format(pct)}% · $perMonthLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = allSubsLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${"%.2f".format(data.sumOf { it.value.toDouble() })}€",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = perMonthLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ChartLegend(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    val totalSum = data.sumOf { it.value.toDouble() }.toFloat()

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(data) { index, item ->
            val color = ChartPalette[index % ChartPalette.size]
            val pct = if (totalSum > 0) item.value / totalSum * 100f else 0f

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Color dot
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    // Name
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Percentage badge
                    Text(
                        text = "${"%.1f".format(pct)}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp)
                    )

                    // Amount
                    Text(
                        text = "${"%.2f".format(item.value)}€",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
