package com.test.citychallenge.data.local.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.test.citychallenge.data.local.dao.CityDao
import com.test.citychallenge.data.local.model.CityEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CityDatabaseTest {

    private lateinit var database: CityDatabase
    private lateinit var dao: CityDao

    @Before
    fun setUp() {
        // Create an in-memory version of the database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CityDatabase::class.java
        )
            .allowMainThreadQueries() // Only for testing purposes
            .build()

        dao = database.cityDAO()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun databaseCreation_isSuccessful() {
        // Check that the database and DAO are not null
        assertNotNull(database)
        assertNotNull(dao)
    }

    @Test
    fun insertAndQueryCity_returnsCorrectData() = runBlocking {
        // Arrange: create a CityEntity and insert it
        val city = CityEntity(
            id = 1,
            name = "TestCity",
            country = "TC",
            lat = 0.0,
            lon = 0.0,
            isFavorite = false
        )
        dao.insertCities(listOf(city))

        // Act: get the count and query the city
        val count = dao.getCount()
        val cities = dao.searchCities("Test", false)

        // Assert: verify that the insertion was successful
        assertEquals(1, count)
        val retrievedCity = cities.first() // first emission from the Flow
        assertEquals(1, retrievedCity.size)
        assertEquals(city.name, retrievedCity.first().name)
    }
}
