package com.semorka.lyryx.core.data

import com.semorka.lyryx.net.lrclib.LyricSegment

data class Track(
    val artistName: String,
    val trackName: String,
    val plainLyrics: String,
    val cover: String,
    val syncedSegments: List<LyricSegment> = emptyList(),
)