package com.test.citychallenge.domain.usecase

import com.test.citychallenge.common.Response
import com.test.citychallenge.domain.CityRepository
import com.test.citychallenge.domain.model.CityModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CityFilterUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {
    operator fun invoke(
        prefix: String,
        onlyFavorites: Boolean = false
    ): Flow<Response<List<CityModel>>> {
        return cityRepository.searchCities(prefix, onlyFavorites)
    }
}