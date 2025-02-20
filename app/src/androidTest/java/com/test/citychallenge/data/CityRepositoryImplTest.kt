package com.test.citychallenge.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.test.citychallenge.common.Response
import com.test.citychallenge.data.local.LocalDataSource
import com.test.citychallenge.data.local.LocalDataSourceImpl
import com.test.citychallenge.data.local.dao.CityDao
import com.test.citychallenge.data.local.database.CityDatabase
import com.test.citychallenge.data.local.model.CityEntity
import com.test.citychallenge.data.remote.CityApiService
import com.test.citychallenge.domain.model.CityModel
import com.test.citychallenge.domain.model.Coord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CityRepositoryImplTest {

    private lateinit var database: CityDatabase
    private lateinit var dao: CityDao
    private lateinit var localDataSource: LocalDataSource
    private lateinit var fakeApiService: FakeCityApiService
    private lateinit var repository: CityRepositoryImpl

    @Before
    fun setUp() {
        // Set up an in-memory Room database.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CityDatabase::class.java
        )
            .allowMainThreadQueries() // For testing only!
            .build()

        dao = database.cityDAO()
        localDataSource = LocalDataSourceImpl(dao)
        fakeApiService = FakeCityApiService()
        repository = CityRepositoryImpl(fakeApiService, localDataSource)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun syncCities_insertsDataWhenDbIsEmpty() = runBlocking {
        // Ensure DB is empty.
        assertTrue(localDataSource.isEmpty())

        // Call syncCities.
        val response = repository.syncCities()

        // Expect a success response.
        assertTrue(response is Response.Success && response.result)
        // And now the DB should no longer be empty.
        assertFalse(localDataSource.isEmpty())
    }

    @Test
    fun syncCities_doesNotInsertWhenDbIsNotEmpty() = runBlocking {
        // Pre-insert a city so DB is not empty.
        val preExistingCity = CityEntity(
            id = 100,
            name = "ExistingCity",
            country = "EX",
            lat = 1.0,
            lon = 1.0,
            isFavorite = false
        )
        localDataSource.insertCities(listOf(preExistingCity))
        assertFalse(localDataSource.isEmpty())

        // Call syncCities, which should skip remote fetching.
        val response = repository.syncCities()

        // Verify that response is successful.
        assertTrue(response is Response.Success && response.result)
        // And the count remains unchanged.
        assertEquals(1, dao.getCount())
    }

    @Test
    fun searchCities_returnsMappedData() = runTest {
        // Insert test cities into the local DB.
        val city1 = CityEntity(
            id = 1,
            name = "Alpha",
            country = "AA",
            lat = 0.0,
            lon = 0.0,
            isFavorite = false
        )
        val city2 = CityEntity(
            id = 2,
            name = "Beta",
            country = "BB",
            lat = 0.0,
            lon = 0.0,
            isFavorite = true
        )
        localDataSource.insertCities(listOf(city1, city2))

        // Search using prefix "Al" (should return only "Alpha").
        repository.searchCities("Al", onlyFavorites = false).test {
            // We expect a single emission.
            val response = awaitItem()
            assertTrue(response is Response.Success)
            response as Response.Success<List<CityModel>>
            assertEquals(1, response.result.size)
            assertEquals("Alpha", response.result.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun toggleFavorite_updatesCityFavoriteStatus() = runBlocking {
        // Insert a city with isFavorite set to false.
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
        repository.toggleFavorite(1L)

        // Retrieve the updated city.
        val updatedCity = dao.searchCities("Test", onlyFavorites = false).first().first()
        assertTrue(updatedCity.isFavorite)
    }
}

/**
 * A fake implementation of CityApiService for testing.
 */
class FakeCityApiService : CityApiService {
    override suspend fun getCities(): List<CityModel> {
        return listOf(
            CityModel(
                id = 1,
                name = "FakeCity",
                country = "FC",
                coord = Coord(lon = 10.0, lat = 20.0)
            ),
            CityModel(
                id = 2,
                name = "AnotherCity",
                country = "AC",
                coord = Coord(lon = 30.0, lat = 40.0)
            )
        )
    }
}
