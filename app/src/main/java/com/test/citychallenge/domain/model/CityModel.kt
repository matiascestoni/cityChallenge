package com.test.citychallenge.domain.model

import com.google.gson.annotations.SerializedName

data class CityModel(
    @SerializedName("_id") val id: Long,
    val name: String,
    val country: String,
    val coord: Coord,
    val isFavorite: Boolean = false
)

data class Coord(
    val lon: Double,
    val lat: Double
)