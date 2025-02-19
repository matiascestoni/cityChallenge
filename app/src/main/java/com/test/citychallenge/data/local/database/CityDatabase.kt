package com.test.citychallenge.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.test.citychallenge.data.local.dao.CityDao
import com.test.citychallenge.data.local.model.CityEntity

@Database(entities = [CityEntity::class], version = 1, exportSchema = false)
abstract class CityDatabase : RoomDatabase() {
    abstract fun cityDAO(): CityDao
}