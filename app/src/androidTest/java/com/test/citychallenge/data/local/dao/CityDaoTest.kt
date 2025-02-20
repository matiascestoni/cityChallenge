package com.test.citychallenge.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.test.citychallenge.data.local.database.CityDatabase
import com.test.citychallenge.data.local.model.CityEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CityDaoTest {

    private lateinit var database: CityDatabase
    private lateinit var dao: CityDao

    @Before
    fun setUp() {
        // Create an in-memory database for testing.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CityDatabase::class.java
        )
            .allowMainThreadQueries() // For testing only!
            .build()

        dao = database.cityDAO()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndGetCount() = runBlocking {
        // Initially, the database should be empty.
        assertEquals(0, dao.getCount())

        // Insert a city.
        val city = CityEntity(
            id = 1,
            name = "TestCity",
            country = "TC",
            lat = 0.0,
            lon = 0.0,
            isFavorite = false
        )
        dao.insertCities(listOf(city))

        // Verify the count increases.
        assertEquals(1, dao.getCount())
    }

    @Test
    fun testSearchCities() = runBlocking {
        // Insert multiple cities.
        val cities = listOf(
            CityEntity(
                id = 1,
                name = "Alpha",
                country = "AA",
                lat = 0.0,
                lon = 0.0,
                isFavorite = false
            ),
            CityEntity(
                id = 2,
                name = "Beta",
                country = "BB",
                lat = 0.0,
                lon = 0.0,
                isFavorite = true
            ),
            CityEntity(
                id = 3,
                name = "Alabama",
                country = "AA",
                lat = 0.0,
                lon = 0.0,
                isFavorite = false
            )
        )
        dao.insertCities(cities)

        // Search for cities starting with "Al" (case-insensitive).
        val results = dao.searchCities("Al", onlyFavorites = false).first()
        // Expecting two results: "Alpha" and "Alabama"
        assertEquals(2, results.size)
    }

    @Test
    fun testToggleFavorite() = runBlocking {
        // Insert a city with isFavorite initially false.
        val city = CityEntity(
            id = 1,
            name = "TestCity",
            country = "TC",
            lat = 0.0,
            lon = 0.0,
            isFavorite = false
        )
        dao.insertCities(listOf(city))

        // Toggle favorite status.
        dao.toggleFavorite(1)
        // Retrieve the updated city using search (adjust prefix as needed).
        val updatedCity = dao.searchCities("Test", onlyFavorites = false).first().first()
        assertTrue(updatedCity.isFavorite)
    }
}
