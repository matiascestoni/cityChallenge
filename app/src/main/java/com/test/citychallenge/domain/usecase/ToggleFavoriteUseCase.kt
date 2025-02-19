package com.test.citychallenge.domain.usecase

import com.test.citychallenge.domain.CityRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {
    suspend operator fun invoke(cityId: Long) {
        cityRepository.toggleFavorite(cityId)
    }
}