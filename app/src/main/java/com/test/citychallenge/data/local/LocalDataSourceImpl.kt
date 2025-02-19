package com.test.citychallenge.data.local

import com.test.citychallenge.data.local.dao.CityDao
import com.test.citychallenge.data.local.model.CityEntity
import javax.inject.Inject

internal class LocalDataSourceImpl @Inject constructor(
    private val dao: CityDao
): LocalDataSource {

    override suspend fun isEmpty(): Boolean = dao.getCount() == 0

    override fun searchCities(prefix: String, onlyFavorites: Boolean) =
        dao.searchCities(prefix, onlyFavorites)

    override suspend fun insertCities(cities: List<CityEntity>) =
        dao.insertCities(cities)

    override suspend fun toggleFavorite(cityId: Long) =
        dao.toggleFavorite(cityId)
}