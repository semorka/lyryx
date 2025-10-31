package com.semorka.lyryx.genius.models

data class GeniusSong(
    val id: Int,
    val title: String,
    val artist_names: String,
    val header_image_url: String?,
    val song_art_image_url: String?,
    val url: String,
    val primary_artist: GeniusArtist
)