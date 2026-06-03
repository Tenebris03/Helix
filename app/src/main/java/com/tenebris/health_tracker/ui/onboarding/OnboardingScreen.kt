package com.tenebris.health_tracker.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tenebris.health_tracker.ui.components.*
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
        onComplete = { viewModel.completeOnboarding() },
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
    onComplete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            ExpressiveHeader(text = "Welcome")
            Text("Let's calculate your daily needs.", style = MaterialTheme.typography.bodyLarge)

            ExpressiveCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Gender", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.gender == "Male",
                            onClick = { onUpdateGender("Male") },
                            label = { Text("Male") },
                            shape = CircleShape,
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                                ),
                        )
                        FilterChip(
                            selected = state.gender == "Female",
                            onClick = { onUpdateGender("Female") },
                            label = { Text("Female") },
                            shape = CircleShape,
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                                ),
                        )
                    }

                    ExpressiveTextField(
                        value = state.age,
                        onValueChange = onUpdateAge,
                        label = "Age",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ExpressiveTextField(
                        value = state.height,
                        onValueChange = onUpdateHeight,
                        label = "Height (cm)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ExpressiveTextField(
                        value = state.weight,
                        onValueChange = onUpdateWeight,
                        label = "Weight (kg)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Activity level: ${String.format(Locale.getDefault(), "%.1f", state.activityLevel)}", style = MaterialTheme.typography.labelLarge)
                    ExpressiveSlider(
                        value = state.activityLevel,
                        onValueChange = onUpdateActivity,
                        valueRange = 1.0f..2.0f,
                        steps = 9,
                        activeColor = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            ExpressiveCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Goal", style = MaterialTheme.typography.labelLarge)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Lose", "Maintain", "Gain").forEach { goal ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = state.goal == goal,
                                    onClick = { onUpdateGoal(goal) },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.tertiary),
                                )
                                Text(goal)
                            }
                        }
                    }

                    if (state.goal != "Maintain") {
                        Text("Offset: ${state.offset} kcal", style = MaterialTheme.typography.bodyMedium)
                        ExpressiveSlider(
                            value = state.offset.toFloat(),
                            onValueChange = { onUpdateOffset(it.toInt()) },
                            valueRange = 0f..1000f,
                            activeColor = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }

            ExpressiveButton(
                onClick = onComplete,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            ) {
                Text("Start tracking", style = MaterialTheme.typography.titleMedium)
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
            state =
                OnboardingState(
                    age = "25",
                    height = "180",
                    weight = "80",
                    goal = "Lose",
                    offset = 500,
                ),
            onUpdateGender = {},
            onUpdateAge = {},
            onUpdateHeight = {},
            onUpdateWeight = {},
            onUpdateActivity = {},
            onUpdateGoal = {},
            onUpdateOffset = {},
            onComplete = {},
        )
    }
}
