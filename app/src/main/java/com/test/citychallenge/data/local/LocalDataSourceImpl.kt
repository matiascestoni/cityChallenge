package com.test.citychallenge.data.local

import androidx.paging.PagingSource
import com.test.citychallenge.data.local.dao.CityDao
import com.test.citychallenge.data.local.model.CityEntity
import javax.inject.Inject

internal class LocalDataSourceImpl @Inject constructor(
    private val dao: CityDao,
    private val selectedCityStore: SelectedCityStore
) : LocalDataSource {

    override suspend fun isEmpty(): Boolean = dao.getCount() == 0

    override fun searchCities(prefix: String, onlyFavorites: Boolean) =
        dao.searchCities(prefix, onlyFavorites)

    override suspend fun insertCities(cities: List<CityEntity>) =
        dao.insertCities(cities)

    override suspend fun toggleFavorite(cityId: Long) =
        dao.toggleFavorite(cityId)

    override suspend fun getSelectedCity(): CityEntity? {
        return selectedCityStore.getSelectedCity()
    }

    override suspend fun setSelectedCity(city: CityEntity) {
        selectedCityStore.saveSelectedCity(city)
    }

    override fun pagingSource(
        prefix: String,
        onlyFavorites: Boolean
    ): PagingSource<Int, CityEntity> {
        return dao.pagingSource(prefix, onlyFavorites)
    }

    override suspend fun insertCity(city: CityEntity) {
        dao.insertCity(city)
    }
}