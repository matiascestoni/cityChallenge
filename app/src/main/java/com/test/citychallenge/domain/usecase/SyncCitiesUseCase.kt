package com.test.citychallenge.domain.usecase

import com.test.citychallenge.common.Response
import com.test.citychallenge.domain.CityRepository
import javax.inject.Inject

class SyncCitiesUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {
    suspend operator fun invoke(): Response<Boolean> {
        return cityRepository.syncCities()
    }
}