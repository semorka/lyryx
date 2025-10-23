package com.semorka.lyryx.data

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class LyricsViewModel(application: Application): ViewModel(), BaseLyricsViewModel{

    val lyricsList: LiveData<List<LyricsEntity>>
    private val repository: LyricsRepository
    override var artistName by mutableStateOf("")
    override var songName by mutableStateOf("")
    override var syncedText by mutableStateOf("")


    private var _searchResult by mutableStateOf<List<LyricsEntity>>(emptyList())
    override val searchResult: List<LyricsEntity>
        get() = _searchResult

    init {
        val lyricsDb = LyricsRoomDatabase.getInstance(application)
        val lyricsDao = lyricsDb.lyricsDao()
        repository = LyricsRepository(lyricsDao)
        lyricsList = repository.userList
    }

    override fun changeArtistName(value: String){
        artistName = value
    }

    override fun changeSongName(value: String){
        songName = value
    }

    override fun changeSyncedText(value: String){
        syncedText = value
    }

     override suspend fun addLyrics(){
        repository.addLyrics(
            LyricsEntity(artistName = artistName, songName = songName, syncedText = syncedText)
        )
    }

    override suspend fun searchLyrics(artistName: String, songName: String){
        _searchResult = repository.searchLyrics(artistName, songName)
    }
}