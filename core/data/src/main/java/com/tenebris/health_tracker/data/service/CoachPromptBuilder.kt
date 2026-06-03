package com.tenebris.health_tracker.data.service

object CoachPromptBuilder {
    fun build(
        currentLog: String,
        remainingCalories: Int,
        calendarContext: String,
        weatherContext: String,
        historicalTrends: String,
    ): String {
        val calorieStatus =
            if (remainingCalories >= 0) {
                "$remainingCalories kcal remaining"
            } else {
                "${kotlin.math.abs(remainingCalories)} kcal OVER budget"
            }

        return """
You are the "Invisible Coach" embedded in a minimalist health application.
Analyze the incoming user log against their immediate life context and historical trends to flag psychological traps.

[Current Log]: $currentLog
[Calorie Context]: $calorieStatus
[Life Context]: Calendar: $calendarContext | Weather: $weatherContext
[Historical Trends]:
$historicalTrends

CRITICAL RULES:
1. Be punchy and direct. Avoid fluff.
2. Explicitly mention the specific food just logged.
3. If the user is over budget, acknowledge the "overage" simply, don't use confusing negative numbers.
4. Instead of a long explanation, suggest ONE specific "Better Next Time" meal or behavior that fits the current context (e.g., "Next time, try Greek yogurt to avoid the sugar crash from this snack").
5. Keep sentences short.
6. Respond ONLY in the strict JSON format specified below.

Expected JSON Schema:
{
  "criticalAlert": true,
  "reasonHeadline": "CONCISE HEADLINE",
  "reasonBody": "Logged [Food Name]. [Context Logic]. Next time: [Specific Better Suggestion]."
}
            """.trimIndent()
    }
}
