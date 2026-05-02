package com.tenebris.health_tracker.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tenebris.health_tracker.ui.components.DotMatrixHeader
import com.tenebris.health_tracker.ui.components.NothingCard
import com.tenebris.health_tracker.ui.theme.NType82
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    var goal by remember(state.goal) { mutableStateOf(state.goal) }
    var offset by remember(state.offset) { mutableStateOf(state.offset.toFloat()) }
    var activity by remember(state.activityLevel) { mutableStateOf(state.activityLevel) }
    var age by remember(state.age) { mutableStateOf(state.age.toString()) }
    var height by remember(state.height) { mutableStateOf(state.height.toString()) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let {
            val outputStream = context.contentResolver.openOutputStream(it)
            viewModel.exportData(context, outputStream)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            viewModel.importData(context, inputStream)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        DotMatrixHeader(
            text = "Settings",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        NothingCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                Text(
                    "Dynamic BMR: ${state.bmr} kcal", 
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = NType82),
                    color = Color.White
                )
                Text(
                    "Based on latest weight: ${state.latestWeight} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Activity level: ${String.format(Locale.getDefault(), "%.1f", activity)}", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = activity,
                    onValueChange = { activity = it },
                    valueRange = 1.0f..2.0f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.tertiary,
                        activeTrackColor = MaterialTheme.colorScheme.tertiary
                    )
                )

                Text("Goal", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Lose", "Maintain", "Gain").forEach { g ->
                        FilterChip(
                            selected = goal == g,
                            onClick = { goal = g },
                            label = { Text(g) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                val totalTarget = when(goal) {
                    "Lose" -> (state.bmr * activity) - offset
                    "Gain" -> (state.bmr * activity) + offset
                    else -> state.bmr * activity
                }

                Text(
                    "Total daily target: ${totalTarget.toInt()} kcal", 
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = NType82
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Text(
                    "Protein target: ${state.proteinTarget}g", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text("Calorie offset: ${offset.toInt()} kcal", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = offset,
                    onValueChange = { offset = it },
                    valueRange = 0f..1000f,
                    steps = 0,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.tertiary,
                        activeTrackColor = MaterialTheme.colorScheme.tertiary
                    )
                )

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                )

                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                )

                Button(
                    onClick = {
                        viewModel.updateSettings(
                            state.bmr,
                            goal,
                            offset.toInt(),
                            state.proteinTarget,
                            activity,
                            state.gender,
                            age.toIntOrNull() ?: 25,
                            height.toIntOrNull() ?: 170
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Save changes", color = Color.White)
                }
            }
        }

        Text(
            "Backup & Restore", 
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.Gray
        )

        NothingCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "You can backup your data directly to Google Drive by selecting it as the destination in the file picker.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Button(
                    onClick = { exportLauncher.launch("health_tracker_backup.db") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("Export data", color = Color.White)
                }

                OutlinedButton(
                    onClick = { importLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                ) {
                    Text("Import data", color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}
