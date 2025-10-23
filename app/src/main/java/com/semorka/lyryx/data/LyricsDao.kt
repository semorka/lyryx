package com.semorka.lyryx.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics")
    fun getLyrics(): LiveData<List<LyricsEntity>>

    @Insert
    suspend fun addLyrics(lyrics: LyricsEntity)

    @Query("SELECT * FROM lyrics WHERE LOWER(artistName) LIKE LOWER('%' || :artistName || '%') AND LOWER(songName) LIKE LOWER('%' || :songName || '%')")
    suspend fun searchLyrics(artistName: String, songName: String): List<LyricsEntity>

    // TODO удаление
}