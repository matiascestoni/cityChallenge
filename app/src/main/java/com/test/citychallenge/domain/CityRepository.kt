package com.test.citychallenge.domain

import com.test.citychallenge.common.Response
import com.test.citychallenge.domain.model.CityModel
import kotlinx.coroutines.flow.Flow

interface CityRepository {
    suspend fun syncCities(): Response<Boolean>
    fun searchCities(prefix: String, onlyFavorites: Boolean = false): Flow<Response<List<CityModel>>>
    suspend fun toggleFavorite(cityId: Long)
}