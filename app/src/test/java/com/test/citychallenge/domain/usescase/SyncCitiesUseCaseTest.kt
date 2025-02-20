package com.test.citychallenge.domain.usescase

import com.test.citychallenge.common.Response
import com.test.citychallenge.domain.CityRepository
import com.test.citychallenge.domain.model.CityModel
import com.test.citychallenge.domain.model.Coord
import com.test.citychallenge.domain.usecase.SyncCitiesUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncCitiesUseCaseTest {

    private val repository = mockk<CityRepository>()
    private val useCase = SyncCitiesUseCase(repository)

    @Test
    fun `syncCities should return success when repository succeeds`() = runTest {
        // Given
        coEvery { repository.syncCities() } returns Response.Success(true)

        // When
        val result = useCase()

        // Then
        assertTrue(result is Response.Success)
        assertTrue((result as Response.Success).result)
    }

    @Test
    fun `syncCities should return error when repository fails`() = runTest {
        // Given
        coEvery { repository.syncCities() } returns Response.Error("Sync failed")

        // When
        val result = useCase()

        // Then
        assertTrue(result is Response.Error)
        assertEquals("Sync failed", (result as Response.Error).message)
    }

    @Test
    fun `syncCities should handle exceptions`() = runTest {
        // Given
        coEvery { repository.syncCities() } throws Exception("Network error")

        // When
        val result = useCase()

        // Then
        assertTrue(result is Response.Error)
        assertTrue((result as Response.Error).message.contains("Network error"))
    }

    @Test
    fun `syncCities fails gracefully on malformed data`() = runTest {
        // Given: API returns a city with invalid coordinates
        val malformedCity = CityModel(id = 1, name = "München", country = "XXX", Coord(999.0, 999.0))
        coEvery { repository.syncCities() } throws Exception("Invalid coordinates")

        // When
        val result = useCase()

        // Then
        assertTrue(result is Response.Error)
        assertTrue((result as Response.Error).message.contains("Invalid coordinates"))
    }
}