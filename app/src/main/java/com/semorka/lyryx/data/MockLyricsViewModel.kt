package com.semorka.lyryx.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MockLyricsViewModel(
    override var artistName: String = "Nepon",
    override var songName: String = "Pon",
    override var syncedText: String = "Mock lyrics text"
) : BaseLyricsViewModel {

    private val _lyricsList = MutableLiveData<List<LyricsEntity>>(emptyList())
    override var lyricsList: LiveData<List<LyricsEntity>> = _lyricsList

    private var _searchResult by mutableStateOf<List<LyricsEntity>>(emptyList())
    override val searchResult: List<LyricsEntity>
        get() = _searchResult

    override fun changeArtistName(value: String) {
        artistName = value
    }

    override fun changeSongName(value: String) {
        songName = value
    }

    override fun changeSyncedText(value: String) {
        syncedText = value
    }

    override suspend fun addLyrics() {
        val currentList = _lyricsList.value ?: emptyList()
        val newLyrics = LyricsEntity(
            artistName = artistName,
            songName = songName,
            syncedText = syncedText
        )
        _lyricsList.value = currentList + newLyrics
    }

    override suspend fun searchLyrics(artistName: String, songName: String) {
        _searchResult = listOf(
            LyricsEntity(
                artistName = artistName,
                songName = songName,
                syncedText = "Mock synced lyrics for $songName by $artistName"
            )
        )
    }
}