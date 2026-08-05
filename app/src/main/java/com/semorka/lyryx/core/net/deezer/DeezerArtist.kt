package com.semorka.lyryx.core.net.deezer

import kotlinx.serialization.Serializable

@Serializable
data class DeezerArtist (
    val id: Long,
    val name: String
)