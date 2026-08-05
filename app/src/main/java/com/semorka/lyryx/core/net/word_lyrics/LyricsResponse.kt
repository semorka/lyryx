package com.semorka.lyryx.core.net.word_lyrics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LyricsResponse(
    val kind: String? = null,
    @SerialName("commontrack_id")
    val commontrackId: Long? = null,
    val lrc: String? = null,
    val error: String? = null
)