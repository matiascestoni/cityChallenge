package com.test.citychallenge.domain.usecase

import com.test.citychallenge.common.Response
import com.test.citychallenge.domain.CityRepository
import com.test.citychallenge.domain.model.CityModel
import javax.inject.Inject

class SetSelectedCityUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {
    suspend operator fun invoke(city: CityModel): Response<Unit> {
        return try {
            cityRepository.setSelectedCity(city)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }
}