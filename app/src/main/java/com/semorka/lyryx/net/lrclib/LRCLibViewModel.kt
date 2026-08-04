package com.semorka.lyryx.net.lrclib

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semorka.lyryx.net.networkClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.regex.Pattern
import kotlin.math.abs

@Serializable
data class LyricSegment(
    val timeMillis: Long,
    val text: String
)

@Serializable
data class LRCLibTrack(
    val trackName: String,
    val artistName: String,
    val duration: Double,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null,
) {
    val hasSynced: Boolean get() = !syncedLyrics.isNullOrBlank()
    val hasPlain: Boolean get() = !plainLyrics.isNullOrBlank()

    val parsedSegments: List<LyricSegment>
        get() = LyricsParser.parseAndSplitLyrics(syncedLyrics ?: plainLyrics ?: "")

    val isActuallySynced: Boolean
        get() = parsedSegments.any { it.timeMillis > 0L }
}

class LRCLibViewModel : ViewModel() {
    private val _lyricsState = MutableStateFlow<List<LRCLibTrack>>(emptyList())
    val lyricsState = _lyricsState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    fun findLyrics(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = networkClient.get("https://lrclib.net/api/search") {
                    parameter("q", query)
                }

                if (response.status.isSuccess()) {
                    val rawTracks = response.body<List<LRCLibTrack>>()

                    val processed = rawTracks
                        .map { track -> processTrackLyrics(track) }
                        .filter { it.hasPlain }
                    
                    _lyricsState.value = processed
                }
            } catch (e: Exception) {
                _lyricsState.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun findLyricsByName(artistName: String, trackName: String) {
        viewModelScope.launch {
            try {
                val response = networkClient.get("https://lrclib.net/api/get") {
                    parameter("artist_name", artistName)
                    parameter("track_name", trackName)
                }

                if (response.status.isSuccess()) {
                    val rawTrack = response.body<LRCLibTrack>()
                    val processedTrack = processTrackLyrics(rawTrack)

                    if (processedTrack.hasPlain) {
                        _lyricsState.value = listOf(processedTrack)
                    } else {
                        _lyricsState.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                _lyricsState.value = emptyList()
            }
        }
    }

    fun findLyricsSmart(artist: String, title: String, duration: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _lyricsState.value = emptyList()

            try {
                val response = networkClient.get("https://lrclib.net/api/get") {
                    parameter("artist_name", artist)
                    parameter("track_name", title)
                    if (duration != null) {
                        parameter("duration", duration)
                    }
                }
                if (response.status.isSuccess()) {
                    val track = response.body<LRCLibTrack>()
                    val processed = processTrackLyrics(track)
                    if (processed.hasPlain) {
                        _lyricsState.value = listOf(processed)
                        _isLoading.value = false
                        return@launch
                    }
                }
            } catch (e: Exception) {
            }

            try {
                val cleanTitle = title.replace(Regex("\\(.*?\\)|\\[.*?]"), "").trim()
                val query = "$artist $cleanTitle"

                val response = networkClient.get("https://lrclib.net/api/search") {
                    parameter("q", query)
                }

                if (response.status.isSuccess()) {
                    val rawTracks = response.body<List<LRCLibTrack>>()
                    val processed = rawTracks
                        .map { track -> processTrackLyrics(track) }
                        .filter { it.hasPlain }
                    
                    _lyricsState.value = if (duration != null) {
                        processed.sortedBy { abs(it.duration.toInt() - duration) }
                    } else {
                        processed
                    }
                }
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun processTrackLyrics(track: LRCLibTrack): LRCLibTrack {
        if (!track.plainLyrics.isNullOrBlank()) return track

        if (!track.syncedLyrics.isNullOrBlank()) {
            val cleanPlain = LyricsParser.stripTimecodes(track.syncedLyrics)
            return track.copy(plainLyrics = cleanPlain)
        }
        return track
    }
}

object LyricsParser {
    private val lrcPattern = Pattern.compile("^\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})]\\s*(.*)$")

    fun stripTimecodes(syncedLyrics: String): String {
        return syncedLyrics.lines().map { line ->
            val trimmed = line.trim()
            val matcher = lrcPattern.matcher(trimmed)
            if (matcher.matches()) {
                matcher.group(4)?.trim() ?: ""
            } else {
                trimmed
            }
        }.filter { it.isNotEmpty() }.joinToString("\n")
    }

    fun parseAndSplitLyrics(rawLyrics: String, maxCharsPerLine: Int = 28): List<LyricSegment> {
        if (rawLyrics.isBlank()) return emptyList()

        val lines = rawLyrics.lines()
        val result = mutableListOf<LyricSegment>()

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            val matcher = lrcPattern.matcher(trimmedLine)

            if (matcher.matches()) {
                val minutes = matcher.group(1)?.toLong() ?: 0L
                val seconds = matcher.group(2)?.toLong() ?: 0L
                val factionGroup = matcher.group(3) ?: "00"
                val textContent = matcher.group(4)?.trim() ?: ""

                val msMultiplier = if (factionGroup.length == 2) 10 else 1
                val milliseconds = factionGroup.toLong() * msMultiplier

                val totalTimeMillis = (minutes * 60 + seconds) * 1000 + milliseconds

                val formattedText = splitLyricIntoLines(textContent, maxCharsPerLine)
                result.add(LyricSegment(timeMillis = totalTimeMillis, text = formattedText))
            } else {
                val formattedText = splitLyricIntoLines(trimmedLine, maxCharsPerLine)
                result.add(LyricSegment(timeMillis = 0L, text = formattedText))
            }
        }
        return result
    }

    private fun splitLyricIntoLines(lyrics: String, maxCharsPerLine: Int): String {
        if (lyrics.length <= maxCharsPerLine) return lyrics

        val naturalBreaks = listOf(",", " - ", "…", " – ", ";", "|", ":")

        for (breakChar in naturalBreaks) {
            if (lyrics.contains(breakChar)) {
                val parts = lyrics.split(breakChar)
                if (parts.size == 2) {
                    val firstPart = parts[0].trim()
                    val secondPart = parts[1].trim()
                    val lengthDiff = abs(firstPart.length - secondPart.length)

                    if (firstPart.length <= maxCharsPerLine + 5 &&
                        secondPart.length <= maxCharsPerLine + 5 &&
                        lengthDiff <= 10) {
                        return "$firstPart\n$secondPart"
                    }
                }
            }
        }

        val words = lyrics.split(" ")
        if (words.size <= 1) return lyrics

        val lines = mutableListOf<String>()
        var currentLine = ""

        for (i in words.indices) {
            val word = words[i]
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"

            if (testLine.length <= maxCharsPerLine) {
                currentLine = testLine
            } else {
                if (i < words.size - 1) {
                    val nextWord = words[i + 1]
                    val lineWithNext = "$testLine $nextWord"
                    val currentLineLength = currentLine.length
                    val nextLineLength = if (i + 2 < words.size) words[i + 2].length else 0

                    if (lineWithNext.length <= maxCharsPerLine + 3 &&
                        abs(currentLineLength - nextLineLength) <= 8) {
                        currentLine = testLine
                        continue
                    }
                }

                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        if (lines.size > 2) {
            return balanceLines(lines, maxCharsPerLine)
        }

        return lines.joinToString("\n")
    }

    private fun balanceLines(lines: List<String>, maxCharsPerLine: Int): String {
        return lines.joinToString("\n")
    }
}