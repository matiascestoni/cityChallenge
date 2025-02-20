package com.test.citychallenge.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CityUIItem(
    val id: Long,
    val name: String,
    val country: String,
    val lon: Double,
    val lat: Double,
    var isFavorite: Boolean = false
): Parcelable