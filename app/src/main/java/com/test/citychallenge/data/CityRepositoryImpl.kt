package com.test.citychallenge.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.test.citychallenge.common.Response
import com.test.citychallenge.data.local.LocalDataSource
import com.test.citychallenge.data.local.mapper.toEntity
import com.test.citychallenge.data.local.mapper.toModel
import com.test.citychallenge.data.remote.CityApiService
import com.test.citychallenge.domain.CityRepository
import com.test.citychallenge.domain.model.CityModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class CityRepositoryImpl @Inject constructor(
    private val apiService: CityApiService,
    private val localDataSource: LocalDataSource
) : CityRepository {

    override suspend fun syncCities(): Response<Boolean> {
        return try {
            // Sync with remote if local DB is empty
            if (localDataSource.isEmpty()) {
                val remoteCities = apiService.getCities().map { it.toEntity() }
                localDataSource.insertCities(remoteCities)
            }
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Sync failed")
        }
    }

    override fun searchCities(
        prefix: String,
        onlyFavorites: Boolean
    ): Flow<Response<List<CityModel>>> = flow {
        localDataSource.searchCities(
            prefix = prefix.lowercase(),
            onlyFavorites = onlyFavorites
        ).map { entities ->
            Response.Success(entities.map { it.toModel() })
        }.catch { e ->
            emit(Response.Error(e.message ?: "Search failed"))
        }.collect {
            emit(it)
        }
    }

    override suspend fun toggleFavorite(cityId: Long) {
        try {
            localDataSource.toggleFavorite(cityId)
        } catch (e: Exception) {
            throw Exception("Failed to toggle favorite: ${e.message}")
        }
    }

    override suspend fun getSelectedCity(): CityModel? {
        try {
            return localDataSource.getSelectedCity()?.toModel()
        } catch (e: Exception) {
            throw Exception("Failed to get selected city: ${e.message}")
        }
    }

    override suspend fun setSelectedCity(city: CityModel) {
        try {
            localDataSource.setSelectedCity(city.toEntity())
        } catch (e: Exception) {
            throw Exception("Failed to set selected city: ${e.message}")
        }
    }

    override fun searchCitiesWithPager(
        prefix: String,
        onlyFavorites: Boolean
    ): Flow<PagingData<CityModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = 50, // Items per page
                prefetchDistance = 20, // Load next page when 20 items remain,
                enablePlaceholders = false
            ),
        ) {
            localDataSource.pagingSource(prefix, onlyFavorites)
        }.flow.map { pagingData ->
            pagingData.map { entity -> entity.toModel() }
        }.catch {
            emit(PagingData.empty())
        }
    }
}