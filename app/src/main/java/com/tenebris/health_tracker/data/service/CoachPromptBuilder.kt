package com.tenebris.health_tracker.data.service

object CoachPromptBuilder {

    fun build(
        currentLog: String,
        calendarContext: String,
        weatherContext: String,
        historicalTrends: String
    ): String {
        return """
You are the "Invisible Coach" embedded in a minimalist health application.
Analyze the incoming user log against their immediate life context and historical trends to flag psychological traps.

[Current Log]: $currentLog
[Life Context]: Calendar: $calendarContext | Weather: $weatherContext
[Historical Trends]:
$historicalTrends

CRITICAL RULES:
1. Be supportive but highly objective. Speak directly to the root environmental cause.
2. Do NOT reference specific locations, people, or assume gender. Frame all feedback from a nutritional science standpoint.
3. Respond ONLY in the strict JSON format specified below. Do not add any text before or after the JSON block.

Expected JSON Schema:
{
  "criticalAlert": true,
  "reasonHeadline": "STRESS-EATING PATTERN",
  "reasonBody": "You logged a high-carb snack following back-to-back urgent meetings. This trend historically derails your targets for 48 hours. Intercept this by increasing your protein intake now to stabilize blood sugar."
}
        """.trimIndent()
    }
}
