package com.test.citychallenge.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.test.citychallenge.data.local.dao.CityDao
import com.test.citychallenge.data.local.database.CityDatabase
import com.test.citychallenge.data.local.model.CityEntity
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalDataSourceImplTest {

    private lateinit var database: CityDatabase
    private lateinit var dao: CityDao
    private lateinit var context: Context
    private lateinit var localDataSource: LocalDataSource
    private lateinit var selectedCityStore: SelectedCityStore

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
        context = mockk(relaxed = true)
        selectedCityStore = spyk(SelectedCityStore(context))
        localDataSource = LocalDataSourceImpl(dao, selectedCityStore)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testIsEmpty_returnsTrueWhenNoCitiesInserted() = runBlocking {
        // The database should be empty initially.
        assertTrue(localDataSource.isEmpty())
    }

    @Test
    fun testInsertCities_andIsEmptyReturnsFalse() = runBlocking {
        // Insert a sample city.
        val city = CityEntity(
            id = 1,
            name = "TestCity",
            country = "TC",
            lat = 0.0,
            lon = 0.0,
            isFavorite = false
        )
        localDataSource.insertCities(listOf(city))
        // After insertion, the database should no longer be empty.
        assertFalse(localDataSource.isEmpty())
    }

    @Test
    fun testSearchCities_returnsCorrectData() = runBlocking {
        // Insert multiple sample cities.
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
                name = "Alaska",
                country = "AA",
                lat = 0.0,
                lon = 0.0,
                isFavorite = false
            )
        )
        localDataSource.insertCities(cities)

        // Search for cities starting with "Al" (case-insensitive).
        val results = localDataSource.searchCities("Al", onlyFavorites = false).first()
        // Expecting "Alpha" and "Alaska" (2 results).
        assertEquals(2, results.size)
        assertTrue(results.any { it.name == "Alpha" })
        assertTrue(results.any { it.name == "Alaska" })
    }

    @Test
    fun testToggleFavorite_updatesFavoriteStatus() = runBlocking {
        // Insert a city with isFavorite initially false.
        val city = CityEntity(
            id = 1,
            name = "TestCity",
            country = "TC",
            lat = 0.0,
            lon = 0.0,
            isFavorite = false
        )
        localDataSource.insertCities(listOf(city))

        // Toggle favorite status.
        localDataSource.toggleFavorite(cityId = 1L)

        // Retrieve the city to verify that the favorite status has been toggled.
        val updatedCity = localDataSource.searchCities("Test", onlyFavorites = false)
            .first()
            .first()

        assertTrue(updatedCity.isFavorite)
    }
}
