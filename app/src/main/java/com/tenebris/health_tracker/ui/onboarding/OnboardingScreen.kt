package com.tenebris.health_tracker.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tenebris.health_tracker.ui.components.DotMatrixHeader
import com.tenebris.health_tracker.ui.components.NothingCard
import com.tenebris.health_tracker.ui.theme.HealthTrackerTheme
import java.util.Locale

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.state.collectAsState()
    OnboardingContent(
        state = state,
        onUpdateGender = { viewModel.updateGender(it) },
        onUpdateAge = { viewModel.updateAge(it) },
        onUpdateHeight = { viewModel.updateHeight(it) },
        onUpdateWeight = { viewModel.updateWeight(it) },
        onUpdateActivity = { viewModel.updateActivityLevel(it) },
        onUpdateGoal = { viewModel.updateGoal(it) },
        onUpdateOffset = { viewModel.updateOffset(it) },
        onComplete = { viewModel.completeOnboarding() }
    )
}

@Composable
fun OnboardingContent(
    state: OnboardingState,
    onUpdateGender: (String) -> Unit,
    onUpdateAge: (String) -> Unit,
    onUpdateHeight: (String) -> Unit,
    onUpdateWeight: (String) -> Unit,
    onUpdateActivity: (Float) -> Unit,
    onUpdateGoal: (String) -> Unit,
    onUpdateOffset: (Int) -> Unit,
    onComplete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            DotMatrixHeader(text = "Welcome")
            Text("Let's calculate your daily needs.", style = MaterialTheme.typography.bodyLarge)

            NothingCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Gender", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.gender == "Male",
                            onClick = { onUpdateGender("Male") },
                            label = { Text("Male") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = state.gender == "Female",
                            onClick = { onUpdateGender("Female") },
                            label = { Text("Female") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    OutlinedTextField(
                        value = state.age,
                        onValueChange = onUpdateAge,
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                    )
                    OutlinedTextField(
                        value = state.height,
                        onValueChange = onUpdateHeight,
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                    )
                    OutlinedTextField(
                        value = state.weight,
                        onValueChange = onUpdateWeight,
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Activity level: ${String.format(Locale.getDefault(), "%.1f", state.activityLevel)}", style = MaterialTheme.typography.labelLarge)
                    Slider(
                        value = state.activityLevel,
                        onValueChange = onUpdateActivity,
                        valueRange = 1.0f..2.0f,
                        steps = 9, // 1.0, 1.1, ..., 2.0 (10 intervals)
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.tertiary,
                            activeTrackColor = MaterialTheme.colorScheme.tertiary
                        )
                    )
                }
            }

            NothingCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Goal", style = MaterialTheme.typography.labelLarge)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Lose", "Maintain", "Gain").forEach { goal ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                RadioButton(
                                    selected = state.goal == goal, 
                                    onClick = { onUpdateGoal(goal) },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.tertiary)
                                )
                                Text(goal)
                            }
                        }
                    }

                    if (state.goal != "Maintain") {
                        Text("Offset: ${state.offset} kcal", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = state.offset.toFloat(),
                            onValueChange = { onUpdateOffset(it.toInt()) },
                            valueRange = 0f..1000f,
                            steps = 0, // Continuous
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.tertiary,
                                activeTrackColor = MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }
                }
            }

            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Start tracking", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun OnboardingPreview() {
    HealthTrackerTheme {
        OnboardingContent(
            state = OnboardingState(
                age = "25",
                height = "180",
                weight = "80",
                goal = "Lose",
                offset = 500
            ),
            onUpdateGender = {},
            onUpdateAge = {},
            onUpdateHeight = {},
            onUpdateWeight = {},
            onUpdateActivity = {},
            onUpdateGoal = {},
            onUpdateOffset = {},
            onComplete = {}
        )
    }
}
