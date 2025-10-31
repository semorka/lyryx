package com.semorka.lyryx.genius

import com.semorka.lyryx.genius.models.GeniusSearchResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GeniusApiService {
    @GET("search")
    suspend fun searchSongs(
        @Header("Authorization") token: String,
        @Query("q") query: String
    ): GeniusSearchResponse

    companion object {
        private const val BASE_URL = "https://api.genius.com/"

        fun create(): GeniusApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeniusApiService::class.java)
        }
    }
}