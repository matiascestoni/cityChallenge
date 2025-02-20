package com.test.citychallenge.data


import com.test.citychallenge.common.Response
import com.test.citychallenge.data.local.LocalDataSource
import com.test.citychallenge.data.local.model.CityEntity
import com.test.citychallenge.data.remote.CityApiService
import com.test.citychallenge.domain.model.CityModel
import com.test.citychallenge.domain.model.Coord
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CityRepositoryImplTest {

    private lateinit var apiService: CityApiService
    private lateinit var localDataSource: LocalDataSource
    private lateinit var repository: CityRepositoryImpl

    @Before
    fun setUp() {
        apiService = mockk()
        localDataSource = mockk()
        repository = CityRepositoryImpl(apiService, localDataSource)
    }

    @Test
    fun `syncCities should fetch remote cities and insert them when DB is empty`() = runTest {
        // Arrange: Local DB is empty.
        coEvery { localDataSource.isEmpty() } returns true

        // Prepare a fake remote city
        val remoteCity = CityModel(
            id = 1,
            name = "TestCity",
            country = "TC",
            coord = Coord(lon = 0.0, lat = 0.0)
        )
        coEvery { apiService.getCities() } returns listOf(remoteCity)
        coEvery {
            localDataSource.insertCities(match { list ->
                list.size == 1 && list.first().id == remoteCity.id
            })
        } returns Unit

        // Act
        val result = repository.syncCities()

        // Assert
        assertTrue(result is Response.Success && result.result)
        coVerify(exactly = 1) { localDataSource.isEmpty() }
        coVerify(exactly = 1) { apiService.getCities() }
        coVerify(exactly = 1) { localDataSource.insertCities(any()) }
    }

    @Test
    fun `syncCities should not fetch remote cities when DB is not empty`() = runTest {
        // Arrange: Local DB is not empty.
        coEvery { localDataSource.isEmpty() } returns false

        // Act
        val result = repository.syncCities()

        // Assert
        assertTrue(result is Response.Success && result.result)
        coVerify(exactly = 1) { localDataSource.isEmpty() }
        coVerify(exactly = 0) { apiService.getCities() }
        coVerify(exactly = 0) { localDataSource.insertCities(any()) }
    }

    @Test
    fun `syncCities returns error when apiService throws exception`() = runTest {
        // Arrange: DB is empty and API throws an exception.
        coEvery { localDataSource.isEmpty() } returns true
        val errorMsg = "API failure"
        coEvery { apiService.getCities() } throws Exception(errorMsg)

        // Act
        val result = repository.syncCities()

        // Assert
        assertTrue(result is Response.Error && result.message.contains(errorMsg))
        coVerify(exactly = 1) { localDataSource.isEmpty() }
        coVerify(exactly = 1) { apiService.getCities() }
        coVerify(exactly = 0) { localDataSource.insertCities(any()) }
    }

    @Test
    fun `searchCities returns mapped result when localDataSource succeeds`() = runTest {
        // Arrange: prepare a CityEntity to be returned.
        val cityEntity = CityEntity(
            id = 1,
            name = "TestCity",
            country = "TC",
            lat = 0.0,
            lon = 0.0,
            isFavorite = false
        )
        // localDataSource.searchCities returns a Flow that emits a list with our entity.
        every { localDataSource.searchCities(any(), any()) } returns flowOf(listOf(cityEntity))

        // Act
        val responses = repository.searchCities("test", onlyFavorites = false).toList()

        // Assert: we expect a single Response.Success with the mapped model.
        assertEquals(1, responses.size)
        val response = responses.first()
        assertTrue(response is Response.Success)
        response as Response.Success
        assertEquals(1, response.result.size)
        assertEquals(cityEntity.id, response.result.first().id)
        assertEquals(cityEntity.name, response.result.first().name)
    }

    @Test
    fun `searchCities emits error when localDataSource flow throws exception`() = runTest {
        // Arrange: simulate an error within the Flow.
        val errorMsg = "Database error"
        every { localDataSource.searchCities(any(), any()) } returns flow<List<CityEntity>> {
            throw Exception(errorMsg)
        }

        // Act
        val responses = repository.searchCities("test", onlyFavorites = false)
            .catch { } // swallow extra exceptions to collect the Response
            .toList()

        // Assert: we expect a Response.Error.
        assertEquals(1, responses.size)
        val response = responses.first()
        assertTrue(response is Response.Error)
        response as Response.Error
        assertTrue(response.message.contains(errorMsg))
    }

    @Test
    fun `toggleFavorite calls localDataSource toggleFavorite successfully`() = runTest {
        // Arrange
        coEvery { localDataSource.toggleFavorite(1L) } returns Unit

        // Act
        repository.toggleFavorite(1L)

        // Assert
        coVerify(exactly = 1) { localDataSource.toggleFavorite(1L) }
    }

    @Test(expected = Exception::class)
    fun `toggleFavorite throws exception when localDataSource fails`() = runTest {
        // Arrange
        val errorMsg = "Toggle failed"
        coEvery { localDataSource.toggleFavorite(1L) } throws Exception(errorMsg)

        // Act: toggleFavorite should throw an exception.
        repository.toggleFavorite(1L)
    }
}
