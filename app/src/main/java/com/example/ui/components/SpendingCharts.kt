package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CategorySummary
import com.example.domain.model.DailySpendPoint
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CurrencyFormatter
import kotlin.math.max

// ==========================================
// 1. PIE CHART / DONUT CHART
// ==========================================
@Composable
fun CategoryPieChart(
    summaries: List<CategorySummary>,
    totalAmount: Double,
    currencySymbol: String,
    title: String = "Category Distribution",
    modifier: Modifier = Modifier
) {
    if (summaries.isEmpty() || totalAmount <= 0.0) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No category data available",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Add transactions to view the pie chart breakdown.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(summaries) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    val selectedSummary = selectedIndex?.let { summaries.getOrNull(it) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "${summaries.size} categories",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Donut Pie Canvas with Center Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(190.dp)
                ) {
                    val strokeWidth = 32.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)

                    var startAngle = -90f
                    val totalProgress = animatedProgress.value

                    summaries.forEachIndexed { index, summary ->
                        val targetSweep = (summary.percentageOfTotal / 100f * 360f).toFloat() * totalProgress
                        val sweepAngle = targetSweep.coerceAtLeast(1.5f)
                        val color = CategoryIconHelper.parseColor(summary.category.colorHex)
                        val isSelected = selectedIndex == index

                        drawArc(
                            color = if (isSelected) color else color.copy(alpha = 0.9f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 2f, // Subtle gap between slices
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = if (isSelected) strokeWidth + 8.dp.toPx() else strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )
                        startAngle += sweepAngle
                    }
                }

                // Center Content (Total or Selected Category Details)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    if (selectedSummary != null) {
                        Text(
                            text = selectedSummary.category.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = CurrencyFormatter.format(selectedSummary.totalAmount, currencySymbol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${String.format("%.1f", selectedSummary.percentageOfTotal)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.format(totalAmount, currencySymbol),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Interactive Color Legend Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                summaries.forEachIndexed { index, summary ->
                    val isSelected = selectedIndex == index
                    val catColor = CategoryIconHelper.parseColor(summary.category.colorHex)

                    Surface(
                        onClick = {
                            selectedIndex = if (selectedIndex == index) null else index
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(catColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = summary.category.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${String.format("%.1f", summary.percentageOfTotal)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = CurrencyFormatter.format(summary.totalAmount, currencySymbol),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. LINE CHART (TREND OVER TIME)
// ==========================================
@Composable
fun CashflowTrendLineChart(
    dailyData: List<DailySpendPoint>,
    currencySymbol: String,
    title: String = "Spending Trend Line",
    lineColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (dailyData.isEmpty() || dailyData.all { it.amount <= 0.0 }) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No trend data recorded yet",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Add transactions to view the cashflow trend line.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxSpend = remember(dailyData) {
        val maxVal = dailyData.maxOfOrNull { it.amount } ?: 0.0
        max(maxVal, 100.0)
    }

    val selectedPoint = selectedIndex?.let { dailyData.getOrNull(it) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = lineColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = selectedPoint?.let { "${it.dayName}: ${CurrencyFormatter.format(it.amount, currencySymbol)}" }
                        ?: "Tap points to inspect",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedPoint != null) lineColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Line Canvas
            val scrollState = rememberScrollState()
            val totalPoints = dailyData.size
            val pointSpacing = 28.dp
            val canvasContentWidth = (totalPoints * 28 + 40).dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .horizontalScroll(scrollState)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(maxOf(320.dp, canvasContentWidth))
                        .height(170.dp)
                        .pointerInput(dailyData) {
                            detectTapGestures { offset ->
                                val step = size.width / (dailyData.size - 1).coerceAtLeast(1).toFloat()
                                val index = ((offset.x + step / 2f) / step).toInt().coerceIn(0, dailyData.lastIndex)
                                selectedIndex = index
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val bottomPadding = 30.dp.toPx()
                    val canvasHeight = size.height - bottomPadding
                    val count = dailyData.size
                    val stepX = if (count > 1) canvasWidth / (count - 1).toFloat() else canvasWidth

                    // Draw subtle grid lines (3 reference levels)
                    val gridColor = Color.LightGray.copy(alpha = 0.35f)
                    for (i in 0..2) {
                        val y = canvasHeight * (i / 2f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Build line and fill paths
                    val linePath = Path()
                    val fillPath = Path()

                    val coordinates = dailyData.mapIndexed { index, point ->
                        val x = index * stepX
                        val normalizedY = if (maxSpend > 0) (point.amount / maxSpend).toFloat() else 0f
                        val y = canvasHeight - (normalizedY * (canvasHeight - 16.dp.toPx()))
                        Offset(x, y)
                    }

                    if (coordinates.isNotEmpty()) {
                        linePath.moveTo(coordinates[0].x, coordinates[0].y)
                        fillPath.moveTo(coordinates[0].x, canvasHeight)
                        fillPath.lineTo(coordinates[0].x, coordinates[0].y)

                        for (i in 1 until coordinates.size) {
                            val prev = coordinates[i - 1]
                            val curr = coordinates[i]
                            val controlPoint1 = Offset(prev.x + (curr.x - prev.x) / 2f, prev.y)
                            val controlPoint2 = Offset(prev.x + (curr.x - prev.x) / 2f, curr.y)
                            linePath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, curr.x, curr.y)
                            fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, curr.x, curr.y)
                        }

                        fillPath.lineTo(coordinates.last().x, canvasHeight)
                        fillPath.close()

                        // 1. Draw smooth gradient fill under the curve
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0.02f)),
                                startY = 0f,
                                endY = canvasHeight
                            )
                        )

                        // 2. Draw curve stroke
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // 3. Draw dots on data points
                        coordinates.forEachIndexed { index, offset ->
                            val isSelected = selectedIndex == index
                            val hasData = dailyData[index].amount > 0

                            if (isSelected) {
                                drawCircle(
                                    color = lineColor.copy(alpha = 0.3f),
                                    radius = 10.dp.toPx(),
                                    center = offset
                                )
                                drawCircle(
                                    color = lineColor,
                                    radius = 5.dp.toPx(),
                                    center = offset
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.5.dp.toPx(),
                                    center = offset
                                )
                            } else if (hasData) {
                                drawCircle(
                                    color = lineColor,
                                    radius = 3.5.dp.toPx(),
                                    center = offset
                                )
                            }
                        }
                    }
                }

                // Days axis labels
                Row(
                    modifier = Modifier
                        .width(maxOf(320.dp, canvasContentWidth))
                        .align(Alignment.BottomStart)
                        .padding(top = 145.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val stepDisplay = if (dailyData.size > 15) 3 else 1
                    dailyData.forEachIndexed { index, point ->
                        if (index % stepDisplay == 0 || index == dailyData.lastIndex) {
                            Text(
                                text = "${point.dayNumber}",
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                color = if (selectedIndex == index) lineColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. BAR CHART (DAILY OR ACTIVITY BARS)
// ==========================================
@Composable
fun DailySpendingBarChart(
    dailyData: List<DailySpendPoint>,
    currencySymbol: String,
    title: String = "Daily Activity Bar Chart",
    barColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (dailyData.isEmpty() || dailyData.all { it.amount <= 0.0 }) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No daily spending recorded yet",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Add transactions to view daily activity bars.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    var selectedPoint by remember { mutableStateOf<DailySpendPoint?>(null) }
    val maxSpend = remember(dailyData) {
        val maxVal = dailyData.maxOfOrNull { it.amount } ?: 0.0
        max(maxVal, 100.0)
    }

    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = barColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = selectedPoint?.let { "${it.dayName}: ${CurrencyFormatter.format(it.amount, currencySymbol)}" }
                        ?: "Tap bar to inspect",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedPoint != null) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Bar Canvas
            val scrollState = rememberScrollState()
            val totalContentWidth = (dailyData.size * 22 + 32).dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .horizontalScroll(scrollState)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(maxOf(320.dp, totalContentWidth))
                        .height(160.dp)
                        .pointerInput(dailyData) {
                            detectTapGestures { offset ->
                                val step = size.width / dailyData.size.toFloat()
                                val index = (offset.x / step).toInt().coerceIn(0, dailyData.lastIndex)
                                selectedPoint = dailyData[index]
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height - 30.dp.toPx()
                    val count = dailyData.size
                    val slotWidth = canvasWidth / count.toFloat()
                    val actualBarWidth = (slotWidth * 0.6f).coerceIn(8.dp.toPx(), 22.dp.toPx())

                    // Baseline
                    drawLine(
                        color = outlineVariant,
                        start = Offset(0f, canvasHeight),
                        end = Offset(canvasWidth, canvasHeight),
                        strokeWidth = 1.dp.toPx()
                    )

                    dailyData.forEachIndexed { index, point ->
                        val barHeight = if (maxSpend > 0) {
                            ((point.amount / maxSpend) * (canvasHeight - 20.dp.toPx())).toFloat()
                        } else 0f

                        val centerX = (index * slotWidth) + (slotWidth / 2f)
                        val barLeft = centerX - (actualBarWidth / 2f)
                        val barTop = canvasHeight - maxOf(barHeight, if (point.amount > 0) 5.dp.toPx() else 1.dp.toPx())

                        val isSelected = selectedPoint?.dateIso == point.dateIso
                        val isHighest = point.amount == maxSpend && point.amount > 0

                        val barBrush = if (isSelected) {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                            )
                        } else if (isHighest) {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                            )
                        } else if (point.amount > 0) {
                            Brush.verticalGradient(
                                colors = listOf(barColor, barColor.copy(alpha = 0.75f))
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(surfaceVariant, surfaceVariant)
                            )
                        }

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(barLeft, barTop),
                            size = Size(actualBarWidth, canvasHeight - barTop),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }

                // Day labels row
                Row(
                    modifier = Modifier
                        .width(maxOf(320.dp, totalContentWidth))
                        .align(Alignment.BottomStart)
                        .padding(top = 135.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val stepDisplay = if (dailyData.size > 15) 3 else 1
                    dailyData.forEachIndexed { index, point ->
                        if (index % stepDisplay == 0 || index == dailyData.lastIndex) {
                            Text(
                                text = "${point.dayNumber}",
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                color = if (selectedPoint?.dateIso == point.dateIso) {
                                    barColor
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = if (selectedPoint?.dateIso == point.dateIso) FontWeight.Bold else FontWeight.Normal
                            )
                        } else {
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. CATEGORY DISTRIBUTION BAR
// ==========================================
@Composable
fun CategoryDistributionBar(
    summaries: List<CategorySummary>,
    modifier: Modifier = Modifier
) {
    if (summaries.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(5.dp))
        ) {
            summaries.take(6).forEach { summary ->
                val weight = (summary.percentageOfTotal / 100.0).toFloat().coerceIn(0.01f, 1f)
                val catColor = CategoryIconHelper.parseColor(summary.category.colorHex)
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .height(10.dp)
                        .background(catColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            summaries.take(5).forEach { summary ->
                val catColor = CategoryIconHelper.parseColor(summary.category.colorHex)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(catColor, CircleShape)
                    )
                    Text(
                        text = "${summary.category.name} (${String.format("%.1f", summary.percentageOfTotal)}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
