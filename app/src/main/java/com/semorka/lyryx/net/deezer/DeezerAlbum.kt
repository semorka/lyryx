package com.semorka.lyryx.net.deezer

import kotlinx.serialization.Serializable

@Serializable
data class DeezerAlbum (
    val id: Long,
    val cover_medium: String
)