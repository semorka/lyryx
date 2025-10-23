package com.semorka.lyryx.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MockLyricsViewModel(
    override var artistName: String = "Nepon",
    override var songName: String = "Pon",
    override var syncedText: String
) : BaseLyricsViewModel {
    private var _searchResult by mutableStateOf<List<LyricsEntity>>(emptyList())
    override val searchResult: List<LyricsEntity>
        get() = _searchResult
    override fun changeArtistName(value: String) {}
    override fun changeSongName(value: String) {}
    override fun changeSyncedText(value: String) {}
    override suspend fun addLyrics(){}
    override suspend fun searchLyrics(artistName: String, songName: String) {
        _searchResult = listOf(
            LyricsEntity(artistName = artistName, songName = songName, syncedText = "Mock lyrics text")
        )
    }
}