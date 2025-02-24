package com.test.citychallenge.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
data class Map(val cityJson: String)