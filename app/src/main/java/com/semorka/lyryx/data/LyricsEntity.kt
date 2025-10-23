package com.semorka.lyryx.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics")
data class LyricsEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var artistName: String,
    var songName: String,
    val syncedText: String,
)