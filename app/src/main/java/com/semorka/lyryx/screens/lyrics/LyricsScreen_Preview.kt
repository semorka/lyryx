package com.semorka.lyryx.screens.lyrics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.semorka.lyryx.MockPlayerViewModel
import com.semorka.lyryx.music.Music
import com.semorka.lyryx.ui.theme.LyryxTheme
import kotlinx.coroutines.delay

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LyricsScreenPreview() {
    val previewMusic = Music(
        "Snow Strippers",
        "Under Your Spell",
        """
        [00:00.00] First line of the song
        [00:01.50] Second line with longer text
        [00:03.00] Third line here
        [00:04.50] Another line of lyrics
        """.trimIndent()
    )

    val mockPlayerVm = viewModel<MockPlayerViewModel>()

    LyryxTheme(dynamicColor = false) {
        LyricsScreen(
            navController = rememberNavController(),
            music = previewMusic,
            audioUri = null,
            playerVm = mockPlayerVm,
            isPreview = true
        )
    }
}

@Composable
fun LyricsScreenPreviewContent(music: Music) {
    var previewTime by remember { mutableStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    val onsetTimestamps = remember { mutableStateListOf<Long>() }
    var lastOnsetTime by remember { mutableStateOf(0L) }
    var lineChanged by remember { mutableStateOf(false) }

    val syncedLines = remember(music.syncedLyrics) {
        music.syncedLyrics?.let { text ->
            text.lines().mapNotNull { line ->
                val timeMatch = Regex("\\[(\\d+):(\\d+)\\.(\\d+)\\]").find(line)
                if (timeMatch != null) {
                    val (min, sec, centi) = timeMatch.destructured
                    val timeMs = min.toLong() * 60_000 + sec.toLong() * 1_000 + centi.toLong() * 10
                    val cleanText = line.replace(Regex("\\[\\d+:\\d+\\.\\d+\\]\\s*"), "").trim()
                    timeMs to cleanText
                } else null
            }.sortedBy { it.first }
        } ?: emptyList()
    }

    val currentLyric by remember(previewTime, syncedLines) {
        derivedStateOf {
            syncedLines
                .takeWhile { it.first <= previewTime }
                .lastOrNull()
                ?.second ?: ""
        }
    }

    var previousLyric by remember { mutableStateOf("") }
    var showMovingText by remember { mutableStateOf(false) }
    var movingText by remember { mutableStateOf("") }

    LaunchedEffect(currentLyric) {
        if (currentLyric.isNotEmpty() && currentLyric != previousLyric) {
            movingText = previousLyric
            if (previousLyric.isNotEmpty()) {
                showMovingText = true
                delay(600)
                showMovingText = false
            }
            previousLyric = currentLyric
        }
    }

    val isOnsetActive by remember(previewTime, lastOnsetTime) {
        derivedStateOf {
            previewTime - lastOnsetTime < 150
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            previewTime += 100

            if (previewTime % 800 == 0L) {
                lastOnsetTime = previewTime
                onsetTimestamps.add(previewTime)
            }
            if (previewTime % 1000 == 0L) {
                lineChanged = true
                delay(500)
                lineChanged = false
            }

            if (previewTime >= 3000) {
                previewTime = 0
            }
        }
    }

    LaunchedEffect(currentLyric) {
        if (currentLyric.isNotEmpty() && currentLyric != "♪") {
            lineChanged = true
            delay(500)
            lineChanged = false
        }
    }

    LyricsScreenUI(
        music = music,
        currentLyric = currentLyric,
        isPlaying = isPlaying,
        formattedTime = String.format("%02d:%02d", previewTime / 60_000, (previewTime % 60_000) / 1_000),
        isOnsetActive = isOnsetActive,
        lineChanged = lineChanged,
        showMovingText = showMovingText,
        movingText = movingText,
        onPlayPause = { isPlaying = !isPlaying },
        showPreviewInfo = true
    )
}