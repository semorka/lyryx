package com.semorka.lyryx.genius.models

data class GeniusLyrics(
    val song_id: Int,
    val title: String,
    val artist: String,
    val lyrics: String,
    val media: List<GeniusMedia>?
)

data class GeniusMedia(
    val url: String,
    val type: String
)