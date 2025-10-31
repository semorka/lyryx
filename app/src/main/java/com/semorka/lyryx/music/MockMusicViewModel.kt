package com.semorka.lyryx.music

import com.semorka.lyryx.genius.models.GeniusArtist
import com.semorka.lyryx.genius.models.GeniusSong

object MockMusicViewModel {
    fun createMusicViewModel(): MusicViewModel {
        return MusicViewModel().apply {
            searchText = "Найдено 3 песни"
            songs = listOf(
                GeniusSong(
                    0,
                    "Under Your Spell",
                    "Snow strippers",
                    "",
                    "https://images.genius.com/cd7ccf1abbe677ab7c8a8d65586e7f6b.1000x1000x1.png",
                    "",
                    GeniusArtist(0, "Snow strippers", "")
                ),
            )
        }
    }
}