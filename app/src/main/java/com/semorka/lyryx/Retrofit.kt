package com.semorka.lyryx

import com.semorka.lyryx.music.MusicApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("https://lrclib.net/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val musicApi: MusicApi = retrofit.create(MusicApi::class.java)