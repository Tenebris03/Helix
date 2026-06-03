package com.tenebris.health_tracker.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.tenebris.health_tracker.ui.components.*
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val apiKey by viewModel.apiKey.collectAsState()

    var goal by remember(state.goal) { mutableStateOf(state.goal) }
    var offset by remember(state.offset) { mutableStateOf(state.offset.toFloat()) }
    var activity by remember(state.activityLevel) { mutableStateOf(state.activityLevel) }
    var age by remember(state.age) { mutableStateOf(state.age.toString()) }
    var height by remember(state.height) { mutableStateOf(state.height.toString()) }
    var apiKeyInput by remember(apiKey) { mutableStateOf(apiKey) }

    val exportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/octet-stream"),
        ) { uri: Uri? ->
            uri?.let {
                val outputStream = context.contentResolver.openOutputStream(it)
                viewModel.exportData(context, outputStream)
            }
        }

    val importLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                viewModel.importData(context, inputStream)
            }
        }

    var hasLocationPerm by remember { mutableStateOf(false) }
    var hasCalendarPerm by remember { mutableStateOf(false) }

    fun checkPermissions() {
        hasLocationPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        hasCalendarPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
    }

    checkPermissions()

    val locationPermLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { checkPermissions() }

    val calendarPermLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { checkPermissions() }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        ExpressiveHeader(
            text = "Settings",
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExpressiveCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val currentAge = age.toIntOrNull() ?: state.age
                val currentHeight = height.toIntOrNull() ?: state.height

                val reactiveBmr =
                    if (state.gender == "Male") {
                        10 * state.latestWeight + 6.25 * currentHeight - 5 * currentAge + 5
                    } else {
                        10 * state.latestWeight + 6.25 * currentHeight - 5 * currentAge - 161
                    }

                Text(
                    "Dynamic BMR: ${reactiveBmr.toInt()} kcal",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Based on latest weight: ${state.latestWeight} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Activity level: ${String.format(Locale.getDefault(), "%.1f", activity)}", style = MaterialTheme.typography.labelLarge)
                ExpressiveSlider(
                    value = activity,
                    onValueChange = { activity = it },
                    valueRange = 1.0f..2.0f,
                    steps = 9,
                    activeColor = MaterialTheme.colorScheme.tertiary,
                )

                Text("Goal", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Lose", "Maintain", "Gain").forEach { g ->
                        FilterChip(
                            selected = goal == g,
                            onClick = { goal = g },
                            label = { Text(g) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                                ),
                        )
                    }
                }

                val totalTarget =
                    when (goal) {
                        "Lose" -> (reactiveBmr * activity) - offset
                        "Gain" -> (reactiveBmr * activity) + offset
                        else -> reactiveBmr * activity
                    }

                Text(
                    "Total daily target: ${totalTarget.toInt()} kcal",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                )

                Text(
                    "Protein target: ${state.proteinTarget}g",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text("Calorie offset: ${offset.toInt()} kcal", style = MaterialTheme.typography.bodyMedium)
                ExpressiveSlider(
                    value = offset,
                    onValueChange = { offset = it },
                    valueRange = 0f..1000f,
                    activeColor = MaterialTheme.colorScheme.tertiary,
                )

                ExpressiveTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = "Age",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )

                ExpressiveTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = "Height (cm)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )

                ExpressiveButton(
                    onClick = {
                        viewModel.updateSettings(
                            goal = goal,
                            offset = offset.toInt(),
                            activityLevel = activity,
                            gender = state.gender,
                            age = age.toIntOrNull() ?: state.age,
                            height = height.toIntOrNull() ?: state.height,
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ) {
                    Text("Save changes")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Backup & Restore",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExpressiveCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "You can backup your data directly to Google Drive by selecting it as the destination in the file picker.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                ExpressiveButton(
                    onClick = { exportLauncher.launch("health_tracker_backup.db") },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Text("Export data")
                }

                OutlinedButton(
                    onClick = { importLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                ) {
                    Text("Import data", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Invisible Coach",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExpressiveCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Enter your Gemini API key to enable the Invisible Coach. It runs entirely on-device and never leaves your phone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                ExpressiveTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = "Gemini API Key",
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpressiveButton(
                        onClick = { viewModel.updateApiKey(apiKeyInput) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                    ) {
                        Text("Save Key")
                    }

                    if (apiKey.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                apiKeyInput = ""
                                viewModel.clearApiKey()
                            },
                            modifier = Modifier.height(48.dp),
                            shape = CircleShape,
                        ) {
                            Text("Remove", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (apiKey.isNotEmpty()) {
                    Text(
                        text = "✓ Key configured",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Permissions",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExpressiveCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Optional permissions that give the Invisible Coach more context for better insights.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Location permission
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Location", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Local weather for context-aware coaching",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (hasLocationPerm) {
                        Text(text = "✓ Granted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                    } else {
                        ExpressiveButton(
                            onClick = { locationPermLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                        ) {
                            Text("Grant")
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Calendar permission
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Calendar", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Detect stress patterns from meeting density",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (hasCalendarPerm) {
                        Text(text = "✓ Granted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                    } else {
                        ExpressiveButton(
                            onClick = { calendarPermLauncher.launch(Manifest.permission.READ_CALENDAR) },
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                        ) {
                            Text("Grant")
                        }
                    }
                }
            }
        }

        if (com.tenebris.health_tracker.BuildConfig.SHOW_DEV_TOOLS) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Developer",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            ExpressiveCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Generate 60 days of mock food entries, weight entries, and profile data for testing. Resets the coach cooldown.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ExpressiveButton(
                        onClick = { viewModel.seedTestData() },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                    ) {
                        Text("Seed test data")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
