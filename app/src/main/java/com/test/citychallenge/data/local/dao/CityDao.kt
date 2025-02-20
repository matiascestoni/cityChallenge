package com.test.citychallenge.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.test.citychallenge.data.local.model.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Query("SELECT COUNT(*) FROM cities")
    suspend fun getCount(): Int

    @Query(
        "SELECT * FROM cities WHERE LOWER(name) LIKE LOWER(:prefix || '%') " +
                "AND (:onlyFavorites = 0 OR isFavorite = 1) " +
                "ORDER BY name, country"
    )
    fun searchCities(prefix: String, onlyFavorites: Boolean): Flow<List<CityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<CityEntity>)

    @Query("UPDATE cities SET isFavorite = NOT isFavorite WHERE id = :cityId")
    suspend fun toggleFavorite(cityId: Long)
}