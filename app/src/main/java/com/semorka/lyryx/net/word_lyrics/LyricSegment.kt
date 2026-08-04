package com.semorka.lyryx.net.word_lyrics

import kotlinx.serialization.Serializable

@Serializable
data class LyricSegment(
    val lineTimeMillis: Long,
    val text: String,
    val words: List<WordSegment> = emptyList()
)