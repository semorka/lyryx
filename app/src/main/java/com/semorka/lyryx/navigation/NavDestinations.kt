package com.semorka.lyryx.navigation

import kotlinx.serialization.Serializable

sealed class Destination {

    @Serializable
    data object LoadTrack : Destination()

    @Serializable
    data object Search : Destination()

    @Serializable
    data object LyricsSearch : Destination()

    @Serializable
    data object Lyrics : Destination()
}