package com.tenebris.health_tracker.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenebris.health_tracker.data.model.FoodEntry
import com.tenebris.health_tracker.ui.coach.CoachUiState
import com.tenebris.health_tracker.ui.coach.CoachViewModel
import com.tenebris.health_tracker.ui.components.*
import com.tenebris.health_tracker.ui.scanner.BarcodeScannerView
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    coachViewModel: CoachViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scannerState by viewModel.scannerState.collectAsStateWithLifecycle()
    val coachState by coachViewModel.state.collectAsStateWithLifecycle()

    var isReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        isReady = true
    }

    if (!isReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            WavyLoader()
        }
    } else {
        DashboardContent(
            state = state,
            scannerState = scannerState,
            coachState = coachState,
            onDateSelected = viewModel::selectDate,
            onDeleteFood = viewModel::deleteFood,
            onAddFood = viewModel::addFood,
            onBarcodeScanned = viewModel::onBarcodeScanned,
            onFoodImageCaptured = viewModel::onFoodImageCaptured,
            onResetScanner = viewModel::resetScanner,
            onDismissCoach = coachViewModel::dismiss,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardContent(
    state: DashboardState,
    scannerState: ScannerState,
    coachState: CoachUiState = CoachUiState(),
    onDateSelected: (java.time.LocalDate) -> Unit,
    onDeleteFood: (FoodEntry) -> Unit,
    onAddFood: (String, Int, Int, Int, Int, Int) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    onFoodImageCaptured: (Bitmap) -> Unit,
    onResetScanner: () -> Unit,
    onDismissCoach: () -> Unit = {},
) {
    var showSheet by remember { mutableStateOf(false) }
    var sheetType by remember { mutableStateOf<SheetType>(SheetType.Choice) }
    val sheetState = rememberModalBottomSheetState()

    var fabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd,
            ) {
                FloatingActionButtonMenu(
                    expanded = fabExpanded,
                    button = {
                        val tertiaryColor = MaterialTheme.colorScheme.tertiary
                        val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer

                        ToggleFloatingActionButton(
                            checked = fabExpanded,
                            onCheckedChange = { fabExpanded = it },
                            containerColor = { progress ->
                                androidx.compose.ui.graphics
                                    .lerp(tertiaryColor, primaryContainerColor, progress)
                            },
                        ) {
                            // Access checkedProgress through ToggleFloatingActionButtonScope
                            val rotation = checkedProgress * 45f
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add",
                                modifier = Modifier.graphicsLayer { rotationZ = rotation },
                                tint = if (fabExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiary,
                            )
                        }
                    },
                ) {
                    FloatingActionButtonMenuItem(
                        onClick = {
                            fabExpanded = false
                            sheetType = SheetType.PreviouslyAdded
                            showSheet = true
                        },
                        icon = { Icon(Icons.Default.Add, "Recent") },
                        text = { Text("Recent") },
                    )
                    FloatingActionButtonMenuItem(
                        onClick = {
                            fabExpanded = false
                            sheetType = SheetType.AddFood
                            showSheet = true
                        },
                        icon = { Icon(Icons.Default.Add, "New") },
                        text = { Text("New food") },
                    )
                }
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
                text = "Dashboard",
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            DatePickerTimeline(
                selectedDate = state.selectedDate,
                onDateSelected = onDateSelected,
                modifier = Modifier.padding(bottom = 32.dp),
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TachometerGauge(
                    caloriesProgress = if (state.targetCalories > 0) state.totalCalories.toFloat() / state.targetCalories else 0f,
                    proteinProgress = if (state.targetProtein > 0) state.totalProtein.toFloat() / state.targetProtein else 0f,
                    currentCalories = state.totalCalories,
                    targetCalories = state.targetCalories,
                )
            }

            if (coachState.apiKeyInvalid) {
                ExpressiveCard(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(10.dp)
                                    .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(50)),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "API KEY INVALID",
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp,
                                    ),
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                            Text(
                                text = "Update your Gemini API key in Settings to reactivate the Invisible Coach.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
            }

            CoachCard(
                headline = coachState.headline ?: "",
                body = coachState.body ?: "",
                visible = coachState.visible,
                onDismiss = onDismissCoach,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Food log",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.secondary,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("food_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.entries, key = { it.id }) { entry ->
                    SwipeToDeleteFoodCard(entry = entry, onDelete = { onDeleteFood(entry) })
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSheet = false
                    onResetScanner()
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp),
                dragHandle = null,
            ) {
                AnimatedContent(
                    targetState = sheetType,
                    label = "sheetTypeTransition",
                ) { type ->
                    when (type) {
                        SheetType.PreviouslyAdded -> {
                            PreviouslyAddedSheetContent(
                                recentEntries = state.recentEntries,
                                onAdd = { entry ->
                                    onAddFood(entry.name, entry.calories, entry.protein, entry.fat, entry.carbohydrates, entry.fiber)
                                    showSheet = false
                                },
                            )
                        }
                        SheetType.AddFood -> {
                            AddFoodSheetContent(
                                scannerState = scannerState,
                                onAdd = { name, kcal, prot, fat, carb, fib ->
                                    onAddFood(name, kcal, prot, fat, carb, fib)
                                    showSheet = false
                                },
                                onBarcodeScanned = onBarcodeScanned,
                                onFoodImageCaptured = onFoodImageCaptured,
                                onResetScanner = onResetScanner,
                            )
                        }
                        else -> {
                            // Choice is handled by the FAB Menu directly now
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteFoodCard(
    entry: FoodEntry,
    onDelete: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val swipeState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.EndToStart) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                    true
                } else {
                    false
                }
            },
            positionalThreshold = { it * 0.4f },
        )

    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color =
                if (swipeState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    Color.Transparent
                }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(color, MaterialTheme.shapes.large)
                        .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        content = {
            FoodCard(entry = entry, onDelete = onDelete)
        },
    )
}

@Composable
fun FoodCard(
    entry: FoodEntry,
    onDelete: () -> Unit,
) {
    ElevatedCard(
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${entry.calories} kcal • P: ${entry.protein}g • F: ${entry.fat}g • C: ${entry.carbohydrates}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

enum class SheetType {
    Choice,
    PreviouslyAdded,
    AddFood,
}

@Composable
fun PreviouslyAddedSheetContent(
    recentEntries: List<FoodEntry>,
    onAdd: (FoodEntry) -> Unit,
) {
    Column(
        modifier = Modifier.padding(24.dp).fillMaxWidth().padding(bottom = 32.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Previously added",
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
        )

        if (recentEntries.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No previous entries", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(recentEntries) { entry ->
                    ElevatedCard(
                        shape = MaterialTheme.shapes.medium,
                        colors =
                            CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${entry.calories} kcal • P: ${entry.protein}g • F: ${entry.fat}g • C: ${entry.carbohydrates}g",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            FilledIconButton(
                                onClick = { onAdd(entry) },
                                modifier = Modifier.size(36.dp),
                                colors =
                                    IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                    ),
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodSheetContent(
    scannerState: ScannerState,
    onAdd: (String, Int, Int, Int, Int, Int) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    onFoodImageCaptured: (Bitmap) -> Unit,
    onResetScanner: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var kcalInput by remember { mutableStateOf("") }
    var proteinInput by remember { mutableStateOf("") }
    var fatInput by remember { mutableStateOf("") }
    var carbInput by remember { mutableStateOf("") }
    var fiberInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("100") }

    var showMacros by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val barcodePermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                showScanner = true
            }
        }

    val visionCameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicturePreview(),
        ) { bitmap ->
            if (bitmap != null) {
                onFoodImageCaptured(bitmap)
            }
        }

    val visionPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                visionCameraLauncher.launch(null)
            }
        }

    var baseCalories by remember { mutableStateOf(0) }
    var baseProtein by remember { mutableStateOf(0) }
    var baseFat by remember { mutableStateOf(0) }
    var baseCarbs by remember { mutableStateOf(0) }
    var baseFiber by remember { mutableStateOf(0) }

    LaunchedEffect(scannerState) {
        if (scannerState is ScannerState.Success) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            name = scannerState.name
            baseCalories = scannerState.calories100g
            baseProtein = scannerState.protein100g
            baseFat = scannerState.fat100g
            baseCarbs = scannerState.carbohydrates100g
            baseFiber = scannerState.fiber100g
            weightInput = scannerState.estimatedWeightGrams.toString()
            showMacros = true // Automatically show macros when data is fetched from scanner
            showScanner = false
        }
    }

    LaunchedEffect(weightInput, baseCalories, baseProtein, baseFat, baseCarbs, baseFiber) {
        val weight = weightInput.toFloatOrNull() ?: 100f
        val multiplier = weight / 100f
        kcalInput = (baseCalories * multiplier).toInt().toString()
        proteinInput = (baseProtein * multiplier).toInt().toString()
        fatInput = (baseFat * multiplier).toInt().toString()
        carbInput = (baseCarbs * multiplier).toInt().toString()
        fiberInput = (baseFiber * multiplier).toInt().toString()
    }

    val isAddEnabled by remember {
        derivedStateOf { name.isNotEmpty() && kcalInput.isNotEmpty() && proteinInput.isNotEmpty() }
    }

    Column(
        modifier =
            Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 16.dp)
                .animateContentSize(animationSpec = tween(500)),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Add food",
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
            )

            Row {
                IconButton(onClick = {
                    onResetScanner()
                    val permission = Manifest.permission.CAMERA
                    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                        visionCameraLauncher.launch(null)
                    } else {
                        visionPermissionLauncher.launch(permission)
                    }
                }) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "AI Vision",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                IconButton(onClick = {
                    if (showScanner) {
                        showScanner = false
                    } else {
                        onResetScanner() // Clear previous results before opening scanner
                        val permission = Manifest.permission.CAMERA
                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                            showScanner = true
                        } else {
                            barcodePermissionLauncher.launch(permission)
                        }
                    }
                }) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "Scan barcode",
                        tint = if (showScanner) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        AnimatedContent(
            targetState = showScanner,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
            },
            label = "scannerTransition",
        ) { targetShowScanner ->
            if (targetShowScanner) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BarcodeScannerView(
                            onBarcodeScanned = onBarcodeScanned,
                            modifier = Modifier.padding(16.dp),
                        )

                        if (scannerState is ScannerState.Loading) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    WavyLoader()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Identifying...", color = Color.White, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showScanner = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                    ) {
                        Text("Cancel Scan", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AnimatedContent(
                        targetState = scannerState,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "scannerStateTransition",
                    ) { state ->
                        when (state) {
                            is ScannerState.Loading -> {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp)
                                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.large),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        WavyLoader()
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            "Analyzing food...",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                            is ScannerState.Error -> {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            else -> {}
                        }
                    }

                    ExpressiveTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Name",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ExpressiveTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = "Weight (g)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                        ExpressiveTextField(
                            value = kcalInput,
                            onValueChange = { kcalInput = it },
                            label = "kcal",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                        ExpressiveTextField(
                            value = proteinInput,
                            onValueChange = { proteinInput = it },
                            label = "Prot (g)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    AnimatedContent(
                        targetState = showMacros,
                        label = "macrosTransition",
                    ) { expanded ->
                        if (expanded) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ExpressiveTextField(
                                    value = fatInput,
                                    onValueChange = { fatInput = it },
                                    label = "Fat (g)",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                )
                                ExpressiveTextField(
                                    value = carbInput,
                                    onValueChange = { carbInput = it },
                                    label = "Carb (g)",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                )
                                ExpressiveTextField(
                                    value = fiberInput,
                                    onValueChange = { fiberInput = it },
                                    label = "Fib (g)",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { showMacros = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = CircleShape,
                                colors =
                                    ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.tertiary,
                                    ),
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add more macros (Fat, Carbs, Fiber)")
                            }
                        }
                    }

                    ExpressiveButton(
                        onClick = {
                            val k = kcalInput.toIntOrNull() ?: 0
                            val p = proteinInput.toIntOrNull() ?: 0
                            val f = fatInput.toIntOrNull() ?: 0
                            val c = carbInput.toIntOrNull() ?: 0
                            val fib = fiberInput.toIntOrNull() ?: 0
                            onAdd(name, k, p, f, c, fib)
                        },
                        enabled = isAddEnabled,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                    ) {
                        Text(
                            "Add to log",
                            color = if (isAddEnabled) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
