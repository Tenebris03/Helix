package com.tenebris.health_tracker.ui.dashboard

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tenebris.health_tracker.ui.components.*
import com.tenebris.health_tracker.ui.theme.HealthTrackerTheme
import com.tenebris.health_tracker.ui.theme.NType82

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.state.collectAsState()
    DashboardContent(
        state = state,
        onDateSelected = { viewModel.selectDate(it) },
        onDeleteFood = { viewModel.deleteFood(it) },
        onAddFood = { name, kcal, prot -> viewModel.addFood(name, kcal, prot) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardState,
    onDateSelected: (java.time.LocalDate) -> Unit,
    onDeleteFood: (com.tenebris.health_tracker.data.model.FoodEntry) -> Unit,
    onAddFood: (String, Int, Int) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = MaterialTheme.colorScheme.tertiary, // Nothing Red
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp) // Avoid overlap with pill
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
                onDismiss = { showSheet = false },
                onAdd = { name, kcal, prot ->
                    onAddFood(name, kcal, prot)
                    showSheet = false
                },
                sheetState = sheetState
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun DashboardPreview() {
    HealthTrackerTheme {
        DashboardContent(
            state = DashboardState(
                entries = listOf(
                    com.tenebris.health_tracker.data.model.FoodEntry(1, "Oatmeal", 350, 12, "2024-05-02"),
                    com.tenebris.health_tracker.data.model.FoodEntry(2, "Protein shake", 200, 40, "2024-05-02")
                ),
                totalCalories = 550,
                totalProtein = 52,
                targetCalories = 2500,
                targetProtein = 180
            ),
            onDateSelected = {},
            onDeleteFood = {},
            onAddFood = { _, _, _ -> }
        )
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
    onDismiss: () -> Unit,
    onAdd: (String, Int, Int) -> Unit,
    sheetState: SheetState
) {
    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0A0A0A), // Custom background
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
            Text(
                "Add food",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = NType82,
                    fontWeight = FontWeight.Bold
                )
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = kcal,
                    onValueChange = { kcal = it },
                    label = { Text("kcal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Button(
                onClick = {
                    val k = kcal.toIntOrNull() ?: 0
                    val p = protein.toIntOrNull() ?: 0
                    if (name.isNotEmpty()) onAdd(name, k, p)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Add to log", color = Color.White)
            }
        }
    }
}
