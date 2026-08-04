package com.semorka.lyryx.net.deezer

import kotlinx.serialization.Serializable

@Serializable
data class DeezerResponse(
    val data: List<DeezerTrack>
)