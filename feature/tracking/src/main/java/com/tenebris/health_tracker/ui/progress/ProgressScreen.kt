package com.tenebris.health_tracker.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tenebris.health_tracker.data.model.WeightEntry
import com.tenebris.health_tracker.ui.components.ExpressiveCard
import com.tenebris.health_tracker.ui.components.ExpressiveHeader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: ProgressViewModel) {
    val state by viewModel.state.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add weight")
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            ExpressiveHeader(
                text = "Progress",
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                if (state.weightEntries.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No weight data yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    WeightGraph(entries = state.weightEntries, modifier = Modifier.fillMaxSize())
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Weight history", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
            ) {
                items(state.weightEntries.reversed()) { entry ->
                    WeightHistoryCard(entry = entry, onDelete = { viewModel.deleteWeight(entry) })
                }
            }
        }
    }

    if (showSheet) {
        AddWeightBottomSheet(
            onDismiss = { showSheet = false },
            onAdd = {
                viewModel.addWeight(it)
                showSheet = false
            },
            sheetState = sheetState,
        )
    }
}

@Composable
fun WeightGraph(
    entries: List<WeightEntry>,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return

    val weights = entries.map { it.weight }
    val maxWeight = weights.maxOrNull() ?: 100f
    val minWeight = weights.minOrNull() ?: 0f

    val baseline = (minWeight - 5f).coerceAtLeast(0f)
    val topPadding = maxWeight + 5f
    val finalRange = (topPadding - baseline).coerceAtLeast(1f)

    val labelFormatter = DateTimeFormatter.ofPattern("MM/dd")

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Spacer(
        modifier =
            modifier
                .drawWithCache {
                    val xAxisPadding = 45.dp.toPx()
                    val yAxisPadding = 30.dp.toPx()
                    val strokeWidth1dp = 1.dp.toPx()
                    val strokeWidth2dp = 2.dp.toPx()
                    val pointRadius = 4.dp.toPx()
                    val labelTextSize = 10.sp.toPx()

                    val textPaint =
                        android.graphics.Paint().apply {
                            color = onSurfaceColor.copy(alpha = 0.6f).hashCode()
                            textSize = labelTextSize
                        }

                    onDrawBehind {
                        val width = size.width
                        val height = size.height
                        val graphWidth = width - xAxisPadding
                        val graphHeight = height - yAxisPadding

                        val mutedOnSurface = onSurfaceColor.copy(alpha = 0.3f)
                        drawLine(
                            color = mutedOnSurface,
                            start = Offset(xAxisPadding, 0f),
                            end = Offset(xAxisPadding, graphHeight),
                            strokeWidth = strokeWidth1dp,
                        )
                        drawLine(
                            color = mutedOnSurface,
                            start = Offset(xAxisPadding, graphHeight),
                            end = Offset(width, graphHeight),
                            strokeWidth = strokeWidth1dp,
                        )

                        if (entries.isEmpty()) return@onDrawBehind

                        val spacing = if (entries.size > 1) graphWidth / (entries.size - 1) else graphWidth / 2

                        val points =
                            entries.mapIndexed { index, entry ->
                                val x = xAxisPadding + (index * spacing)
                                val y = graphHeight - ((entry.weight - baseline) / finalRange * graphHeight)
                                Offset(x, y)
                            }

                        val yLabelCount = 4
                        for (i in 0..yLabelCount) {
                            val yValue = baseline + (finalRange * i / yLabelCount)
                            val yPos = graphHeight - (i.toFloat() / yLabelCount * graphHeight)
                            drawContext.canvas.nativeCanvas.drawText(
                                String.format(Locale.getDefault(), "%.0f", yValue),
                                5.dp.toPx(),
                                yPos + 5.dp.toPx(),
                                textPaint.apply { textAlign = android.graphics.Paint.Align.LEFT },
                            )
                        }

                        val path =
                            Path().apply {
                                if (points.isNotEmpty()) {
                                    moveTo(points[0].x, points[0].y)
                                    for (i in 1 until points.size) {
                                        lineTo(points[i].x, points[i].y)
                                    }
                                }
                            }

                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = strokeWidth2dp),
                        )

                        points.forEachIndexed { index, point ->
                            drawCircle(
                                color = tertiaryColor,
                                radius = pointRadius,
                                center = point,
                            )

                            if (entries.size < 7 || index % (entries.size / 4).coerceAtLeast(1) == 0) {
                                drawContext.canvas.nativeCanvas.drawText(
                                    LocalDate.parse(entries[index].date).format(labelFormatter),
                                    point.x - 15.dp.toPx(),
                                    height - 5.dp.toPx(),
                                    textPaint.apply { textAlign = android.graphics.Paint.Align.CENTER },
                                )
                            }
                        }
                    }
                },
    )
}

@Composable
fun WeightHistoryCard(
    entry: WeightEntry,
    onDelete: () -> Unit,
) {
    val date = LocalDate.parse(entry.date)
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    ExpressiveCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = date.format(formatter),
                    style = MaterialTheme.typography.labelSmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${entry.weight} kg",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWeightBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (Float) -> Unit,
    sheetState: SheetState,
) {
    var weight by remember { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp),
        dragHandle = null,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Log weight",
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            )

            Button(
                onClick = {
                    weight.toFloatOrNull()?.let { onAdd(it) }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
            ) {
                Text("Add", color = MaterialTheme.colorScheme.onTertiary)
            }
        }
    }
}
