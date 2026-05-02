package com.tenebris.health_tracker.ui.dashboard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tenebris.health_tracker.ui.components.*
import com.tenebris.health_tracker.ui.scanner.BarcodeScannerView
import com.tenebris.health_tracker.ui.theme.HealthTrackerTheme
import com.tenebris.health_tracker.ui.theme.NType82
import com.tenebris.health_tracker.ui.theme.NothingRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.state.collectAsState()
    val scannerState by viewModel.scannerState.collectAsState()

    DashboardContent(
        state = state,
        scannerState = scannerState,
        onDateSelected = { viewModel.selectDate(it) },
        onDeleteFood = { viewModel.deleteFood(it) },
        onAddFood = { name, kcal, prot -> viewModel.addFood(name, kcal, prot) },
        onBarcodeScanned = { viewModel.onBarcodeScanned(it) },
        onResetScanner = { viewModel.resetScanner() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardState,
    scannerState: ScannerState,
    onDateSelected: (java.time.LocalDate) -> Unit,
    onDeleteFood: (com.tenebris.health_tracker.data.model.FoodEntry) -> Unit,
    onAddFood: (String, Int, Int) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    onResetScanner: () -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
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

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Food log",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.secondary
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.entries, key = { it.id }) { entry ->
                    FoodCard(entry = entry, onDelete = { onDeleteFood(entry) })
                }
            }
        }

        if (showSheet) {
            AddFoodBottomSheet(
                scannerState = scannerState,
                onDismiss = { 
                    showSheet = false
                    onResetScanner()
                },
                onAdd = { name, kcal, prot ->
                    onAddFood(name, kcal, prot)
                    showSheet = false
                },
                onBarcodeScanned = onBarcodeScanned,
                sheetState = sheetState
            )
        }
    }
}

@Composable
fun FoodCard(entry: com.tenebris.health_tracker.data.model.FoodEntry, onDelete: () -> Unit) {
    NothingCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = NType82,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${entry.calories} kcal • ${entry.protein}g protein",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = NType82
                    ),
                    color = MaterialTheme.colorScheme.secondary
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
fun AddFoodBottomSheet(
    scannerState: ScannerState,
    onDismiss: () -> Unit,
    onAdd: (String, Int, Int) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    sheetState: SheetState
) {
    var name by remember { mutableStateOf("") }
    var kcalInput by remember { mutableStateOf("") }
    var proteinInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("100") }
    
    var showScanner by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanner = true
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0A0A0A),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Add food",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = NType82,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                IconButton(onClick = { 
                    if (showScanner) {
                        showScanner = false
                    } else {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                }) {
                    Icon(
                        Icons.Default.QrCodeScanner, 
                        contentDescription = "Scan barcode",
                        tint = if (showScanner) NothingRed else Color.White
                    )
                }
            }

            if (showScanner) {
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
            } else {
                if (scannerState is ScannerState.Loading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        PulsingDotMatrixLoader()
                    }
                }

                if (scannerState is ScannerState.Error) {
                    Text(
                        text = scannerState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
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
                        if (name.isNotEmpty()) onAdd(name, k, p)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NothingRed)
                ) {
                    Text("Add to log", color = Color.White)
                }
            }
        }
    }
}
