package com.test.citychallenge.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

class CityApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: CityApiService
    private val gson: Gson = GsonBuilder().create()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()

        // Create Retrofit instance using the mock WebServer's URL
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/")) // Dynamic base URL for MockWebServer
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CityApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getCities should return a list of cities`() = runBlocking {
        // Sample JSON response
        val jsonResponse = """
            [
                {
                    "id": 1,
                    "name": "TestCity",
                    "country": "TC",
                    "coord": { "lat": 10.0, "lon": 20.0 }
                },
                {
                    "id": 2,
                    "name": "AnotherCity",
                    "country": "AC",
                    "coord": { "lat": 30.0, "lon": 40.0 }
                }
            ]
        """.trimIndent()

        // Enqueue a successful mock response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(jsonResponse)
        )

        // Act - Call API
        val cities = apiService.getCities()

        // Assert - Validate the response
        assertEquals(2, cities.size)
        assertEquals("TestCity", cities[0].name)
        assertEquals("AnotherCity", cities[1].name)
    }

    @Test
    fun `getCities should return empty list when API returns empty response`() = runBlocking {
        // Enqueue an empty JSON response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("[]")
        )

        // Act - Call API
        val cities = apiService.getCities()

        // Assert - Validate that the list is empty
        assertEquals(0, cities.size)
    }

    @Test
    fun `getCities should throw exception on HTTP error`() = runBlocking {
        // Enqueue an HTTP 500 error response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
        )

        try {
            // Act - Call API (should throw an exception)
            apiService.getCities()
            assert(false) // Fail test if no exception occurs
        } catch (e: Exception) {
            // Assert - Ensure exception was thrown
            assert(e.message!!.contains("500"))
        }
    }
}
