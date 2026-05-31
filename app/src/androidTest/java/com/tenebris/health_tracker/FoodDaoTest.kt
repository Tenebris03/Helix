package com.tenebris.health_tracker

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tenebris.health_tracker.data.local.AppDatabase
import com.tenebris.health_tracker.data.local.FoodDao
import com.tenebris.health_tracker.data.model.FoodEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class FoodDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: FoodDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.foodDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndReadEntry() = runBlocking {
        val entry = FoodEntry(
            name = "Test Meal",
            calories = 500,
            protein = 25,
            date = LocalDate.now().toString()
        )
        dao.insertEntry(entry)

        val entries = dao.getEntriesByDate(LocalDate.now().toString()).first()
        assertEquals(1, entries.size)
        assertEquals("Test Meal", entries[0].name)
        assertEquals(500, entries[0].calories)
    }

    @Test
    fun deleteEntry() = runBlocking {
        val entry = FoodEntry(
            name = "Delete Me",
            calories = 200,
            protein = 10,
            date = LocalDate.now().toString()
        )
        dao.insertEntry(entry)
        assertEquals(1, dao.getEntriesByDate(LocalDate.now().toString()).first().size)

        val inserted = dao.getEntriesByDate(LocalDate.now().toString()).first()[0]
        dao.deleteEntry(inserted)
        assertEquals(0, dao.getEntriesByDate(LocalDate.now().toString()).first().size)
    }

    @Test
    fun getTotalCaloriesByDate() = runBlocking {
        val today = LocalDate.now().toString()
        dao.insertEntry(FoodEntry(name = "Meal 1", calories = 300, protein = 15, date = today))
        dao.insertEntry(FoodEntry(name = "Meal 2", calories = 400, protein = 20, date = today))

        val total = dao.getTotalCaloriesByDate(today).first()
        assertNotNull(total)
        assertEquals(700, total)
    }

    @Test
    fun getUniqueRecentEntriesGroupsByNameAndCalories() = runBlocking {
        val today = LocalDate.now().toString()
        dao.insertEntry(FoodEntry(name = "Chicken", calories = 400, protein = 30, date = today))
        dao.insertEntry(FoodEntry(name = "Chicken", calories = 400, protein = 30, date = today))
        dao.insertEntry(FoodEntry(name = "Salad", calories = 200, protein = 10, date = today))

        val unique = dao.getUniqueRecentEntries().first()
        assertEquals(2, unique.size)
    }
}
