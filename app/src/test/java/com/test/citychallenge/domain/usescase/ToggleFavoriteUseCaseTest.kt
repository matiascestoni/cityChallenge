package com.test.citychallenge.domain.usescase

import com.test.citychallenge.common.Response
import com.test.citychallenge.domain.CityRepository
import com.test.citychallenge.domain.usecase.ToggleFavoriteUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    private val repository = mockk<CityRepository>()
    private val useCase = ToggleFavoriteUseCase(repository)

    @Test
    fun `toggleFavorite should delegate to repository`() = runTest {
        // Given
        coEvery { repository.toggleFavorite(1) } just Runs

        // When
        useCase(1)

        // Then
        coVerify(exactly = 1) { repository.toggleFavorite(1) }
    }

    @Test
    fun `toggleFavorite should propagate exceptions`() = runTest {
        // Given
        coEvery { repository.toggleFavorite(1) } throws Exception("DB error")

        // When
        val result = useCase(1)

        // Then
        assertTrue(result is Response.Error)
        assertTrue((result as Response.Error).message.contains("DB error"))
    }
}