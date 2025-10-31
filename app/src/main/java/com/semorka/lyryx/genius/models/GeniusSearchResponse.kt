package com.semorka.lyryx.genius.models

data class GeniusSearchResponse(
    val meta: GeniusMeta,
    val response: GeniusResponse
)

data class GeniusMeta(
    val status: Int
)

data class GeniusResponse(
    val hits: List<GeniusHit>
)

data class GeniusHit(
    val index: String,
    val type: String,
    val result: GeniusSong
)