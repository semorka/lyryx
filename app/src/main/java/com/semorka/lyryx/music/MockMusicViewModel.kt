package com.semorka.lyryx.music

object MockMusicViewModel {
    fun createMusicViewModel(): MusicViewModel {
        return MusicViewModel().apply {
            searchText = "Найдено 3 текста"
            songs = listOf(
                Music("Snow Strippers", "Under Your Spell", "[00:00.00] You keep me under your spell"),
                Music("Artist2", "Song2", "[00:00.00] Lyrics line 1")
            )
        }
    }
}