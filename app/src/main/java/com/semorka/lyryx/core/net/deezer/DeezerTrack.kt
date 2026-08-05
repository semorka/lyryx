package com.semorka.lyryx.core.net.deezer

import kotlinx.serialization.Serializable

@Serializable
data class DeezerTrack (
    val id: Long,
    val title: String = "Noname",
    val artist: DeezerArtist,
    val album: DeezerAlbum,
    val duration: Int = 0,
    val bpm: Float = 0f
)