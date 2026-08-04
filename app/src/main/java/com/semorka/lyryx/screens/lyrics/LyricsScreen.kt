package com.semorka.lyryx.screens.lyrics

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.semorka.lyryx.sound.v2.ExoPlayerViewModel
import com.semorka.lyryx.music.MusicViewModel
import com.semorka.lyryx.net.word_lyrics.WordLyricsViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.text.ifEmpty

@OptIn(UnstableApi::class)
@Composable
fun LyricsScreen(
    navController: NavController,
    audioUri: Uri?,
    playerVm: ExoPlayerViewModel?,
    isPreview: Boolean = false,
    musicVm: MusicViewModel,
    wordsVm: WordLyricsViewModel
) {
    LyricsScreenContent(navController, audioUri, playerVm, musicVm, wordsVm)
}

@Composable
fun MovingTextCopy(originalText: String, isActive: Boolean) {
    var animate by remember { mutableStateOf(false) }

    val offset by animateDpAsState(
        targetValue = if (animate) 300f.dp else 0f.dp,
        animationSpec = tween(1500)
    )

    val alpha by animateFloatAsState(
        targetValue = if (animate) 0f else 0.5f,
        animationSpec = tween(1200)
    )

    LaunchedEffect(isActive) {
        if (isActive) {
            animate = true
            delay(1500)
            animate = false
        }
    }

    Text(
        text = originalText,
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
        modifier = Modifier.offset(y = offset),
        textAlign = TextAlign.Center
    )
}

fun splitLyricIntoLines(lyrics: String, maxCharsPerLine: Int = 28): String {
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
    val result = lines.toMutableList()

    for (i in 0 until result.size - 1) {
        val currentLine = result[i]
        val nextLine = result[i + 1]

        val currentWords = currentLine.split(" ").toMutableList()
        val nextWords = nextLine.split(" ").toMutableList()

        if (currentLine.length < nextLine.length - 5 && currentWords.isNotEmpty() && nextWords.isNotEmpty()) {
            val lastWordOfCurrent = currentWords.last()
            val firstWordOfNext = nextWords.first()

            if ("$lastWordOfCurrent $firstWordOfNext".length <= maxCharsPerLine) {
                currentWords[currentWords.size - 1] = "$lastWordOfCurrent $firstWordOfNext"
                nextWords.removeAt(0)

                result[i] = currentWords.joinToString(" ")
                result[i + 1] = nextWords.joinToString(" ")
            }
        }
    }

    return result.joinToString("\n")
}

fun calculateMaxCharsPerLine(currentLyric: String): Int {
    val cleanLyric = currentLyric.ifEmpty { "♪" }

    return when {
        cleanLyric.any { it in "ABCDEFGHIJKLMNOPQRSTUVWXYZ" } -> 24
        cleanLyric.length > 60 -> 30
        cleanLyric.length > 40 -> 28
        else -> 26
    }
}

fun calculateBaseTextSize(currentLyric: String): Float {
    val cleanLyric = currentLyric.ifEmpty { "♫" }

    val lineCount = cleanLyric.count { it == '\n' } + 1
    val totalLength = cleanLyric.replace("\n", "").length

    return when {
        lineCount == 1 && totalLength <= 15 -> 42f
        lineCount == 1 && totalLength <= 25 -> 38f
        lineCount == 1 && totalLength <= 35 -> 34f
        lineCount == 1 -> 30f

        lineCount >= 2 && totalLength <= 30 -> 36f
        lineCount >= 2 && totalLength <= 50 -> 32f
        lineCount >= 2 -> 30f
        else -> 28f
    }
}

fun getScaleMultiplier(currentLyric: String): Float {
    val cleanLyric = currentLyric.ifEmpty { "♪" }
    val lineCount = cleanLyric.count { it == '\n' } + 1
    val totalLength = cleanLyric.replace("\n", "").length

    return when {
        lineCount == 1 && totalLength <= 15 -> 1.25f
        lineCount == 1 && totalLength <= 25 -> 1.20f
        lineCount == 1 && totalLength <= 35 -> 1.15f
        lineCount == 1 -> 1.10f

        lineCount == 2 -> 1.28f
        lineCount == 3 -> 1.22f
        lineCount >= 4 -> 1.18f
        else -> 1.15f
    }
}
