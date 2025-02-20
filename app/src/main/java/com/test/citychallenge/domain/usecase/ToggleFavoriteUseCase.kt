package com.test.citychallenge.domain.usecase

import com.test.citychallenge.common.Response
import com.test.citychallenge.domain.CityRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {
    suspend operator fun invoke(cityId: Long): Response<Unit> {
        return try {
            cityRepository.toggleFavorite(cityId)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }
}