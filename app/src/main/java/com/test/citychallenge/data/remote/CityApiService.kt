package com.test.citychallenge.data.remote

import com.test.citychallenge.domain.model.CityModel
import retrofit2.http.GET

interface CityApiService {

    @GET("cities.json")
    suspend fun getCities(): List<CityModel>
}