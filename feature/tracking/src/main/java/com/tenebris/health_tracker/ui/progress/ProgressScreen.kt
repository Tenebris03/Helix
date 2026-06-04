package com.tenebris.health_tracker.ui.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tenebris.health_tracker.data.model.WeightEntry
import com.tenebris.health_tracker.ui.components.ExpressiveCard
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: ProgressViewModel) {
    val state by viewModel.state.collectAsState()
    val editingEntry by viewModel.editingEntry.collectAsState()
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
        val filterOptions = remember { WeightRange.entries.toTypedArray() }
        var selectedRange by remember { mutableStateOf(WeightRange.ALL) }
        val cutoffDate = remember(selectedRange) {
            selectedRange.let { range ->
                if (range == WeightRange.ALL) null else LocalDate.now().minus(range.period)
            }
        }
        val filteredEntries = remember(state.weightEntries, cutoffDate) {
            if (cutoffDate == null) state.weightEntries
            else state.weightEntries.filter { LocalDate.parse(it.date).isAfter(cutoffDate) }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.38f)) {
                if (state.weightEntries.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No weight data yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    WeightGraph(entries = filteredEntries, targetWeight = state.targetWeight, modifier = Modifier.fillMaxSize())
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                filterOptions.forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { selectedRange = range },
                        label = {
                            Text(
                                range.label,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Weight history", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
            ) {
                items(state.weightEntries.reversed()) { entry ->
                    WeightHistoryCard(entry = entry, onDelete = { viewModel.deleteWeight(entry) }, onClick = { viewModel.startEdit(entry) })
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

    if (editingEntry != null) {
        val editEntry = editingEntry!!
        val editSheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = viewModel::cancelEdit,
            sheetState = editSheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp),
            dragHandle = null,
        ) {
            var weight by remember(editEntry) { mutableStateOf(editEntry.weight.toString()) }
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth().padding(bottom = 32.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "Edit weight",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = viewModel::cancelEdit,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Button(
                        onClick = {
                            weight.toFloatOrNull()?.let { viewModel.saveEdit(it) }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    ) {
                        Text("Save", color = MaterialTheme.colorScheme.onTertiary)
                    }
                }
            }
        }
    }
}

@Composable
fun WeightGraph(
    entries: List<WeightEntry>,
    targetWeight: Float = 70f,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return

    val weights = entries.map { it.weight }
    val maxWeight = maxOf(weights.maxOrNull() ?: 100f, targetWeight + 3f)
    val minWeight = minOf(weights.minOrNull() ?: 0f, targetWeight - 3f)
    val baseline = (minWeight - 3f).coerceAtLeast(0f)
    val topPadding = maxWeight + 3f
    val finalRange = (topPadding - baseline).coerceAtLeast(1f)

    var selectedIndex by remember { mutableIntStateOf(-1) }
    var graphWidth by remember { mutableFloatStateOf(0f) }
    var graphHeight by remember { mutableFloatStateOf(0f) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val density = LocalDensity.current

    // Round weight range to nearest 2kg for grid labels
    val gridMin = (baseline / 2f).toInt() * 2
    val gridMax = (topPadding / 2f).toInt() * 2 + 2
    val gridStep = maxOf((gridMax - gridMin) / 4, 2)

    Box(modifier = modifier.fillMaxWidth()) {
        Spacer(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                    .onSizeChanged { size ->
                        graphWidth = size.width.toFloat()
                        graphHeight = size.height.toFloat()
                    }
                    .pointerInput(entries) {
                        detectTapGestures { offset ->
                            val spacing =
                                if (entries.size > 1) graphWidth / (entries.size - 1)
                                else graphWidth / 2
                            val nearestIndex: Int? =
                                entries.indices.minByOrNull { i: Int ->
                                    val dotX = i * spacing
                                    abs(offset.x - dotX)
                                }
                            val thresholdPx = with(density) { 30.dp.toPx() }
                            selectedIndex =
                                if (nearestIndex != null && abs(offset.x - nearestIndex * spacing) < thresholdPx) nearestIndex
                                else -1
                        }
                    }
                    .drawWithCache {
                        onDrawBehind {
                            val canvasW = size.width
                            val canvasH = size.height
                            val spacing =
                                if (entries.size > 1) canvasW / (entries.size - 1)
                                else canvasW / 2

                            val points =
                                entries.mapIndexed { index, entry ->
                                    val x = index * spacing
                                    val y = canvasH - ((entry.weight - baseline) / finalRange * canvasH)
                                    Offset(x, y)
                                }

                            val strokeWidth2dp = 2.dp.toPx()
                            val pointRadius = 4.dp.toPx()
                            val selectedRadius = 7.dp.toPx()
                            val dashLen = 6.dp.toPx()
                            val gapLen = 3.dp.toPx()

                            // ── Horizontal grid lines ──
                            var gridValue = gridMin
                            while (gridValue <= gridMax) {
                                val gy = canvasH - ((gridValue - baseline) / finalRange * canvasH)
                                if (gy in 0f..canvasH) {
                                    var gx = 0f
                                    while (gx < canvasW) {
                                        drawLine(
                                            color = onSurfaceColor.copy(alpha = 0.08f),
                                            start = Offset(gx, gy),
                                            end = Offset((gx + dashLen).coerceAtMost(canvasW), gy),
                                            strokeWidth = 1.dp.toPx(),
                                        )
                                        gx += dashLen + gapLen
                                    }
                                }
                                gridValue += gridStep
                            }

                            // ── Target weight dashed line ──
                            val targetY = canvasH - ((targetWeight - baseline) / finalRange * canvasH)
                            if (targetY in 0f..canvasH) {
                                var tx = 0f
                                while (tx < canvasW) {
                                    drawLine(
                                        color = errorColor.copy(alpha = 0.4f),
                                        start = Offset(tx, targetY),
                                        end = Offset((tx + dashLen).coerceAtMost(canvasW), targetY),
                                        strokeWidth = strokeWidth2dp,
                                    )
                                    tx += dashLen + gapLen
                                }
                            }

                            // ── Vertical indicator line for selected point ──
                            if (selectedIndex in points.indices) {
                                val sel = points[selectedIndex]
                                drawLine(
                                    color = tertiaryColor.copy(alpha = 0.15f),
                                    start = Offset(sel.x, sel.y),
                                    end = Offset(sel.x, canvasH),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                        floatArrayOf(dashLen, gapLen), 0f,
                                    ),
                                )
                            }

                            // ── Build smooth cubic bezier path ──
                            val linePath = Path()
                            if (points.isNotEmpty()) {
                                linePath.moveTo(points[0].x, points[0].y)
                                if (points.size >= 2) {
                                    for (i in 0 until points.size - 1) {
                                        val p1 = points[i]
                                        val p2 = points[i + 1]
                                        val prev = if (i == 0) p1 else points[i - 1]
                                        val next = if (i + 2 < points.size) points[i + 2] else p2
                                        val cp1 = Offset(
                                            p1.x + (p2.x - prev.x) / 6f,
                                            p1.y + (p2.y - prev.y) / 6f,
                                        )
                                        val cp2 = Offset(
                                            p2.x - (next.x - p1.x) / 6f,
                                            p2.y - (next.y - p1.y) / 6f,
                                        )
                                        linePath.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y)
                                    }
                                }
                            }

                            // ── Gradient area fill under the curve ──
                            if (points.size >= 2) {
                                val fillPath = Path().apply {
                                    addPath(linePath)
                                    lineTo(points.last().x, canvasH)
                                    lineTo(points.first().x, canvasH)
                                    close()
                                }
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.35f),
                                            primaryColor.copy(alpha = 0f),
                                        ),
                                        startY = 0f,
                                        endY = canvasH,
                                    ),
                                )
                            }

                            // ── Draw the line over the fill ──
                            drawPath(path = linePath, color = primaryColor, style = Stroke(width = strokeWidth2dp))

                            // ── Data point dots ──
                            points.forEachIndexed { index, point ->
                                if (index == selectedIndex) {
                                    drawCircle(color = tertiaryColor.copy(alpha = 0.2f), radius = selectedRadius + 4.dp.toPx(), center = point)
                                }
                                drawCircle(
                                    color = if (index == selectedIndex) tertiaryColor else primaryColor,
                                    radius = if (index == selectedIndex) selectedRadius else pointRadius,
                                    center = point,
                                )
                            }
                        }
                    },
        )

        // ── Value pill tooltip ──
        if (selectedIndex in entries.indices) {
            val entry = entries[selectedIndex]
            val spacingPx = if (entries.size > 1) graphWidth / (entries.size - 1) else graphWidth / 2
            val offsetDp = with(density) {
                val dotX = selectedIndex * spacingPx + 16.dp.toPx()
                val dotY = graphHeight - ((entry.weight - baseline) / finalRange * graphHeight) + 4.dp.toPx()
                val tx = (dotX - 70.dp.toPx()).coerceAtLeast(0f).toInt()
                val ty = (dotY - 68.dp.toPx()).coerceAtLeast(0f).toInt()
                IntOffset(tx, ty)
            }

            Surface(
                shadowElevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 2.dp,
                modifier =
                    Modifier
                        .offset { offsetDp }
                        .widthIn(min = 110.dp),
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text(
                        text = "${entry.weight} kg",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = LocalDate.parse(entry.date).format(dateFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun WeightHistoryCard(
    entry: WeightEntry,
    onDelete: () -> Unit,
    onClick: () -> Unit = {},
) {
    val date = LocalDate.parse(entry.date)
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    ExpressiveCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
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

enum class WeightRange(val label: String, val period: Period) {
    ALL("All", Period.ofYears(9999)),
    YEAR("1Y", Period.ofYears(1)),
    MONTHS_6("6M", Period.ofMonths(6)),
    MONTHS_3("3M", Period.ofMonths(3)),
    MONTH_1("1M", Period.ofMonths(1)),
    WEEK_1("1W", Period.ofDays(7)),
}
