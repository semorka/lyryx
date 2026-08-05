package com.semorka.lyryx.core.navigation

import kotlinx.serialization.Serializable


sealed interface Destination {
    @Serializable
    data object LoadTrack : Destination

    @Serializable
    data object Lyrics : Destination

    sealed interface SearchFlow : Destination {
        @Serializable
        data object TrackSearch : SearchFlow
        @Serializable
        data object LyricsSearch : SearchFlow
    }
}