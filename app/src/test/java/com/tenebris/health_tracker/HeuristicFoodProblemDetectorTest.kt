package com.tenebris.health_tracker

import com.tenebris.health_tracker.data.service.HeuristicFoodProblemDetector
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeuristicFoodProblemDetectorTest {
    private val detector = HeuristicFoodProblemDetector()

    @Test
    fun lateNightHighCalorieBurger_isProblematic() =
        runTest {
            assertTrue(detector.isProblematic("burger", 650, 22))
        }

    @Test
    fun lateNightPizza_isProblematic() =
        runTest {
            assertTrue(detector.isProblematic("pizza", 800, 23))
        }

    @Test
    fun healthySaladEvening_notProblematic() =
        runTest {
            assertFalse(detector.isProblematic("grilled chicken salad", 350, 19))
        }

    @Test
    fun appleMidday_notProblematic() =
        runTest {
            assertFalse(detector.isProblematic("apple", 95, 14))
        }

    @Test
    fun highCalBurgerEarly_notProblematic() =
        runTest {
            assertFalse(detector.isProblematic("burger", 400, 14))
        }

    @Test
    fun lowCalChipsLate_notProblematic() =
        runTest {
            assertFalse(detector.isProblematic("chips", 120, 23))
        }

    @Test
    fun redFlagWithVeryHighCalories_isProblematic() =
        runTest {
            assertTrue(detector.isProblematic("chocolate bar", 550, 15))
        }

    @Test
    fun healthySnackLate_notProblematic() =
        runTest {
            assertFalse(detector.isProblematic("greek yogurt", 100, 23))
        }
}
