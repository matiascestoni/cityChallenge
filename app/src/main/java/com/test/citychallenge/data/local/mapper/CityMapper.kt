package com.test.citychallenge.data.local.mapper

import com.test.citychallenge.data.local.model.CityEntity
import com.test.citychallenge.domain.model.CityModel
import com.test.citychallenge.domain.model.Coord

fun CityEntity.toModel(): CityModel {
    return CityModel(
        id = this.id,
        name = this.name,
        country = this.country,
        coord = Coord(lat = this.lat, lon = this.lon),
        isFavorite = this.isFavorite
    )
}

fun CityModel.toEntity(): CityEntity {
    return CityEntity(
        id = this.id,
        name = this.name,
        country = this.country,
        lat = this.coord.lat,
        lon = this.coord.lon,
        isFavorite = false
    )
}