package com.test.citychallenge.domain.usecase

import com.test.citychallenge.common.Response
import com.test.citychallenge.domain.CityRepository
import com.test.citychallenge.domain.model.CityModel
import javax.inject.Inject

class GetSelectedCityUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {
    suspend operator fun invoke(): Response<CityModel?> {
        return try {
            val city = cityRepository.getSelectedCity()
            Response.Success(city)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }
}