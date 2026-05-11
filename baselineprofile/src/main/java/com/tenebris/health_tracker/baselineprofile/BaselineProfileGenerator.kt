package com.tenebris.health_tracker.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.tenebris.health_tracker",
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()

        // Interact with "Add Food" button
        device.findObject(By.desc("Add food"))?.click()
        
        // Wait for sheet
        device.waitForIdle()
        
        // Scroll the food list if visible
        val foodList = device.findObject(By.res("food_list"))
        foodList?.scroll(Direction.DOWN, 0.5f)
    }
}
