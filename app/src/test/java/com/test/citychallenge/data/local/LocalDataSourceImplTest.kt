package com.test.citychallenge.data.local

import android.content.Context
import com.test.citychallenge.data.local.dao.CityDao
import com.test.citychallenge.data.local.model.CityEntity
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LocalDataSourceImplTest {

    private lateinit var dao: CityDao
    private lateinit var context: Context
    private lateinit var selectedCityStore: SelectedCityStore
    private lateinit var localDataSource: LocalDataSourceImpl

    @Before
    fun setup() {
        dao = mockk()
        context = mockk(relaxed = true)
        selectedCityStore = spyk(SelectedCityStore(context))
        localDataSource = LocalDataSourceImpl(dao, selectedCityStore)
    }

    @Test
    fun `isEmpty returns true when dao returns 0 count`() = runTest {
        // Arrange
        coEvery { dao.getCount() } returns 0

        // Act
        val result = localDataSource.isEmpty()

        // Assert
        assertTrue(result)
        coVerify(exactly = 1) { dao.getCount() }
    }

    @Test
    fun `isEmpty returns false when dao returns non-zero count`() = runTest {
        // Arrange
        coEvery { dao.getCount() } returns 5

        // Act
        val result = localDataSource.isEmpty()

        // Assert
        assertFalse(result)
        coVerify(exactly = 1) { dao.getCount() }
    }

    @Test
    fun `searchCities delegates to dao and returns a Flow`() = runTest {
        // Arrange
        val cityEntity = CityEntity(
            id = 1,
            name = "TestCity",
            country = "TC",
            lat = 0.0,
            lon = 0.0,
            isFavorite = false
        )
        val flowResult = flowOf(listOf(cityEntity))
        every { dao.searchCities("test", false) } returns flowResult

        // Act
        val resultFlow = localDataSource.searchCities("test", false)
        val list = resultFlow.toList().flatten()

        // Assert
        assertEquals(1, list.size)
        assertEquals(cityEntity, list.first())
        verify(exactly = 1) { dao.searchCities("test", false) }
    }

    @Test
    fun `insertCities delegates to dao`() = runTest {
        // Arrange
        val cityEntity = CityEntity(
            id = 1,
            name = "TestCity",
            country = "TC",
            lat = 0.0,
            lon = 0.0,
            isFavorite = false
        )
        coEvery { dao.insertCities(listOf(cityEntity)) } returns Unit

        // Act
        localDataSource.insertCities(listOf(cityEntity))

        // Assert
        coVerify(exactly = 1) { dao.insertCities(listOf(cityEntity)) }
    }

    @Test
    fun `toggleFavorite delegates to dao`() = runTest {
        // Arrange
        coEvery { dao.toggleFavorite(1L) } returns Unit

        // Act
        localDataSource.toggleFavorite(1L)

        // Assert
        coVerify(exactly = 1) { dao.toggleFavorite(1L) }
    }
}
