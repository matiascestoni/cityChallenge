package com.test.citychallenge.data.local

import androidx.paging.PagingSource
import com.test.citychallenge.data.local.model.CityEntity
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    suspend fun isEmpty(): Boolean
    fun searchCities(prefix: String, onlyFavorites: Boolean): Flow<List<CityEntity>>
    suspend fun insertCities(cities: List<CityEntity>)
    suspend fun toggleFavorite(cityId: Long)
    suspend fun getSelectedCity(): CityEntity?
    suspend fun setSelectedCity(city: CityEntity)
    fun pagingSource(
        prefix: String, onlyFavorites: Boolean
    ): PagingSource<Int, CityEntity>

    suspend fun insertCity(city: CityEntity)
}