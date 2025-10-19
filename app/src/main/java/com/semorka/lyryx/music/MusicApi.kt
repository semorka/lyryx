package com.semorka.lyryx.music

import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApi {
    @GET("api/search")
    suspend fun searchMusic(@Query("q") query: String): List<Music>
}