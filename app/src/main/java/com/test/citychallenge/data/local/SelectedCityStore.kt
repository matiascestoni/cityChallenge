package com.test.citychallenge.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.test.citychallenge.data.local.model.CityEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectedCityStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(name = "selected_city")
    private val selectedCityKey = stringPreferencesKey("selected_city")

    suspend fun saveSelectedCity(city: CityEntity) {
        context.dataStore.edit { preferences ->
            preferences[selectedCityKey] = Json.encodeToString(city)
        }
    }

    suspend fun getSelectedCity(): CityEntity? {
        return try {
            context.dataStore.data
                .map { preferences ->
                    preferences[selectedCityKey]?.let {
                        Json.decodeFromString<CityEntity>(it)
                    }
                }
                .first()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clearSelectedCity() {
        context.dataStore.edit { preferences ->
            preferences.remove(selectedCityKey)
        }
    }
}