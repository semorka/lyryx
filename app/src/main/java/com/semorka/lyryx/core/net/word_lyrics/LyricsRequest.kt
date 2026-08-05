package com.semorka.lyryx.core.net.word_lyrics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LyricsRequest(
    val track: String,
    val artist: String,
    val album: String? = null,
    val duration: Long? = null,
    @SerialName("deezer_id")
    val deezerId: String? = null
)