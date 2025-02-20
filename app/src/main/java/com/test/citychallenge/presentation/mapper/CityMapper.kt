package com.test.citychallenge.presentation.mapper

import com.test.citychallenge.domain.model.CityModel
import com.test.citychallenge.domain.model.Coord
import com.test.citychallenge.presentation.model.CityUIItem

fun CityUIItem.toModel(): CityModel {
    return CityModel(
        id = this.id,
        name = this.name,
        country = this.country,
        coord = Coord(lat = this.lat, lon = this.lon),
        isFavorite = this.isFavorite
    )
}

fun CityModel.toUIItem(): CityUIItem {
    return CityUIItem(
        id = this.id,
        name = this.name,
        country = this.country,
        lat = this.coord.lat,
        lon = this.coord.lon,
        isFavorite = false
    )
}