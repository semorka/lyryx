package com.semorka.lyryx.net.word_lyrics

import kotlinx.serialization.Serializable

@Serializable
data class WordSegment(
    val timeMillis: Long,
    val word: String
)