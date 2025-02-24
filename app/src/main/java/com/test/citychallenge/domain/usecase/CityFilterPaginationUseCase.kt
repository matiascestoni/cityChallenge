package com.test.citychallenge.domain.usecase

import androidx.paging.PagingData
import com.test.citychallenge.domain.CityRepository
import com.test.citychallenge.domain.model.CityModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CityFilterPaginationUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {
    operator fun invoke(
        prefix: String,
        onlyFavorites: Boolean = false
    ): Flow<PagingData<CityModel>> {
        return cityRepository.searchCitiesWithPager(
            prefix.trim().lowercase(),
            onlyFavorites
        )
    }
}