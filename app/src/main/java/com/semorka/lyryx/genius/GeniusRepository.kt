package com.semorka.lyryx.genius

import com.semorka.lyryx.genius.models.GeniusSong
import com.semorka.lyryx.supabase
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText

class GeniusRepository {
    private val apiService = GeniusApiService.create()

    suspend fun searchSongs(query: String): List<GeniusSong> {
        val token = getGeniusToken()
        val response = apiService.searchSongs(token = "Bearer $token", query = query)
        return response.response.hits.map { it.result }
    }

    suspend fun searchSongsByArtist(artist: String, song: String): List<GeniusSong> {
        return searchSongs("$artist $song")
    }

    private suspend fun getGeniusToken(): String {
        val response = supabase.functions
            .invoke("genius-proxy")
        return response.bodyAsText()
    }
}