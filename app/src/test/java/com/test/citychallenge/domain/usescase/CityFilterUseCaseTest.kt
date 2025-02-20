package com.test.citychallenge.domain.usescase

import com.test.citychallenge.common.Response
import com.test.citychallenge.domain.CityRepository
import com.test.citychallenge.domain.model.CityModel
import com.test.citychallenge.domain.model.Coord
import com.test.citychallenge.domain.usecase.CityFilterUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class CityFilterUseCaseTest {

    private val repository = mockk<CityRepository>()
    private val useCase = CityFilterUseCase(repository)

    @Test
    fun `searchCities should return success flow when repository succeeds`() = runTest {
        // Given
        val cities =
            listOf(CityModel(id = 1, name = "Hurzuf", country = "UA", Coord(34.283333, 44.549999)))
        coEvery { repository.searchCities("hu", false) } returns flowOf(Response.Success(cities))

        // When
        val result = useCase("hu").first()

        // Then
        assertTrue(result is Response.Success)
        assertEquals(cities, (result as Response.Success).result)
    }

    @Test
    fun `searchCities should return error flow when repository fails`() = runTest {
        // Given
        coEvery {
            repository.searchCities(
                "invalid",
                false
            )
        } returns flowOf(Response.Error("Error"))

        // When
        val result = useCase("invalid").first()

        // Then
        assertTrue(result is Response.Error)
        assertEquals("Error", (result as Response.Error).message)
    }

    @Test
    fun `searchCities should handle empty prefix`() = runTest {
        // Given
        val allCities =
            listOf(CityModel(id = 1, name = "Hurzuf", country = "UA", Coord(34.283333, 44.549999)))
        coEvery { repository.searchCities("", false) } returns flowOf(Response.Success(allCities))

        // When
        val result = useCase("").first()

        // Then
        assertTrue(result is Response.Success)
        assertEquals(allCities, (result as Response.Success).result)
    }

    @Test
    fun `searchCities handles non-ASCII characters`() = runTest {
        // Given
        val city = CityModel(id = 1, name = "München", country = "GER", Coord(34.283333, 44.549999))
        coEvery { repository.searchCities("mün", false) } returns flowOf(Response.Success(listOf(city)))

        // When
        val result = useCase("mün").first()

        // Then
        assertTrue(result is Response.Success)
        assertEquals("München", (result as Response.Success).result.first().name)
    }
}