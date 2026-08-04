package com.semorka.lyryx.net.deezer

import kotlinx.serialization.Serializable

@Serializable
data class DeezerArtist (
    val id: Long,
    val name: String
)