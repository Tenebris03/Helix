package com.tenebris.health_tracker.ui.dashboard

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import android.graphics.Bitmap
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenebris.health_tracker.ui.coach.CoachUiState
import com.tenebris.health_tracker.ui.coach.CoachViewModel
import com.tenebris.health_tracker.ui.components.*
import com.tenebris.health_tracker.ui.scanner.BarcodeScannerView
import com.tenebris.health_tracker.ui.theme.HealthTrackerTheme
import com.tenebris.health_tracker.ui.theme.NType82
import com.tenebris.health_tracker.ui.theme.NothingRed
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    coachViewModel: CoachViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scannerState by viewModel.scannerState.collectAsStateWithLifecycle()
    val coachState by coachViewModel.state.collectAsStateWithLifecycle()

    // Smooth transition delay for heavy UI components
    var isReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        isReady = true
    }

    if (!isReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PulsingDotMatrixLoader()
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
            onDismissCoach = coachViewModel::dismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardState,
    scannerState: ScannerState,
    coachState: CoachUiState = CoachUiState(),
    onDateSelected: (java.time.LocalDate) -> Unit,
    onDeleteFood: (com.tenebris.health_tracker.data.model.FoodEntry) -> Unit,
    onAddFood: (String, Int, Int) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    onFoodImageCaptured: (Bitmap) -> Unit,
    onResetScanner: () -> Unit,
    onDismissCoach: () -> Unit = {}
) {
    var showSheet by remember { mutableStateOf(false) }
    var sheetType by remember { mutableStateOf<SheetType>(SheetType.Choice) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    sheetType = SheetType.Choice
                    showSheet = true 
                },
                containerColor = NothingRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add food")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            DotMatrixHeader(
                text = "Dashboard",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            DatePickerTimeline(
                selectedDate = state.selectedDate,
                onDateSelected = onDateSelected,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TachometerGauge(
                    caloriesProgress = if (state.targetCalories > 0) state.totalCalories.toFloat() / state.targetCalories else 0f,
                    proteinProgress = if (state.targetProtein > 0) state.totalProtein.toFloat() / state.targetProtein else 0f,
                    currentCalories = state.totalCalories,
                    targetCalories = state.targetCalories
                )
            }

            if (coachState.apiKeyInvalid) {
                NothingCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(NothingRed)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "API KEY INVALID",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = NType82,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                color = NothingRed
                            )
                            Text(
                                text = "Update your Gemini API key in Settings to reactivate the Invisible Coach.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            CoachCard(
                headline = coachState.headline ?: "",
                body = coachState.body ?: "",
                visible = coachState.visible,
                onDismiss = onDismissCoach
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Food log",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.secondary
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("food_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.entries, key = { it.id }) { entry ->
                    FoodCard(entry = entry, onDelete = { onDeleteFood(entry) })
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
                containerColor = Color(0xFF0A0A0A),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                dragHandle = null
            ) {
                AnimatedContent(
                    targetState = sheetType,
                    label = "sheetTypeTransition"
                ) { type ->
                    when (type) {
                        SheetType.Choice -> {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth().padding(bottom = 32.dp, top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Add food",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = NType82,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Button(
                                    onClick = { sheetType = SheetType.PreviouslyAdded },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                                ) {
                                    Text("Previously added", color = Color.White)
                                }
                                Button(
                                    onClick = { sheetType = SheetType.AddFood },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NothingRed)
                                ) {
                                    Text("Add new food", color = Color.White)
                                }
                            }
                        }
                        SheetType.PreviouslyAdded -> {
                            PreviouslyAddedSheetContent(
                                recentEntries = state.recentEntries,
                                onAdd = { name, kcal, prot ->
                                    onAddFood(name, kcal, prot)
                                    showSheet = false
                                },
                                onBack = { sheetType = SheetType.Choice }
                            )
                        }
                        SheetType.AddFood -> {
                            AddFoodSheetContent(
                                scannerState = scannerState,
                                onAdd = { name, kcal, prot ->
                                    onAddFood(name, kcal, prot)
                                    showSheet = false
                                },
                                onBarcodeScanned = onBarcodeScanned,
                                onFoodImageCaptured = onFoodImageCaptured,
                                onBack = { sheetType = SheetType.Choice }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodCard(entry: com.tenebris.health_tracker.data.model.FoodEntry, onDelete: () -> Unit) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = surfaceColor,
                    cornerRadius = CornerRadius(32.dp.toPx())
                )
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${entry.calories} kcal • ${entry.protein}g protein",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}

enum class SheetType {
    Choice, PreviouslyAdded, AddFood
}

@Composable
fun PreviouslyAddedSheetContent(
    recentEntries: List<com.tenebris.health_tracker.data.model.FoodEntry>,
    onAdd: (String, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp).fillMaxWidth().padding(bottom = 32.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Previously added",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = NType82,
                    fontWeight = FontWeight.Bold
                )
            )
            TextButton(onClick = onBack) {
                Text("Back", color = NothingRed)
            }
        }

        if (recentEntries.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No previous entries", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentEntries) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.name, style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(
                                "${entry.calories} kcal • ${entry.protein}g protein",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        IconButton(
                            onClick = { onAdd(entry.name, entry.calories, entry.protein) },
                            modifier = Modifier.background(NothingRed, CircleShape).size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(20.dp))
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
    onAdd: (String, Int, Int) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    onFoodImageCaptured: (Bitmap) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var kcalInput by remember { mutableStateOf("") }
    var proteinInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("100") }
    
    var showScanner by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val barcodePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanner = true
        }
    }

    val visionCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onFoodImageCaptured(bitmap)
        }
    }

    val visionPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            visionCameraLauncher.launch(null)
        }
    }

    // Reference values for 100g
    var baseCalories by remember { mutableStateOf(0) }
    var baseProtein by remember { mutableStateOf(0) }

    LaunchedEffect(scannerState) {
        if (scannerState is ScannerState.Success) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            name = scannerState.name
            baseCalories = scannerState.calories100g
            baseProtein = scannerState.protein100g
            weightInput = "100"
            showScanner = false
        }
    }

    // Live scaling
    LaunchedEffect(weightInput, baseCalories, baseProtein) {
        val weight = weightInput.toFloatOrNull() ?: 100f
        val multiplier = weight / 100f
        kcalInput = (baseCalories * multiplier).toInt().toString()
        proteinInput = (baseProtein * multiplier).toInt().toString()
    }

    val isAddEnabled by remember {
        derivedStateOf { name.isNotEmpty() && kcalInput.isNotEmpty() && proteinInput.isNotEmpty() }
    }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .padding(bottom = 32.dp, top = 16.dp)
            .animateContentSize(animationSpec = tween(500)),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Add food",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = NType82,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onBack) {
                    Text("Back", color = NothingRed)
                }
            }

            Row {
                IconButton(onClick = {
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
                        tint = Color.White
                    )
                }

                IconButton(onClick = {
                    if (showScanner) {
                        showScanner = false
                    } else {
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
                        tint = if (showScanner) NothingRed else Color.White
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
            label = "scannerTransition"
        ) { targetShowScanner ->
            if (targetShowScanner) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color.Black, RoundedCornerShape(16.dp))
                    ) {
                        BarcodeScannerView(
                            onBarcodeScanned = onBarcodeScanned,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    Button(
                        onClick = { showScanner = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("Cancel Scan", color = Color.White)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AnimatedContent(
                        targetState = scannerState,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "scannerStateTransition"
                    ) { state ->
                        when (state) {
                            is ScannerState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PulsingDotMatrixLoader()
                                }
                            }
                            is ScannerState.Error -> {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            else -> {}
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        OutlinedTextField(
                            value = kcalInput,
                            onValueChange = { kcalInput = it },
                            label = { Text("kcal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        OutlinedTextField(
                            value = proteinInput,
                            onValueChange = { proteinInput = it },
                            label = { Text("Protein (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val k = kcalInput.toIntOrNull() ?: 0
                            val p = proteinInput.toIntOrNull() ?: 0
                            onAdd(name, k, p)
                        },
                        enabled = isAddEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NothingRed,
                            disabledContainerColor = Color.DarkGray
                        )
                    ) {
                        Text("Add to log", color = if (isAddEnabled) Color.White else Color.Gray)
                    }
                }
            }
        }
    }
}

