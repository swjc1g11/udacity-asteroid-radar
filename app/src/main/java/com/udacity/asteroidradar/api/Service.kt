package com.udacity.asteroidradar.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.BuildConfig
import com.udacity.asteroidradar.Constants
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// TODO Add marsApiKey to gradle.properties for this service to work
const val API_KEY = BuildConfig.MARS_API_KEY

interface NasaService {

    @GET("/planetary/apod")
    suspend fun getImageOfTheDayUrl(
            @Query("api_key") apiKey: String = API_KEY
    ): NetworkImageOfTheDay

    @GET("/neo/rest/v1/feed")
    suspend fun getNearEarthObjectsBetweenDates(
            @Query("start_date") startDateFormatted: String,
            @Query("end_date") endDateFormatted: String,
            @Query("api_key") apiKey: String = API_KEY
    ) : String
}

private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

object Network {
    private val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    val nasaService = retrofit.create(NasaService::class.java)
}