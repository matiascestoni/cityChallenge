package com.test.citychallenge.data.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.test.citychallenge.data.CityRepositoryImpl
import com.test.citychallenge.data.local.LocalDataSource
import com.test.citychallenge.data.local.LocalDataSourceImpl
import com.test.citychallenge.data.local.SelectedCityStore
import com.test.citychallenge.data.local.dao.CityDao
import com.test.citychallenge.data.local.database.CityDatabase
import com.test.citychallenge.data.remote.CityApiService
import com.test.citychallenge.domain.CityRepository
import com.test.citychallenge.presentation.navigation.NavigationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCityDatabase(@ApplicationContext context: Context): CityDatabase {
        return Room.databaseBuilder(
            context,
            CityDatabase::class.java,
            "city_database"
        ).build()
    }

    @Provides
    fun provideCityDAO(database: CityDatabase): CityDao = database.cityDAO()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://gist.githubusercontent.com/hernan-uala/dce8843a8edbe0b0018b32e137bc2b3a/raw/0996accf70cb0ca0e16f9a99e0ee185fafca7af1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAPIService(retrofit: Retrofit): CityApiService {
        return retrofit.create(CityApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLocalDataSource(cityDAO: CityDao, selectedCityStore: SelectedCityStore): LocalDataSource {
        return LocalDataSourceImpl(cityDAO, selectedCityStore)
    }


    @Provides
    @Singleton
    fun provideCityRepository(
        apiService: CityApiService,
        localDataSource: LocalDataSource
    ): CityRepository {
        return CityRepositoryImpl(apiService, localDataSource)
    }

    @Provides
    @Singleton
    fun providesNavigationManager(): NavigationManager {
        return NavigationManager()
    }

    @Provides
    @Singleton
    fun provideSelectedCityStore(@ApplicationContext context: Context): SelectedCityStore {
        return SelectedCityStore(context)
    }
}
