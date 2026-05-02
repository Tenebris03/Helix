package com.tenebris.health_tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tenebris.health_tracker.ui.theme.NType82
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DotMatrixHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontFamily = NType82
        ),
        modifier = modifier
    )
}

@Composable
fun NothingCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier,
        content = content
    )
}

@Composable
fun TachometerGauge(
    caloriesProgress: Float, // 0.0 to 1.0
    proteinProgress: Float,  // 0.0 to 1.0
    currentCalories: Int,
    targetCalories: Int,
    modifier: Modifier = Modifier
) {
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorSecondary = MaterialTheme.colorScheme.secondary
    val colorTertiary = MaterialTheme.colorScheme.tertiary // NothingRed

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 20.dp.toPx()
            val innerStrokeWidth = 12.dp.toPx()
            
            // Background arcs
            drawArc(
                color = colorSecondary.copy(alpha = 0.1f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )

            drawArc(
                color = colorSecondary.copy(alpha = 0.1f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round),
                size = Size(size.width - 60.dp.toPx(), size.height - 60.dp.toPx()),
                topLeft = Offset(30.dp.toPx(), 30.dp.toPx())
            )

            // Progress arcs
            drawArc(
                color = colorPrimary,
                startAngle = 135f,
                sweepAngle = 270f * caloriesProgress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )

            drawArc(
                color = colorTertiary,
                startAngle = 135f,
                sweepAngle = 270f * proteinProgress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round),
                size = Size(size.width - 60.dp.toPx(), size.height - 60.dp.toPx()),
                topLeft = Offset(30.dp.toPx(), 30.dp.toPx())
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$currentCalories",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = NType82,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "/ $targetCalories kcal",
                style = MaterialTheme.typography.labelLarge,
                color = colorSecondary
            )
        }
    }
}

@Composable
fun DatePickerTimeline(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val dates = ((-30..30).map { LocalDate.now().plusDays(it.toLong()) })
    val listState = rememberLazyListState()
    val formatterDay = DateTimeFormatter.ofPattern("EEE\ndd")
    val formatterMonth = DateTimeFormatter.ofPattern("MMM")

    LaunchedEffect(Unit) {
        listState.scrollToItem(30) // Center on today
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .width(70.dp)
                    .clickable { onDateSelected(date) }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = date.format(formatterMonth).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = date.format(formatterDay).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
