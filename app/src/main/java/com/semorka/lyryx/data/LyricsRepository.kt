package com.semorka.lyryx.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LyricsRepository(private val lyricsDao: LyricsDao) {

    val userList: LiveData<List<LyricsEntity>> = lyricsDao.getLyrics()

    suspend fun addLyrics(lyrics: LyricsEntity){
        lyricsDao.addLyrics(lyrics)
    }

    suspend fun searchLyrics(artistName: String, songName: String): List<LyricsEntity>  {
        return lyricsDao.searchLyrics(artistName, songName)
    }

    // TODO удаление
}