package com.semorka.lyryx.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface BaseLyricsViewModel {
    var artistName: String
    var songName: String
    var syncedText: String
    val searchResult: List<LyricsEntity>
    fun changeArtistName(value: String)
    fun changeSongName(value: String)
    fun changeSyncedText(value: String)

    suspend fun searchLyrics(artistName: String, songName: String)
    suspend fun addLyrics()
}