package com.semorka.lyryx.net.word_lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semorka.lyryx.net.supabase.supabase
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WordLyricsViewModel : ViewModel() {

    private val _lyricsSegments = MutableStateFlow<List<LyricSegment>>(emptyList())
    val lyricsSegments = _lyricsSegments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    private val _lyricsKind = MutableStateFlow<String?>(null)

    val isWordSynced: StateFlow<Boolean> = _lyricsKind
        .map { kind -> kind == "word" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun fetchLyrics(
        track: String,
        artist: String,
        durationSec: Long? = null,
        deezerId: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val httpResponse = supabase.functions.invoke(
                    function = "lyrics-word-parser",
                    body = LyricsRequest(
                        track = track,
                        artist = artist,
                        duration = durationSec,
                        deezerId = deezerId
                    )
                )

                val response: LyricsResponse = httpResponse.body()

                _lyricsKind.value = response.kind

                response.lrc?.takeIf { it.isNotBlank() }?.let { rawLrc ->
                    _lyricsSegments.value = EnhancedLyricsParser.parseEnhancedLrc(rawLrc)
                } ?: run {
                    _lyricsSegments.value = emptyList()
                }

            } catch (e: Exception) {
                _lyricsSegments.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

