package com.test.citychallenge.data.local

import com.test.citychallenge.data.local.model.CityEntity
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    suspend fun isEmpty(): Boolean
    fun searchCities(prefix: String, onlyFavorites: Boolean) : Flow<List<CityEntity>>
    suspend fun insertCities(cities: List<CityEntity>)
    suspend fun toggleFavorite(cityId: Long)
}