package com.shadowings.devfestmilano26.apod.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ApodApiService {
    @GET("planetary/apod")
    suspend fun getAstronomyPictureOfTheDay(
        @Query("api_key") apiKey: String,
        @Query("date") date: String? = null,
    ): ApodDto
}
