package com.semorka.lyryx.core.net.deezer

import kotlinx.serialization.Serializable

@Serializable
data class DeezerResponse(
    val data: List<DeezerTrack>
)